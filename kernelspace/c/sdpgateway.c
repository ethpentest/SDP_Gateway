#include <signal.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <bpf/libbpf.h>
#include <pthread.h>
#include <time.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/syscall.h> /*添加上此头文件*/
#include <asm/unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <linux/if_ether.h>
#include <linux/if_packet.h>
#include <sys/ioctl.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <net/if.h>
#include "/usr/include/mysql/mysql.h"
#include "sdpgateway.skel.h"
#include "sdpgateway.h"
#include "zlog.h"
#pragma GCC diagnostic ignored "-Wchar-subscripts"
#pragma GCC diagnostic ignored "-Wformat-security"

#define LEN 16

static const char * ipproto_mapping[IPPROTO_MAX] = {
	[IPPROTO_IP] = "IP",
	[IPPROTO_ICMP] = "ICMP",
	[IPPROTO_IGMP] = "IGMP",
	[IPPROTO_IPIP] = "IPIP",
	[IPPROTO_TCP] = "TCP",
	[IPPROTO_EGP] = "EGP",
	[IPPROTO_PUP] = "PUP",
	[IPPROTO_UDP] = "UDP",
	[IPPROTO_IDP] = "IDP",
	[IPPROTO_TP] = "TP",
	[IPPROTO_DCCP] = "DCCP",
	[IPPROTO_IPV6] = "IPV6",
	[IPPROTO_RSVP] = "RSVP",
	[IPPROTO_GRE] = "GRE",
	[IPPROTO_ESP] = "ESP",
	[IPPROTO_AH] = "AH",
	[IPPROTO_MTP] = "MTP",
	[IPPROTO_BEETPH] = "BEETPH",
	[IPPROTO_ENCAP] = "ENCAP",
	[IPPROTO_PIM] = "PIM",
	[IPPROTO_COMP] = "COMP",
	[IPPROTO_SCTP] = "SCTP",
	[IPPROTO_UDPLITE] = "UDPLITE",
	[IPPROTO_MPLS] = "MPLS",
	[IPPROTO_RAW] = "RAW"
};

#define HOST 		"localhost" /*MySql服务器地址*/
#define PORT 		3306
#define USERNAME 	"root" /*用户名*/
#define PASSWORD 	"111111" /*数据库连接密码*/
#define DATABASE 	"sdp_gateway_new" /*需要连接的数据库*/
#define IP_SIZE    	16

MYSQL m_sqlCon;
MYSQL mysql_connet_read;

//日志
int rc;
zlog_category_t *category;


static volatile sig_atomic_t exiting = 0;

static void sig_int(int signo) {
    exiting = 1;
}

static int libbpf_print_fn(enum libbpf_print_level level, const char *format, va_list args) {
	return vfprintf(stderr, format, args);
}

static __u32 ipTOint(char* ipstr) {
	char *temp = strtok(ipstr, ".");
    __u32 ip[4];
    int i = 0;
    while (temp)
    {
        ip[i++] = atoi(temp);
        temp = strtok(NULL, ".");
    }
    __u32 IP_Int =  (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	return IP_Int;
}

/** 
 * 逆置字符串 
 */
void swapStr(char *str, int begin, int end) { 
  int i, j; 
  
  for (i = begin, j = end; i <= j; i ++, j --) { 
    if (str[i] != str[j]) { 
      str[i] = str[i] ^ str[j]; 
      str[j] = str[i] ^ str[j]; 
      str[i] = str[i] ^ str[j]; 
    } 
  } 
} 
  
/** 
 * 整形转ip字符串 
 */
char* ipTstr(__u32 ipint) { 
  char *new = (char *)malloc(LEN); 
  memset(new, '\0', LEN); 
  new[0] = '.'; 
  char token[4]; 
  int bt, ed, len, cur; 
  
  while (ipint) { 
    cur = ipint % 256; 
    sprintf(token, "%d", cur); 
    strcat(new, token); 
    ipint /= 256; 
    if (ipint) strcat(new, "."); 
  } 
  
  len = strlen(new); 
  swapStr(new, 0, len - 1); 
  
  for (bt = ed = 0; ed < len;) { 
    while (ed < len && new[ed] != '.') { 
      ed ++; 
    } 
    swapStr(new, bt, ed - 1); 
    ed += 1; 
    bt = ed; 
  } 
  
  new[len - 1] = '\0'; 
  
  return new; 
} 

/*
int get_local_ip(char *eth_inf, char *ip) {
	int sd;
    struct sockaddr_in sin;
    struct ifreq ifr;
 
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (-1 == sd)
    {
        printf("socket error: %s\n", strerror(errno));
        return -1;
    }
 
    strncpy(ifr.ifr_name, eth_inf, IFNAMSIZ);
    ifr.ifr_name[IFNAMSIZ - 1] = 0;
 
    // if error: No such device
    if (ioctl(sd, SIOCGIFADDR, &ifr) < 0)
    {
        printf("ioctl error: %s\n", strerror(errno));
        close(sd);
        return -1;
    }
 
    memcpy(&sin, &ifr.ifr_addr, sizeof(sin));
    snprintf(ip, IP_SIZE, "%s", inet_ntoa(sin.sin_addr));
 
    close(sd);
    return 0;

}
*/

//base64表，用于编码
static const char Base64Code[64] = 
{
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
    'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
    'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
    'w', 'x', 'y', 'z', '0', '1', '2', '3',
    '4', '5', '6', '7', '8', '9', '+', '/'
};
//base64逆表，用于解码
static const char Base64Back[128] = 
{
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1,  0, -1, -1,
    -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 
    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, 
    -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 
    41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1,
};
//编码过程，其中in待编码字符，out为编码后字符
void base64(char* in,char* out)
{
	int i,j=0,w,m;
	w=strlen(in)/3*3;
	m=strlen(in)%3;
	
	for(i = 0;i < w;i+=3){
			out[j++] = Base64Code[in[i]>>2];
			out[j++] = Base64Code[(in[i]<<4 & 0x3f) | in[i+1]>>4];
			out[j++] = Base64Code[(in[i+1]<<2 & 0x3f) | in[i+2]>>6];
			out[j++] = Base64Code[in[i+2] & 0x3f];
	}
	switch(m){
		case 0:	out[j] = '\0';
				return;
		case 1:	out[j++] = Base64Code[in[i]>>2];
				out[j++] = Base64Code[(in[i]<<4 & 0x3f)];
				out[j++] = '=';
				out[j++] = '=';
				out[j] = '\0';
				return;
		default:out[j++] = Base64Code[in[i]>>2];
				out[j++] = Base64Code[(in[i]<<4 & 0x3f) | in[i+1]>>4];
				out[j++] = Base64Code[(in[i+1]<<2 & 0x3f)];
				out[j++] = '=';
				out[j] = '\0';
				return;
	}
}

//解码函数，in为代解码字符，out为解码后的字符
void base64_d(char* in,char* out)
{
	int i,j=0,w;
	w=strlen(in);
	for(i = 0;i < w;i+=4){
		out[j++] = Base64Back[in[i]]<<2 | Base64Back[in[i+1]]>>4;
		out[j++] = Base64Back[in[i+1]]<<4 | Base64Back[in[i+2]]>>2;
		out[j++] = Base64Back[in[i+2]]<<6 | Base64Back[in[i+3]];
	}
	
	if(in[i+2] == '='){			//倒数第二位为'='
		out[j++] = Base64Back[in[i]]<<2 | Base64Back[in[i+1]]>>4;
		out[j]='\0';
		return;
	}
	else if(in[i+3] == '=' ){	//最后一位为'='
		out[j++] = Base64Back[in[i]]<<2 | Base64Back[in[i+1]]>>4;
		out[j++] = Base64Back[in[i+1]]<<4 | Base64Back[in[i+2]]>>2;
		out[j++] = '\0';
	}
	else{
		out[j++] = Base64Back[in[i]]<<2 | Base64Back[in[i+1]]>>4;
		out[j++] = Base64Back[in[i+1]]<<4 | Base64Back[in[i+2]]>>2;
		out[j++] = Base64Back[in[i+2]]<<6 | Base64Back[in[i+3]];
		out[j] = '\0';
		return;
	}
}

//  获取当前系统时间 yyyy-mm-dd hh:mm:ss
int get_local_data(char *out) {
    char data[80];
	size_t out_size = sizeof(out);
    memset(out, 0, out_size);
    memset(data, 0, sizeof(data));
    time_t rawtime;
    time(&rawtime);
    struct tm ts;
    ts = *localtime(&rawtime);
    strftime(data, sizeof(data), "%Y-%m-%d %H:%M:%S", &ts);
    // printf("当前时间： %s\n", data);
    strcpy(out, data);

    return 0;
}

//  获取当前系统时间  mm
int get_local_data_minute(char *out) {
    char data[80];
	size_t out_size = sizeof(out);
    memset(out, 0, out_size);
    memset(data, 0, sizeof(data));
    time_t rawtime;
    time(&rawtime);
    struct tm ts;
    ts = *localtime(&rawtime);
    strftime(data, sizeof(data), "%M", &ts);
    // printf("当前时间： %s", data);
    strcpy(out, data);

    return 0;
}

//  获取当前系统时间 hh
int get_local_data_hour(char *out) {
    char data[80];
	size_t out_size = sizeof(out);
    memset(out, 0, out_size);
    memset(data, 0, sizeof(data));
    time_t rawtime;
    time(&rawtime);
    struct tm ts;
    ts = *localtime(&rawtime);
    strftime(data, sizeof(data), "%H", &ts);
    // printf("当前时间： %s", data);
    strcpy(out, data);
    return 0;
}

/**
 * 从内核获取日志信息后上传到数据库中
*/
static int handle_event(void *ctx, void *data, size_t data_sz) {

	const struct so_event *e = data;
	char ifname[IF_NAMESIZE];

	if (e->pkt_type != PACKET_HOST)
		return 0;

	if (e->ip_proto < 0 || e->ip_proto >= IPPROTO_MAX)
		return 0;

	if (!if_indextoname(e->ifindex, ifname))
		return 0;

	struct in_addr saddr = {0};
	struct in_addr daddr = {0};
	char interface[32], sip[32], dip[32];
    memset(interface, 0, sizeof(interface));
	memset(sip, 0, sizeof(sip));
	memset(dip, 0, sizeof(dip));

	memcpy(&saddr.s_addr, &e->src_addr, sizeof(e->src_addr));
	memcpy(&daddr.s_addr, &e->dst_addr, sizeof(e->dst_addr));

    strcpy(interface, ifname);  
	strcpy(sip, inet_ntoa(saddr));
	strcpy(dip, inet_ntoa(daddr));

	char create_time[255], create_hour[2]; // create_date[255], create_minute[2],
	get_local_data(create_time);
	get_local_data_hour(create_hour);

	// 数据报文类型
	int pkt_type = e->my_pkt_type;
	char *pkt_type_str;
	switch (pkt_type) {
	case 0:
		pkt_type_str = "attacker_packet";
		break;
	case 1:
		pkt_type_str = "controller_packet";
		break;
	case 2:
		pkt_type_str = "access_service_success";
		break;
	case 3:
		pkt_type_str = "access_service_failed";
		break;
	case 4:
		pkt_type_str = "hmac_and_hotp_time_out";
		break;
	case 5:
		pkt_type_str = "spa_packet_success";
		break;
	case 6:
		pkt_type_str = "spa_packet_failed";
		break;
	default:
		pkt_type_str = "";
		break;
	}

	// 数据报文是否通过
	int ac_flag = e->access_flag;
	char *ac_flag_str;
	switch (ac_flag) {
	case 0:
		ac_flag_str = "allow";
		break;
	case 2:
		ac_flag_str = "deny";
		break;
	default:
		ac_flag_str = "";
		break;
	}

	char buff[512];
	sprintf(buff, "interface: %s\tprotocol: %s\tsrc_addr: %s\tdest_addr: %s\tsrc_port: %d\tdest_port: %d\tpkt_type: %s\taccess_flag: %s\tcreate_time: %s",
			interface, ipproto_mapping[e->ip_proto], sip, dip, ntohs(e->port16[0]), ntohs(e->port16[1]), pkt_type_str, ac_flag_str, create_time);
	zlog_info(category, buff);

/*
	sprintf(buff, "INSERT INTO access_log(interface, protocol, src_addr, dest_addr, src_port, dest_port, pkt_type, access_flag, create_time) VALUES('%s', '%s', '%s', '%s', '%d', '%d', '%d', '%d', '%s')",
		interface, ipproto_mapping[e->ip_proto], sip, dip, ntohs(e->port16[0]), ntohs(e->port16[1]), e->my_pkt_type, e->access_flag, create_time);
	printf("sql = %s\n", buff);
	//插入数据库
	if (mysql_real_query(&m_sqlCon, buff, strlen(buff))) {
		// 返回0成功
		printf("mysql_real_query:%s\n", mysql_error(&m_sqlCon));
	}
	MYSQL_RES *res = mysql_store_result(&m_sqlCon);
    mysql_free_result(res);
*/
	
	return 0;
}

int init_mysql_connect() {

	MYSQL mysql_write, mysql_read;

    // 初始化写的mysql 对象
    mysql_init(&mysql_write);
	//连接数据库。3306是数据库端口号，CLIENT_MULTI_STATEMENTS为开启多SQL语句执行。
    if (!mysql_real_connect(&mysql_write, HOST, USERNAME, PASSWORD, DATABASE, PORT, NULL, 0)) {
        printf("mysql_real_connct [%s]\n", mysql_error(&mysql_write));
        return 1;
    }

    memcpy(&m_sqlCon, &mysql_write, sizeof(mysql_write));

    // 初始化读的mysql对象
    mysql_init(&mysql_read);
	//连接数据库。3306是数据库端口号，CLIENT_MULTI_STATEMENTS为开启多SQL语句执行。
    if (!mysql_real_connect(&mysql_read, HOST, USERNAME, PASSWORD, DATABASE, PORT, NULL, 0)) {
        printf("mysql_real_connct [%s]\n", mysql_error(&mysql_read));
        return 1;
    }
    memcpy(&mysql_connet_read, &mysql_read, sizeof(mysql_read));
    return 0;
}

/**
 * 从数据库查询控制器ip添加到controller_map中
 * 对controller_map中的ip直接方行
*/
void update_controller(struct bpf_map *controller_map) {
	char *sql = "select * from sdp_gateway_new.gateway_controller";
    if (mysql_real_query(&mysql_connet_read, sql, strlen(sql))) { //(返回0为成功)
        printf("从数据库查询client的hmac mysql_real_query:%s\n", mysql_error(&mysql_connet_read));
        return;
    }
	MYSQL_RES *res_controller;
    MYSQL_ROW row_controller;

	do {
		if (!(res_controller = mysql_store_result(&mysql_connet_read))) {
			printf("Got fatal error processing query/n");
		}
		while ((row_controller = mysql_fetch_row(res_controller))) {
			__u32 controller_ip = ipTOint(row_controller[1]);
			__u32 controller_flag = atoi(row_controller[2]);

			bpf_map__update_elem(controller_map, &controller_ip, sizeof(__u32), &controller_flag, sizeof(__u32), BPF_ANY);			

		}
		mysql_free_result(res_controller);
	} while (!mysql_next_result(&mysql_connet_read));

}

/**
 * 从数据库查询攻击者ip添加到attacker_map中
 * 对attacker_map中的ip直接拦截拒绝
*/
void update_attacker(struct bpf_map *attacker_map) {
	char *sql = "select * from sdp_gateway_new.gateway_attacker";
    if (mysql_real_query(&mysql_connet_read, sql, strlen(sql))) { //(返回0为成功)
        printf("从数据库查询client的hmac mysql_real_query:%s\n", mysql_error(&mysql_connet_read));
        return;
    }
	MYSQL_RES *res_attacker;
    MYSQL_ROW row_attacker;

	do {
		if (!(res_attacker = mysql_store_result(&mysql_connet_read))) {
			printf("Got fatal error processing query/n");
		}
		while ((row_attacker = mysql_fetch_row(res_attacker))) {
			__u32 attacker_ip = ipTOint(row_attacker[0]);
			__u32 attacker_flag = atoi(row_attacker[1]);

			bpf_map__update_elem(attacker_map, &attacker_ip, sizeof(__u32), &attacker_flag, sizeof(__u32), BPF_ANY);			

		}
		mysql_free_result(res_attacker);
	} while (!mysql_next_result(&mysql_connet_read));

}

/**
 * 从数据库查询client的hmac和hotp值进行更新
 * 		struct bpf_map *client_hmac_map;
*/
void update_client_hmac(struct bpf_map *client_hmac_map) {

	char *sql = "select * from sdp_gateway_new.gateway_client";
    if (mysql_real_query(&mysql_connet_read, sql, strlen(sql))) { //(返回0为成功)
        printf("从数据库查询client的hmac mysql_real_query:%s\n", mysql_error(&mysql_connet_read));
        return;
    }
	MYSQL_RES *res_hmac;
    MYSQL_ROW row_hmac;
	client_hmac client_hmac01;

	do {
		if (!(res_hmac = mysql_store_result(&mysql_connet_read))) {
            printf("Got fatal error processing query/n");
        }
		while ((row_hmac = mysql_fetch_row(res_hmac))) {
			__u32 client_ip = ipTOint(row_hmac[0]);
			client_hmac01.client_ip = client_ip;
			char *hmac_d = row_hmac[1]; // 从数据库中查询的hotp（base64加密）
			char *hotp_d = row_hmac[2]; // 从数据库中查询的hmac（base64加密）
			char hotp_e[32]; // 解密后的hotp
			char hmac_e[32]; // 解密后的hmac
			base64_d(hotp_d, hotp_e);
			base64_d(hmac_d, hmac_e);
			for (int i = 0; i < 32; i++) {
				client_hmac01.hmac[i] = hmac_e[i];
				client_hmac01.hotp[i] = hotp_e[i];
			}
			// printf("%d, %llu, %llu, %llu, %llu, %llu, %llu, %llu, %llu\n", client_hmac01.client_ip, client_hmac01.HMAC1, client_hmac01.HMAC2, client_hmac01.HMAC3, client_hmac01.HMAC4, client_hmac01.HOTP1, client_hmac01.HOTP2, client_hmac01.HOTP3, client_hmac01.HOTP4);
			bpf_map__update_elem(client_hmac_map, &client_ip, sizeof(__u32), &client_hmac01, sizeof(client_hmac), BPF_ANY);			
		}
		mysql_free_result(res_hmac);
	} while (!mysql_next_result(&mysql_connet_read));
}

/**
 * 从数据库查询policy进行更新
*/
void update_policy(struct bpf_map *policy_map) {
	
	char *sql = "select * from sdp_gateway_new.gateway_policy";
	if (mysql_real_query(&mysql_connet_read, sql, strlen(sql))) { //(返回0为成功)
        printf("从数据库查询policy进行更新 mysql_real_query:%s\n", mysql_error(&mysql_connet_read));
        return;
    }

	MYSQL_RES *res_policy;
    MYSQL_ROW row_policy;

	do {
		if (!(res_policy = mysql_store_result(&mysql_connet_read))) {
            printf("Got fatal error processing query/n");
        }
		while ((row_policy = mysql_fetch_row(res_policy))) {
			__u32 client_ip = ipTOint(row_policy[0]);
			__u32 service_port = atoi(row_policy[1]);
			__u32 policy_access = atoi(row_policy[2]);

			__u64 client_ip_port = (__u64) client_ip << 32 | service_port;

			bpf_map__update_elem(policy_map, &client_ip_port, sizeof(__u64), &policy_access, sizeof(__u32), BPF_ANY);			
		}
		mysql_free_result(res_policy);
	} while (!mysql_next_result(&mysql_connet_read));
}


int main(int argc, char **argv) {

	struct ring_buffer *rb_log = NULL;

	char if_name[255];
	printf("请输入网卡名称：\n");
	scanf("%s",if_name);

	int IFINDEX;
	IFINDEX = if_nametoindex(if_name);

	init_mysql_connect(); // 初始化mysql 连接
	
	//home && log dir
	// ../../../../../sdpgatewaylog/logconfig/sdpgatewaylog.conf
	char* log_dir = "../../../../../sdpgatewaylog/logconfig/sdpgatewaylog.conf";
	 

	rc = zlog_init(log_dir);
	if (rc) {
		printf("init failed\n");
		return -1;
	}
	category = zlog_get_category("sdpgatewaylog");
	if (!category) {
		printf("get sdpgateway log fail\n");
		zlog_fini();
		return -2;
	}

	// tc ingress 对入流量进行过滤拦截
	DECLARE_LIBBPF_OPTS(bpf_tc_hook, tc_hook_ingress,
		.ifindex = IFINDEX, .attach_point = BPF_TC_INGRESS);
	DECLARE_LIBBPF_OPTS(bpf_tc_opts, tc_opts_ingress,
		.handle = 1, .priority = 1);
/*
	// tc egress 对出流量进行拦截
	DECLARE_LIBBPF_OPTS(bpf_tc_hook, tc_hook_egress,
		.ifindex = IFINDEX, .attach_point = BPF_TC_EGRESS);
	DECLARE_LIBBPF_OPTS(bpf_tc_opts, tc_opts_egress,
		.handle = 1, .priority = 1);
*/
	struct sdpgateway_bpf *skel;
	bool hook_created = false;
	int err;

	struct rlimit r = {RLIM_INFINITY, RLIM_INFINITY};
		if (setrlimit(RLIMIT_MEMLOCK, &r)) {
				fprintf(stderr, "ERROR: setrlimit(RLIMIT_MEMLOCK) \"%s\"\n",
					strerror(errno));
				exit(EXIT_FAILURE);
			}

    libbpf_set_print(libbpf_print_fn);

    skel = sdpgateway_bpf__open_and_load();
    if (!skel) {
        fprintf(stderr, "Failed to open BPF skeleton\n");
		return 1;
    }

	/* Set up ring buffer polling */
	rb_log = ring_buffer__new(bpf_map__fd(skel->maps.rb_log), handle_event, NULL, NULL);

    
	// 创建ingress hook函数
    err = bpf_tc_hook_create(&tc_hook_ingress);
    if (!err)
		hook_created = true;
	if (err && err != -EEXIST) {
		fprintf(stderr, "Failed to create TC INGRESS hook: %d\n", err);
		goto cleanup;
	}
/*
	// 创建egress hook函数
	err = bpf_tc_hook_create(&tc_hook_egress);
    if (!err)
		hook_created = true;
	if (err && err != -EEXIST) {
		fprintf(stderr, "Failed to create TC EGRESS hook: %d\n", err);
		goto cleanup;
	}
*/
	// tc ingress opts
    tc_opts_ingress.prog_fd = bpf_program__fd(skel->progs.sdp_gateway_ingress);
    err = bpf_tc_attach(&tc_hook_ingress, &tc_opts_ingress);
    if (err) {
		fprintf(stderr, "Failed to attach TC INGRESS: %d\n", err);
		goto cleanup;
	}
/*
	// tc egress opts
	tc_opts_egress.prog_fd = bpf_program__fd(skel->progs.sdp_gateway_egress);
    err = bpf_tc_attach(&tc_hook_egress, &tc_opts_egress);
    if (err) {
		fprintf(stderr, "Failed to attach TC EGRESS: %d\n", err);
		goto cleanup;
	}
*/
	
	if (signal(SIGINT, sig_int) == SIG_ERR) {
			err = errno;
			fprintf(stderr, "Can't set signal handler: %s\n", strerror(errno));
			goto cleanup;
		}

    printf("Successfully started! Please run `sudo cat /sys/kernel/debug/tracing/trace_pipe` "
	       "to see output of the BPF program.\n");

    while (!exiting) {
		// 从数据库查询controller ip进行更新
		update_controller(skel->maps.controller_map);
		// 从数据库查询client的hmac和hotp值进行更新
		update_client_hmac(skel->maps.client_hmac_map);
		// 从数据库查询policy进行更新
		update_policy(skel->maps.policy_map);
		// 从数据库查询attacker进行更新
		update_attacker(skel->maps.attacker_map);

		err = ring_buffer__poll(rb_log, 10 /* timeout, ms */);
		/* Ctrl-C will cause -EINTR */
		if (err == -EINTR) {
			err = 0;
			break;
		}
		if (err < 0) {
			fprintf(stderr, "Error polling perf buffer: %d\n", err);
			break;
		}
		sleep(1);
	}

	// TC INGRESS DETACH
	zlog_fini();
    tc_opts_ingress.flags = tc_opts_ingress.prog_fd = tc_opts_ingress.prog_id = 0;
	err = bpf_tc_detach(&tc_hook_ingress, &tc_opts_ingress);
	if (err) {
		fprintf(stderr, "Failed to detach TC ingress: %d\n", err);
		goto cleanup;
	}
/*
	// TC EGRESS DETACH
    tc_opts_egress.flags = tc_opts_egress.prog_fd = tc_opts_egress.prog_id = 0;
	err = bpf_tc_detach(&tc_hook_egress, &tc_opts_egress);
	if (err) {
		fprintf(stderr, "Failed to detach TC egress: %d\n", err);
		goto cleanup;
	}
*/


    // tc_opts_egress.flags = tc_opts_egress.prog_fd = tc_opts_egress.prog_id = 0;
	// err = bpf_tc_detach(&tc_hook_egress, &tc_opts_egress);
	// if (err) {
	// 	fprintf(stderr, "Failed to detach TC: %d\n", err);
	// 	goto cleanup;
	// }

cleanup:
	if (hook_created)
		bpf_tc_hook_destroy(&tc_hook_ingress);
		// bpf_tc_hook_destroy(&tc_hook_egress);
	ring_buffer__free(rb_log);
	sdpgateway_bpf__destroy(skel);
	mysql_close(&m_sqlCon);
    mysql_close(&mysql_connet_read);
	zlog_fini();
	return -err;
    
}
