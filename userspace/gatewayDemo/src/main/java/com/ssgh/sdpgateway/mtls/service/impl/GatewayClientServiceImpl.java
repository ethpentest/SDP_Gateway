package com.ssgh.sdpgateway.mtls.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ssgh.sdpgateway.mtls.entity.GatewayClient;
import com.ssgh.sdpgateway.mtls.mapper.GatewayClientMapper;
import com.ssgh.sdpgateway.mtls.message.type.NetClient;
import com.ssgh.sdpgateway.mtls.service.GatewayClientService;
import org.springframework.stereotype.Service;

/**
* @author UpUpS
* @description 针对表【gateway_client】的数据库操作Service实现
* @createDate 2023-06-08 20:55:15
*/
@Service
public class GatewayClientServiceImpl extends ServiceImpl<GatewayClientMapper, GatewayClient>
    implements GatewayClientService{

    @Override
    public GatewayClient toGatewayClient(NetClient netClient) {
        GatewayClient gatewayClient = new GatewayClient();

        gatewayClient.setGatewayClientIp(netClient.getClientIp());

        gatewayClient.setGatewayClientHmac(netClient.getClientHMAC());
        gatewayClient.setGatewayClientHotp(netClient.getClientHOTP());

        return gatewayClient;
    }
}




