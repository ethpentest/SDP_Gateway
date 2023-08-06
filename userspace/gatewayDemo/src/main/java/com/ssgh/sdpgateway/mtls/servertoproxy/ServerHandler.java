package com.ssgh.sdpgateway.mtls.servertoproxy;

import com.ssgh.sdpgateway.mtls.message.ResponseToProxy;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        log.info("客户端连接信息：有一条客户端连接到服务端");
        log.info("客户端 IP : " + channel.localAddress().getHostString());
        log.info("客户端 port : " + channel.localAddress().getPort());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端断开连接" + ctx.channel().localAddress().toString());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object in) throws Exception {
        //接收消息
        ResponseToProxy responseToProxy = new ResponseToProxy();
        responseToProxy.setAction("MTLS_response");
        responseToProxy.setStatus("200");
        ctx.writeAndFlush(responseToProxy);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("异常信息 : " + cause.getMessage());
        ctx.close();
    }
}
