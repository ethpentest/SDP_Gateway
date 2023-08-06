package com.ssgh.sdpgateway.spa.clienttocontroller;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SPAClientChannelInitializer extends ChannelInitializer<NioDatagramChannel> {

    @Resource
    SPAClientHandler spaClientHandler;

    @Override
    protected void initChannel(NioDatagramChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(spaClientHandler);
    }
}
