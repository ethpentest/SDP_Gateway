package com.ssgh.sdpgateway.mtls.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ssgh.sdpgateway.mtls.entity.GatewayService;
import com.ssgh.sdpgateway.mtls.message.type.NetService;
import com.ssgh.sdpgateway.mtls.service.GatewayServiceService;
import com.ssgh.sdpgateway.mtls.mapper.GatewayServiceMapper;
import org.springframework.stereotype.Service;

/**
* @author UpUpS
* @description 针对表【gateway_service】的数据库操作Service实现
* @createDate 2023-06-08 21:02:43
*/
@Service
public class GatewayServiceServiceImpl extends ServiceImpl<GatewayServiceMapper, GatewayService>
    implements GatewayServiceService{

    @Override
    public GatewayService toGatewayService(NetService netService) {
        GatewayService gatewayService = new GatewayService();
        gatewayService.setGatewayServiceId(netService.getServiceId());
        gatewayService.setGatewayServicePort(netService.getServicePort());
       gatewayService.setGatewayServiceProto("");
        gatewayService.setGatewayServiceDescription(netService.getServiceDescription());

        return gatewayService;
    }
}




