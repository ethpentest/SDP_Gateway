package com.ssgh.sdpgateway.mtls.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName gateway_policy
 */
@TableName(value ="gateway_policy")
@Data
public class GatewayPolicy implements Serializable {
    /**
     * 客户端IP地址
     */
    @TableId
    private String policyClientIp;

    /**
     * 服务的端口号
     */
    @TableId
    private Integer policyServicePort;

    /**
     * 指定IP的客户端是否可以访问指定的端口服务，0：不可以，1：可以
     */
    private Integer policyFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}