package com.ssgh.sdpgateway.spa.servertoproxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SPAServerChannelInitializer extends ChannelInitializer<NioDatagramChannel> {

    @Resource
    SPAServerHandler spaServerHandler;

    @Override
    protected void initChannel(NioDatagramChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(spaServerHandler);
    }
}
