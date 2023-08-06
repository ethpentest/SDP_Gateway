package com.ssgh.sdpgateway.spa.clienttocontroller;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@ChannelHandler.Sharable
public class SPAClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("sdp client channel is ready");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String msg = packet.content().toString(StandardCharsets.UTF_8);
        log.info("来自服务器的消息: " + msg);
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
