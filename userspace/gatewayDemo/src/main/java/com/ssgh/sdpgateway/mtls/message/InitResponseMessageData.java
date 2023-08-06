package com.ssgh.sdpgateway.mtls.message;

import com.ssgh.sdpgateway.mtls.message.type.NetClient;
import com.ssgh.sdpgateway.mtls.message.type.NetService;
import lombok.Data;

import java.util.List;

@Data
public class InitResponseMessageData {
    public String hotp;
    public String hmac;
    public String updateTime;
    public String expireTime;
    public List<NetService> serviceList;
    public List<NetClient> clientAccessible;
}
