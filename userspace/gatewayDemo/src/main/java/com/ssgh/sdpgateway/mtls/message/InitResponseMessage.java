package com.ssgh.sdpgateway.mtls.message;

import lombok.Data;

@Data
public class InitResponseMessage {
    public String action;
    public InitResponseMessageData data;
}
