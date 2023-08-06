package com.ssgh.sdpgateway.mtls.message;

import lombok.Data;

@Data
public class ResponseToProxy {
    String action;
    String status;
}
