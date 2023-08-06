package com.ssgh.sdpgateway.spa.servertoproxy;

import com.ssgh.sdpgateway.spa.config.SPAProperties;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class SPAServer {
    @Resource
    SPAProperties spaProperties;
    @Resource
    SPAServerChannelInitializer spaServerChannelInitializer;
    public void start() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)    //广播
                    .option(ChannelOption.SO_RCVBUF, 2048 * 1024)// 设置UDP读缓冲区为2M
                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024)// 设置UDP写缓冲区为1M
                    .handler(spaServerChannelInitializer);

            ChannelFuture future = bootstrap.bind(spaProperties.getGatewayPort() + 1).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
