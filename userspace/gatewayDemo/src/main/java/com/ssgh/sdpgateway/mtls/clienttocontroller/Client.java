package com.ssgh.sdpgateway.mtls.clienttocontroller;

import com.ssgh.sdpgateway.mtls.config.MtlsProperties;
import com.ssgh.sdpgateway.mtls.message.RequestMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Client {
    @Resource
    MtlsProperties mtlsProperties;
    @Resource
    ClientChannelInitializer clientChannelInitializer;
    public void connect() throws SSLException {

        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.AUTO_READ, true)
                    .handler(clientChannelInitializer);

            ChannelFuture future = bootstrap.connect(mtlsProperties.getControllerIP(), mtlsProperties.getControllerPort()).sync();
            log.info("gateway start done!");
            RequestMessage message = new RequestMessage();
            message.setAction("gateway_init_request");
            future.channel().writeAndFlush(message);
            TimeUnit.MINUTES.sleep(5);
            while (true) {
                message.setAction("service_refresh_request");
                future.channel().writeAndFlush(message);
                TimeUnit.MINUTES.sleep(10);
                message.setAction("access_refresh_request");
                future.channel().writeAndFlush(message);
                TimeUnit.MINUTES.sleep(10);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
