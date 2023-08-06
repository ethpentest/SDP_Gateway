package com.ssgh.sdpgateway.mtls.clienttocontroller;

import com.ssgh.sdpgateway.mtls.config.MtlsProperties;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class MtlsClientSslContext {

    public SslContext getSslContext() throws IOException {
        // 获取网关证书
        ClassPathResource gatewayCrtResource = new ClassPathResource("key/gateway/gateway.crt");
        InputStream gatewayCrtChainInputStream = gatewayCrtResource.getInputStream();
        // 获取网关密钥
        ClassPathResource gatewayKeyResource = new ClassPathResource("key/gateway/gateway_pkcs8.key");
        InputStream gatewayKeyInputStream = gatewayKeyResource.getInputStream();
        // 获取ca证书
        ClassPathResource caCrtResource = new ClassPathResource("key/ca/ca.crt");
        InputStream caCrtInputStream = caCrtResource.getInputStream();

        return SslContextBuilder.forClient()
                .keyManager(gatewayCrtChainInputStream, gatewayKeyInputStream)
                .trustManager(caCrtInputStream)
                .build();
    }
}
