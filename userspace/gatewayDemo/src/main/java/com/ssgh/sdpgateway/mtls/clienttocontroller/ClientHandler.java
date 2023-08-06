package com.ssgh.sdpgateway.mtls.clienttocontroller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ssgh.sdpgateway.mtls.config.MtlsProperties;
import com.ssgh.sdpgateway.mtls.entity.GatewayClient;
import com.ssgh.sdpgateway.mtls.entity.GatewayPolicy;
import com.ssgh.sdpgateway.mtls.entity.GatewayService;
import com.ssgh.sdpgateway.mtls.message.*;
import com.ssgh.sdpgateway.mtls.message.type.NetClient;
import com.ssgh.sdpgateway.mtls.message.type.NetPolicy;
import com.ssgh.sdpgateway.mtls.message.type.NetService;
import com.ssgh.sdpgateway.mtls.service.GatewayClientService;
import com.ssgh.sdpgateway.mtls.service.GatewayPolicyService;
import com.ssgh.sdpgateway.mtls.service.GatewayServiceService;

import com.ssgh.sdpgateway.utils.ByteUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Resource
    GatewayServiceService gatewayServiceService;
    @Resource
    GatewayClientService gatewayClientService;
    @Resource
    GatewayPolicyService gatewayPolicyService;
    @Resource
    MtlsProperties mtlsProperties;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        log.info("本客户端连接到服务端。channelId：" + channel.id());
        log.info("服务端IP: " + channel.localAddress().getHostString() + "服务端Port: " + channel.localAddress().getPort());
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("断开连接 " + ctx.channel().localAddress().toString());
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object in) throws Exception {
        //接收消息
        String message = (String) in;
        log.info(message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        String action = jsonObject.getString("action");
        // SDP 控制器回复网关的初始化响应
        if (action != null && action.equals("gateway_init_response")) {

            InitResponseMessage initResponseMessage = JSON.parseObject(message, InitResponseMessage.class);
            InitResponseMessageData messageData = initResponseMessage.getData();

            // 获取gateway自身的hmac 和 hotp
            String gatewayHotp = messageData.getHotp();// Base64 hotp
            String gatewayHmac = messageData.getHmac();// Base64 hmac
            NetClient gateway = new NetClient(); // 用来存储网关自身的Hotp和hmac
            gateway.setClientHOTP(gatewayHotp);
            gateway.setClientHMAC(gatewayHmac);
            gateway.setClientIp(mtlsProperties.getGatewayIP());

            GatewayClient gatewayClient = gatewayClientService.toGatewayClient(gateway); // 保存网关自身Hotp和hmac到数据库中
            // 将 gateway 自己的hmac和hotp保存在数据库中
            boolean saveGateway = gatewayClientService.saveOrUpdate(gatewayClient);

            List<NetService> netServiceList = messageData.getServiceList();
            List<NetClient> netClientList = messageData.getClientAccessible();
            
            // 控制器下发的访问控制策略保存到数据库中
            updateAccess(netClientList);
            // 控制器下发的服务列表保存到数据库中
            updateService(netServiceList);
        }

        // SDP 控制器回复网关服务刷新的 响应
        if (action != null && action.equals("service_refresh_response")) {
            ServiceRefreshResponseMessage serviceRefreshResponseMessage = JSON.parseObject(message, ServiceRefreshResponseMessage.class);
            ServiceData data = serviceRefreshResponseMessage.getData();
            List<NetService> netServiceList = data.getServiceRefreshData();
            updateService(netServiceList);
        }
        // SDP 控制器回复网关访问控制策略刷新的 响应
        if (action != null && action.equals("access_refresh_response")) {
            AccessRefreshResponseMessage accessRefreshResponseMessage = JSON.parseObject(message, AccessRefreshResponseMessage.class);
            AccessData data = accessRefreshResponseMessage.getData();
            List<NetClient> netClientList = data.getAccessRefreshData();
            // 控制器下发的访问控制策略不为空
            updateAccess(netClientList);
        }

        // SDP 控制器回复网关服务推送的 响应
        if (action != null && action.equals("service_refresh_notify")) {
            ServiceRefreshNotifyMessage serviceRefreshNotifyMessage = JSON.parseObject(message, ServiceRefreshNotifyMessage.class);
            ServiceData data = serviceRefreshNotifyMessage.getData();
            List<NetService> netServiceList = data.getServiceRefreshData();
            updateService(netServiceList);
        }

        // SDP 控制器回复网关访问控制策略策略推送的响应
        if (action != null && action.equals("access_refresh_notify")) {
            AccessRefreshNotifyMessage accessRefreshNotifyMessage = JSON.parseObject(message, AccessRefreshNotifyMessage.class);
            AccessData data = accessRefreshNotifyMessage.getData();
            List<NetClient> netClientList = data.getAccessRefreshData();
            // 控制器下发的访问控制策略不为空
            updateAccess(netClientList);
        }

        // SDP 控制器向网关推送攻击者 的响应
        if (action != null && action.equals("attacker_refresh_notify")) {
            // todo:
        }
    }
    public void updateAccess(List<NetClient> netClientList) {
        // 控制器下发的访问控制策略不为空
        if (!netClientList.isEmpty()) {
            for (NetClient netClient : netClientList) {
                String clientIp = netClient.getClientIp();
                boolean validIPAddress = ByteUtils.isValidIPAddress(clientIp);
                if (!validIPAddress) {
                    continue;
                }
                List<NetPolicy> netPolicyList = netClient.getAccessibleList();
                NetPolicy clientMtlsPolicy = new NetPolicy();
                clientMtlsPolicy.setServicePort(mtlsProperties.getGatewayPort());
                netPolicyList.add(clientMtlsPolicy);
                // 客户端代理可以访问的服务列表--》数据库
                if (!netPolicyList.isEmpty()) {
                    for (NetPolicy netPolicy : netPolicyList) {
                        GatewayPolicy policy = gatewayPolicyService.toGatewayPolicy(netPolicy, clientIp);
                        QueryWrapper<GatewayPolicy> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("policy_client_ip", policy.getPolicyClientIp());
                        queryWrapper.eq("policy_service_port", policy.getPolicyServicePort());
                        GatewayPolicy one = gatewayPolicyService.getOne(queryWrapper);
                        if (one == null) {
                            gatewayPolicyService.save(policy);
                        }
                    }
                }
                // 客户端代理的hotp和hmac--》数据库
                if (!StringUtils.isAnyBlank(netClient.getClientHOTP(), netClient.getClientHMAC(), netClient.getClientIp())) {
                    GatewayClient client = gatewayClientService.toGatewayClient(netClient);
                    gatewayClientService.saveOrUpdate(client);
                }
            }
        }
    }
    public void updateService(List<NetService> netServiceList) {
        if (!netServiceList.isEmpty()) {
            List<GatewayService> gatewayServiceList = netServiceList.stream().map(netService -> gatewayServiceService.toGatewayService(netService)).collect(Collectors.toList());
            gatewayServiceService.saveOrUpdateBatch(gatewayServiceList);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.error("异常信息" + cause.getMessage());
    }
}
