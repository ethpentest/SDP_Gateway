package com.ssgh.sdpgateway.spa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "spa")
@Data
@Configuration
public class SPAProperties {
    /**
     * 控制器 SPA IP地址
     */
    private String controllerIP;
    /**
     * 网关 SPA IP地址
     */
    private String gatewayIP;
    /**
     * 控制器 SPA 端口号
     */
    private Integer controllerPort;
    /**
     * 网关 SPA 端口
     */
    private Integer gatewayPort;
    /**
     * 消息长度字段的所占字节
     */
    private int lenFieldLength = 4;
}
