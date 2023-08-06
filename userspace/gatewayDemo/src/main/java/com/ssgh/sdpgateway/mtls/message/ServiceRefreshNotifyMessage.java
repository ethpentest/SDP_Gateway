package com.ssgh.sdpgateway.mtls.message;

import com.ssgh.sdpgateway.mtls.message.type.NetService;
import lombok.Data;

import java.util.List;

@Data
public class ServiceRefreshNotifyMessage {
    public String action;
    public ServiceData data;
}
