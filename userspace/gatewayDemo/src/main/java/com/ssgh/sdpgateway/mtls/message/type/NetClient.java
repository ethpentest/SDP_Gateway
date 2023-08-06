package com.ssgh.sdpgateway.mtls.message.type;

import lombok.Data;

import java.util.List;

@Data
public class NetClient {
    private Integer clientId;
    private String clientIp;
    private String clientHOTP;
    private String clientHMAC;
    private List<NetPolicy> accessibleList;

}
