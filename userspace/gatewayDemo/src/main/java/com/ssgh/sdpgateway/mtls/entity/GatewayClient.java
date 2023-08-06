package com.ssgh.sdpgateway.mtls.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName gateway_client
 */
@TableName(value ="gateway_client")
@Data
public class GatewayClient implements Serializable {
    /**
     * 代理IP
     */
    @TableId
    private String gatewayClientIp;

    /**
     * 代理hmac04

     */
    private String gatewayClientHmac;

    /**
     * 代理hotp01
     */
    private String gatewayClientHotp;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}