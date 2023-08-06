package com.ssgh.sdpgateway.mtls.coder;

import com.alibaba.fastjson2.JSONObject;
import com.ssgh.sdpgateway.mtls.config.MtlsProperties;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class JSONEncoder extends MessageToByteEncoder<Object> {
    @Resource
    MtlsProperties mtlsProperties;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) throws Exception {
        byte[] data = JSONObject.toJSONString(msg).getBytes();
//        byte[] lenFiled = new byte[mtlsProperties.getLenFieldLength()];
        // 得到数据的长度
        int len = data.length;
        out.writeInt(len);
        out.writeBytes(data);
    }
}
