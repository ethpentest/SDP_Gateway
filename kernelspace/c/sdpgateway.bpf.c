#include <stddef.h>
#include <linux/bpf.h>
#include <linux/if_ether.h>
#include <linux/tcp.h>
#include <linux/version.h>
#include <linux/ip.h>
#include <linux/in.h>
#include <bpf/bpf_helpers.h>
#include <bpf/bpf_endian.h>
#include <string.h>
#include <stdlib.h>
#include <linux/pkt_cls.h>
#include "sdpgateway.h"

#define ETH_P_IP	0x0800		/* Internet Protocol packet	*/

/**
 * ring_buf类型的ebpf_map用来存储日志数据，并将其发送到用户态程序
*/
struct {
	__uint(type, BPF_MAP_TYPE_RINGBUF);
	__uint(max_entries, 256 * 1024);/* 256 KB */
} rb_log SEC(".maps");

/**
 * 存储控制器ip，允许控制器流量直接通过
*/
struct {
	__uint(type, BPF_MAP_TYPE_HASH);
	__uint(max_entries, 2048);
	__type(key, __u32);
	__type(value, __u32);
} controller_map SEC(".maps");

/**
 * 存储控制器ip，允许控制器流量直接通过
*/
struct {
	__uint(type, BPF_MAP_TYPE_HASH);
	__uint(max_entries, 2048);
	__type(key, __u32);
	__type(value, __u32);
} attacker_map SEC(".maps");

/**
 * 存储从数据库获取客户端代理的hmac值和hotp值
 * 作为客户端代理进行spa认证的依据
*/
struct {
    __uint(type, BPF_MAP_TYPE_HASH);
    __uint(max_entries, 2048);
    __type(key, __u32);
    __type(value, client_hmac);
} client_hmac_map SEC(".maps");

/**
 * 存储通过spa认证的client
 * key: 合法client的ip地址
 * value： 时间辍--内核时间
*/
struct {
	__uint(type, BPF_MAP_TYPE_HASH);
	__uint(max_entries, 2048);
	__type(key, __u32);
	__type(value, __u64);
} client_auth_spa_map SEC(".maps");

/**
 * 存储访问控制策略，从数据库中获取
 * key：合法客户端的ip地址和所访问的端口号port组成的64位数
 * value：是否运行通过，1：通过，0：不通过
*/
struct {
    __uint(type, BPF_MAP_TYPE_HASH);
	__uint(max_entries, 2048);
	__type(key, __u64);
	__type(value, __u32);
} policy_map SEC(".maps");

/**
 * 判断Hotp和hmac是否相同
*/
int is_same(__u8 src[], __u8 dest[]) {
    int is_equal = 1;
    for (int i = 0; i < 32; i ++) {
        if (src[i] != dest[i]) {
            is_equal = 0;
            break;
        }
    }
    return is_equal;
}

/**
 * 向用户空间传输数据，上传流量日志信息
*/
static void send_data(struct __sk_buff *skb, __u32 type, __u32 access_flag) {

    struct so_event *e;
	__u8 verlen1;
    __u32 src_addr;
    __u32 dest_addr;
    /**
     * 通过ringbuf上传流量日志信息
     * reserve sample from BPF ringbuf
    */
	e = bpf_ringbuf_reserve(&rb_log, sizeof(*e), 0);
	if (!e)
		return;

    bpf_skb_load_bytes(skb, ETH_HLEN + offsetof(struct iphdr, saddr), &(src_addr), 4);
    src_addr = __bpf_ntohl(src_addr);
    bpf_skb_load_bytes(skb, ETH_HLEN + offsetof(struct iphdr, daddr), &(dest_addr), 4);
    dest_addr = __bpf_ntohl(dest_addr);

    bpf_skb_load_bytes(skb, ETH_HLEN + offsetof(struct iphdr, protocol), &e->ip_proto, 1);

	if (e->ip_proto != IPPROTO_GRE) {
		bpf_skb_load_bytes(skb, ETH_HLEN + offsetof(struct iphdr, saddr), &(e->src_addr), 4);
		bpf_skb_load_bytes(skb, ETH_HLEN + offsetof(struct iphdr, daddr), &(e->dst_addr), 4);
	}

    bpf_skb_load_bytes(skb, ETH_HLEN + 0, &verlen1, 1);
	bpf_skb_load_bytes(skb, ETH_HLEN + ((verlen1 & 0xF) << 2), &(e->ports), 4);
	e->pkt_type = skb->pkt_type;
	e->ifindex = skb->ifindex;
    e->my_pkt_type = type;
    e->access_flag = access_flag;
    // bpf_printk("e->src_addr = %u e->dst_addr = %u e->access = %u\n", e->src_addr, e->dst_addr, e->access_flag);
    bpf_ringbuf_submit(e, 0);
}

/**
 * 处理攻击者数据报文
*/
int handle_attacker(struct __sk_buff *skb, __u32 attacker_ip) {
    int rc = TC_ACT_SHOT;
    int my_pkt_tpye = 0; // 攻击者IP数据包
    send_data(skb, my_pkt_tpye, rc);
    return rc;
}

/**
 * 处理控制器数据报文
*/
int handle_controller(struct __sk_buff *skb, __u32 controller_ip) {
    int rc = TC_ACT_OK;
    int my_pkt_tpye = 1; // 控制器数据报
    send_data(skb, my_pkt_tpye, rc);
    return rc;
}

/**
 * authed_client_ip：经过spa认证的客户端的时间辍
*/
int handle_authed_client(struct __sk_buff *skb) {
    int rc = TC_ACT_SHOT;
    
    int my_pkt_tpye;

    __u32 nhoff = ETH_HLEN;
    __u32 src_addr; // 数据包原ip地址
	__u16 port; // 传输层端口号

	// 获取数据包的ip地址
    bpf_skb_load_bytes(skb, nhoff + offsetof(struct iphdr, saddr), &src_addr, 4);
    src_addr = __bpf_ntohl(src_addr);

	// 获取数据包的目的端口号
    bpf_skb_load_bytes(skb, 36, &port, 2);
    port = __bpf_ntohs(port);
    
    __u64 *authed_client_ts;
    __u64 now_ts;
    authed_client_ts = bpf_map_lookup_elem(&client_auth_spa_map, &src_addr);
    now_ts = bpf_ktime_get_ns();

    if (authed_client_ts != NULL && (now_ts - *authed_client_ts) < 1000000000L * 60 * 60) { // 设置超时时间为 60 分钟
        __u64 ip_port = (__u64) src_addr << 32 | port;
        __u32 *policy_access;
        policy_access = bpf_map_lookup_elem(&policy_map, &ip_port);
        if (policy_access != NULL && *policy_access == 1) {
            // bpf_printk("authed ip = %u\t, port = %u\t, policy_access = %u\n", src_addr, port, *policy_access);
            my_pkt_tpye = 2; // 访问服务成功的数据报
            rc = TC_ACT_OK; 
        } else {
            my_pkt_tpye = 3; // 经过授权认证的数据报，但是没有访问该服务的权限
            rc = TC_ACT_SHOT;
        }
    } else {
        my_pkt_tpye = 4; // hmac 超时过期 
        long flag;
        flag = bpf_map_delete_elem(&client_auth_spa_map, &src_addr);
    }
    send_data(skb, my_pkt_tpye, rc);
    return rc; 
}

//普通client携带spa报文进行敲门
int handle_unauthed_client(struct __sk_buff *skb) {

    int my_pkt_tpye;
    int rc = TC_ACT_SHOT;
    __u32 addr; // 数据包原ip地址

    __u8 hmac[32];
    __u8 hotp[32];

    client_hmac *client; // 保存代理的hmac和hotp值
	// 获取数据包的ip地址
    bpf_skb_load_bytes(skb, ETH_HLEN + offsetof(struct iphdr, saddr), &addr, 4);
    addr = __bpf_ntohl(addr);
    // 获取 hmac和hotp
    bpf_skb_load_bytes(skb, 138, &hotp, 32);
    bpf_skb_load_bytes(skb, 170, &hmac, 32);

    // if (addr == 2046309732) {
    //     for (int i = 0; i < 32; i++) {
    //         bpf_printk("%d,", hmac[i]);
    //     }
    //     for (int i = 0; i < 32; i++) {
    //         bpf_printk("%d,", hotp[i]);
    //     }
    // }
    
	client = bpf_map_lookup_elem(&client_hmac_map, &addr);
   
    if (client != NULL && is_same(hotp, client->hotp) && is_same(hmac, client->hmac)) {
        bpf_printk("spa success\n");
        __u64 client_ts = bpf_ktime_get_ns();
        bpf_map_update_elem(&client_auth_spa_map, &addr, &client_ts, BPF_ANY);
        my_pkt_tpye = 5; // spa 认证成功
        rc = TC_ACT_OK;
    } else {
        my_pkt_tpye = 6; // spa 认证失败
        rc = TC_ACT_SHOT;
    }
    send_data(skb, my_pkt_tpye, rc);
    return rc;
}


SEC("tc")
int sdp_gateway_ingress(struct __sk_buff *skb) {

	int rc = TC_ACT_SHOT;

	__u8 verlen;
    __u32 my_pkt_tpye;
    __u32 nhoff = ETH_HLEN;
    __u32 src_addr; // 数据包原ip地址
	ports s_d_port; // 传输层端口号
    __u32 *is_controller_ip; // 判断数据包地址是否为控制器地址
    __u32 *is_attacker_ip; // 判断数据包是否为攻击者地址
    __u64 *is_authed_client_ip; // 判断数据包地址是否为经过SPA认证的客户端地址

    if (skb->protocol != bpf_htons(ETH_P_IP))
		return TC_ACT_OK;

    // 获取数据包的ip地址
    bpf_skb_load_bytes(skb, nhoff + offsetof(struct iphdr, saddr), &(src_addr), 4);
    src_addr = __bpf_ntohl(src_addr);

	// 获取数据包的目的端口号
	// bpf_skb_load_bytes(skb, nhoff + 0, &verlen, 1);
	// bpf_skb_load_bytes(skb, nhoff + ((verlen & 0xF) << 2), &(s_d_port.ports), 4);
    // __u16 dest_port = __bpf_ntohs(s_d_port.port16[1]);

    is_attacker_ip = bpf_map_lookup_elem(&attacker_map, &src_addr);
    is_controller_ip = bpf_map_lookup_elem(&controller_map, &src_addr);
    is_authed_client_ip = bpf_map_lookup_elem(&client_auth_spa_map, &src_addr);
    
    if (is_attacker_ip) {
        // 如果是攻击者发送的数据包，则直接拦截丢弃
        rc = handle_attacker(skb, *is_attacker_ip);
    } else if (is_controller_ip) {
        //1、首先判断是否为控制器Controller发送的数据包，
        //如果是则将控制器发送的hmac和hotp值进行保存在controller map中
        rc = handle_controller(skb, *is_controller_ip);
	} else if(is_authed_client_ip) {
        //2、判断是否为经过SPA认证的client，如果是，查看认证是否过期，若过期则丢弃，否则判断权限
        //并且判断该client是否有权限访问指定端口的数据
        rc = handle_authed_client(skb);
    } else {
        //3、判断为未经认证的client发送的数据包，解析SPA数据包内容，
        // 与client_hmac_map中保存的hmac和hotp值进行比较
	    //通过认证则将该client的ip和时间辍保存在client_auth_spa_map中
        rc = handle_unauthed_client(skb);
    }
    
    return rc;
}


char __license[] SEC("license") = "GPL";
