package com.ssgh.sdpgateway.mtls.coder;

import com.ssgh.sdpgateway.mtls.config.MtlsProperties;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@ChannelHandler.Sharable
public class JSONDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Resource
    MtlsProperties mtlsProperties;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf msg, List<Object> list) throws Exception {
        int len = msg.readableBytes();//可读取数据长度
        byte[] data = new byte[len - mtlsProperties.getLenFieldLength()];
        msg.getBytes(msg.readerIndex() + mtlsProperties.getLenFieldLength(), data, 0, len - mtlsProperties.getLenFieldLength());

        String message = new String(data);
        list.add(message);
    }
}
