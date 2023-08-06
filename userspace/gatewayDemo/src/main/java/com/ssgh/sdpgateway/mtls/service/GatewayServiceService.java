package com.ssgh.sdpgateway.mtls.service;

import com.ssgh.sdpgateway.mtls.entity.GatewayService;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ssgh.sdpgateway.mtls.message.type.NetService;

/**
* @author UpUpS
* @description 针对表【gateway_service】的数据库操作Service
* @createDate 2023-06-08 21:02:43
*/
public interface GatewayServiceService extends IService<GatewayService> {
    GatewayService toGatewayService(NetService netService);

}
