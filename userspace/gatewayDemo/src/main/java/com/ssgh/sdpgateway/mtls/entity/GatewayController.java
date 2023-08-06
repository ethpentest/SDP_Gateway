package com.ssgh.sdpgateway.mtls.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName gateway_controller
 */
@TableName(value ="gateway_controller")
@Data
public class GatewayController implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer gatewayControllerId;

    /**
     * 控制器IP地址
     */
    private String gatewayControllerIp;

    /**
     * 是否为控制器1：是，0：不是
     */
    private Integer gatewayControllerFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}