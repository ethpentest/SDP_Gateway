package com.ssgh.sdpgateway.mtls.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ssgh.sdpgateway.mtls.entity.GatewayPolicy;
import com.ssgh.sdpgateway.mtls.message.type.NetPolicy;
import com.ssgh.sdpgateway.mtls.service.GatewayPolicyService;
import com.ssgh.sdpgateway.mtls.mapper.GatewayPolicyMapper;
import org.springframework.stereotype.Service;

/**
* @author UpUpS
* @description 针对表【gateway_policy】的数据库操作Service实现
* @createDate 2023-06-08 21:00:10
*/
@Service
public class GatewayPolicyServiceImpl extends ServiceImpl<GatewayPolicyMapper, GatewayPolicy>
    implements GatewayPolicyService{

    @Override
    public GatewayPolicy toGatewayPolicy(NetPolicy netPolicy, String clientIp) {
        GatewayPolicy gatewayPolicy = new GatewayPolicy();

        gatewayPolicy.setPolicyClientIp(clientIp);
        gatewayPolicy.setPolicyServicePort(netPolicy.getServicePort());
        gatewayPolicy.setPolicyFlag(1);

        return gatewayPolicy;
    }
}




