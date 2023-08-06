package com.ssgh.sdpgateway.mtls.message;

import com.ssgh.sdpgateway.mtls.message.type.NetClient;
import lombok.Data;

import java.util.List;

@Data
public class AccessRefreshNotifyMessage {
    public String action;
    public AccessData data;
}
