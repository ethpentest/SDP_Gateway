package com.ssgh.sdpgateway.spa.message;

import lombok.Data;

@Data
public class SPAMessage {
    int clientIp;
    long timeStamp;
    int randomNum;
    int messageType;
    long defaultValue;
    int clientId;
    String userId; // 256 位
    String deviceId; // 256 位
    String hotp; // 256 位
    String hmac; // 256 位
}
