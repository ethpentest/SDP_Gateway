package com.ssgh.sdpgateway.mtls.message.type;

import lombok.Data;

@Data
public class NetService {
    public Integer serviceId;
    public String serviceIp;
    public Integer servicePort;
//    public String serviceProto;
    public String serviceDescription;
}
