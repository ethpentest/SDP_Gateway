package com.ssgh.sdpgateway.spa.servertoproxy;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ssgh.sdpgateway.mtls.entity.GatewayClient;
import com.ssgh.sdpgateway.mtls.service.GatewayClientService;
import com.ssgh.sdpgateway.spa.message.SPAMessage;
import com.ssgh.sdpgateway.spa.message.SPAResponse;
import com.ssgh.sdpgateway.utils.ByteUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


@Component
@Slf4j
@ChannelHandler.Sharable
public class SPAServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Resource
    GatewayClientService gatewayClientService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        byte[] data = new byte[160];
        packet.content().readBytes(data);
        SPAMessage spaMessage = ByteUtils.bytesToSPAMessage(data);
        log.info(spaMessage.toString());
        int clientIp = spaMessage.getClientIp();
        String IP = ByteUtils.intToIp(clientIp);
        String clientHOTP = spaMessage.getHotp();
        String clientHMAC = spaMessage.getHmac();
        QueryWrapper<GatewayClient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gateway_client_ip", IP);
        queryWrapper.eq("gateway_client_hotp", clientHOTP);
        queryWrapper.eq("gateway_client_hmac", clientHMAC);
        GatewayClient client = gatewayClientService.getOne(queryWrapper); // 查询该client的HOTP和HMAC是否在数据库中，存在：向client返回200.不存在返回300

        // 向客户端发送消息
        SPAResponse spaResponseToClient = new SPAResponse();
        spaResponseToClient.setAction("spa_response");
        if (client != null) {
            spaResponseToClient.setStatus("200");
        } else {
            spaResponseToClient.setStatus("300");
        }

        String messageToClient = JSONObject.toJSONString(spaResponseToClient);
        byte[] bytes = messageToClient.getBytes(StandardCharsets.UTF_8);
        DatagramPacket dataToClient = new DatagramPacket(Unpooled.copiedBuffer(bytes), packet.sender());
        ctx.writeAndFlush(dataToClient);
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
