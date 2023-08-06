package com.ssgh.sdpgateway.mtls.clienttocontroller;

import com.ssgh.sdpgateway.mtls.coder.JSONDecoder;
import com.ssgh.sdpgateway.mtls.coder.JSONEncoder;
import com.ssgh.sdpgateway.mtls.config.MtlsProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Resource
    MtlsClientSslContext mtlsClientSslContext;
    @Resource
    MtlsProperties mtlsProperties;
    @Resource
    ClientHandler clientHandler;
    @Resource
    JSONDecoder jsonDecoder;
    @Resource
    JSONEncoder jsonEncoder;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(mtlsClientSslContext.getSslContext().newHandler(channel.alloc()));
        channel.pipeline().addLast("lengthFieldDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, mtlsProperties.getLenFieldLength()));
        channel.pipeline().addLast("jsonDecoder", jsonDecoder);
        channel.pipeline().addLast("jsonEncoder", jsonEncoder);
        channel.pipeline().addLast(clientHandler);
    }
}
