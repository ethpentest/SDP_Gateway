package com.ssgh.sdpgateway.mtls.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "mtls")
@Data
@Configuration
public class MtlsProperties {

    /**
     * 控制器 MTLS IP地址
     */
    private String controllerIP;
    /**
     * 控制器 MTLS IP地址
     */
    private String gatewayIP;
    /**
     * 控制器 MTLS 端口号
     */
    private Integer controllerPort;
    /**
     * 网关 MTLS 端口
     */
    private Integer gatewayPort;
    /**
     * 消息长度字段的所占字节
     */
    private int lenFieldLength = 4;
}
