package com.ssgh.sdpgateway.spa.clienttocontroller;

import com.ssgh.sdpgateway.spa.config.SPAProperties;
import com.ssgh.sdpgateway.spa.message.SPAMessage;
import com.ssgh.sdpgateway.utils.ByteUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Random;

@Component
@Slf4j
public class SPAClient {
    @Resource
    SPAProperties spaProperties;
    @Resource
    SPAClientChannelInitializer spaClientChannelInitializer;

    public void connect() {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(spaClientChannelInitializer);

            Channel channel = bootstrap.bind(spaProperties.getGatewayPort()).sync().channel();

            Random random = new Random(System.currentTimeMillis());
            int randomNum = random.nextInt();
            SPAMessage spaMessage = new SPAMessage();
            spaMessage.setClientIp(ByteUtils.ip2Int(spaProperties.getGatewayIP()));
            spaMessage.setTimeStamp(System.currentTimeMillis());
            spaMessage.setRandomNum(randomNum);
            spaMessage.setMessageType(0);
            spaMessage.setClientId(3);
            spaMessage.setDefaultValue(0L);
            spaMessage.setUserId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            spaMessage.setDeviceId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            spaMessage.setHotp("                                ");
            spaMessage.setHmac("                                ");

            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(ByteUtils.spaMessageToBytes(spaMessage)), new InetSocketAddress(spaProperties.getControllerIP(), spaProperties.getControllerPort()))).sync();
            channel.closeFuture().await();

        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
