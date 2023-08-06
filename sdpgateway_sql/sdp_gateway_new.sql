/*
 Navicat Premium Data Transfer

 Source Server         : sgh
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : localhost:3306
 Source Schema         : sdp_gateway_new

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 19/06/2023 14:57:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gateway_attacker
-- ----------------------------
DROP TABLE IF EXISTS `gateway_attacker`;
CREATE TABLE `gateway_attacker`  (
  `gateway_attacker_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '攻击者IP地址',
  `gateway_attacker_flag` int NOT NULL DEFAULT 1 COMMENT '1：是攻击者，0：不是攻击者',
  PRIMARY KEY (`gateway_attacker_ip`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gateway_client
-- ----------------------------
DROP TABLE IF EXISTS `gateway_client`;
CREATE TABLE `gateway_client`  (
  `gateway_client_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理IP',
  `gateway_client_hmac` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理hmac04\n',
  `gateway_client_hotp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代理hotp01',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`gateway_client_ip`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gateway_controller
-- ----------------------------
DROP TABLE IF EXISTS `gateway_controller`;
CREATE TABLE `gateway_controller`  (
  `gateway_controller_id` int NOT NULL AUTO_INCREMENT,
  `gateway_controller_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '控制器IP地址',
  `gateway_controller_flag` int NOT NULL DEFAULT 1 COMMENT '是否为控制器1：是，0：不是',
  PRIMARY KEY (`gateway_controller_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gateway_policy
-- ----------------------------
DROP TABLE IF EXISTS `gateway_policy`;
CREATE TABLE `gateway_policy`  (
  `policy_client_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '客户端IP地址',
  `policy_service_port` int NOT NULL COMMENT '服务的端口号',
  `policy_flag` int NOT NULL DEFAULT 0 COMMENT '指定IP的客户端是否可以访问指定的端口服务，0：不可以，1：可以',
  PRIMARY KEY (`policy_client_ip`, `policy_service_port`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gateway_service
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service`;
CREATE TABLE `gateway_service`  (
  `gateway_service_id` int NOT NULL COMMENT '服务ID',
  `gateway_service_port` int NOT NULL COMMENT '服务端口',
  `gateway_service_proto` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务协议类型',
  `gateway_service_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务描述',
  PRIMARY KEY (`gateway_service_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
