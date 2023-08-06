package com.ssgh.sdpgateway.mtls.service;

import com.ssgh.sdpgateway.mtls.entity.GatewayClient;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ssgh.sdpgateway.mtls.message.type.NetClient;

/**
* @author UpUpS
* @description 针对表【gateway_client】的数据库操作Service
* @createDate 2023-06-08 20:55:15
*/
public interface GatewayClientService extends IService<GatewayClient> {

    GatewayClient toGatewayClient(NetClient netClient);
}
