package com.ssgh.sdpgateway.mtls.message.type;

import lombok.Data;

@Data
public class NetPolicy {
    private Integer serviceId;
    private Integer servicePort;
    private String serviceProto;
}
