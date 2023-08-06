package com.ssgh.sdpgateway.mtls.servertoproxy;

import com.ssgh.sdpgateway.mtls.coder.JSONDecoder;
import com.ssgh.sdpgateway.mtls.coder.JSONEncoder;
import com.ssgh.sdpgateway.mtls.config.MtlsProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Resource
    MtlsServerSslContext mtlsServerSslContext;
    @Resource
    MtlsProperties mtlsProperties;
    @Resource
    ServerHandler serverHandler;
    @Resource
    JSONEncoder jsonEncoder;
    @Resource
    JSONDecoder jsonDecoder;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //添加SSL安装验证
        channel.pipeline().addLast(mtlsServerSslContext.getSslContext().newHandler(channel.alloc()));
        channel.pipeline().addLast("lengthFieldDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, mtlsProperties.getLenFieldLength()));
        channel.pipeline().addLast("jsonDecoder", jsonDecoder);
        channel.pipeline().addLast("jsonEncoder", jsonEncoder);
        channel.pipeline().addLast(serverHandler);
    }
}
