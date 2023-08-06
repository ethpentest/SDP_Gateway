package com.ssgh.sdpgateway.mtls.servertoproxy;

import com.ssgh.sdpgateway.mtls.config.MtlsProperties;
import io.netty.handler.ssl.ClientAuth;
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
public class MtlsServerSslContext {
    @Resource
    MtlsProperties mtlsProperties;
    public SslContext getSslContext() throws IOException {

        // 获取网关证书
        ClassPathResource controllerCrtResource = new ClassPathResource("key/gateway/gateway.crt"); 
        InputStream controllerCrtChainInputStream = controllerCrtResource.getInputStream();
        // 获取网关密钥
        ClassPathResource controllerKeyResource = new ClassPathResource("key/gateway/gateway_pkcs8.key"); 
        InputStream controllerKeyInputStream = controllerKeyResource.getInputStream();
        // 获取ca证书
        ClassPathResource caCrtResource = new ClassPathResource("key/ca/ca.crt");
        InputStream caCrtInputStream = caCrtResource.getInputStream();

        return SslContextBuilder
                .forServer(controllerCrtChainInputStream, controllerKeyInputStream)
                .trustManager(caCrtInputStream)
                .clientAuth(ClientAuth.REQUIRE)
                .build();
    }
}
