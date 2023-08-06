#ifndef __SDPGATEWAY_H
#define __SDPGATEWAY_H

typedef struct client_hmac {
	__u32 client_ip;
    __u8 hmac[32];
    __u8 hotp[32];
} client_hmac;

typedef struct access_policy {
    __u64 client_ip_port;
    __u32 access;
} access_policy;

struct so_event {
	__be32 src_addr;
	__be32 dst_addr;
	union {
		__be32 ports;
		__be16 port16[2];
	};
	__u32 ip_proto;
	__u32 pkt_type;
	__u32 ifindex;
    __u32 my_pkt_type;
    __u32 access_flag;
};
typedef union ports {
    __be32 ports;
	__be16 port16[2];
} ports;

//base64编码
int base64_encode(const char *indata, int inlen, char *outdata, int *outlen);
//base64解码
int base64_decode(const char *indata, int inlen, char *outdata, int *outlen);

// #define ETH_H   14
// #define IP_H    20
// #define UDP_H   8

#endif