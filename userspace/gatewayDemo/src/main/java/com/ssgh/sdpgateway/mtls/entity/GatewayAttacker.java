package com.ssgh.sdpgateway.mtls.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName gateway_attacker
 */
@TableName(value ="gateway_attacker")
@Data
public class GatewayAttacker implements Serializable {
    /**
     * 攻击者IP地址
     */
    @TableId
    private String gatewayAttackerIp;

    /**
     * 1：是攻击者，0：不是攻击者
     */
    private Integer gatewayAttackerFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}