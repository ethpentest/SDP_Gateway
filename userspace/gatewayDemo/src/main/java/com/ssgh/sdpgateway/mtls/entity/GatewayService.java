package com.ssgh.sdpgateway.mtls.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName gateway_service
 */
@TableName(value ="gateway_service")
@Data
public class GatewayService implements Serializable {
    /**
     * 服务ID
     */
    @TableId
    private Integer gatewayServiceId;

    /**
     * 服务端口
     */
    private Integer gatewayServicePort;

    /**
     * 服务协议类型
     */
    private String gatewayServiceProto;

    /**
     * 服务描述
     */
    private String gatewayServiceDescription;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}