package com.ssgh.sdpgateway.mtls.service;

import com.ssgh.sdpgateway.mtls.entity.GatewayPolicy;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ssgh.sdpgateway.mtls.message.type.NetPolicy;

/**
* @author UpUpS
* @description 针对表【gateway_policy】的数据库操作Service
* @createDate 2023-06-08 21:00:10
*/
public interface GatewayPolicyService extends IService<GatewayPolicy> {
    GatewayPolicy toGatewayPolicy(NetPolicy netPolicy, String clientIp);
}
