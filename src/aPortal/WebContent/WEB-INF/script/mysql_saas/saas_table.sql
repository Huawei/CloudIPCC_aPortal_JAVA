/*
Navicat MySQL Data Transfer

Source Server         : cloud
Source Server Version : 50155
Source Host           : 10.184.211.80:3306
Source Database       : mysql

Target Server Type    : MYSQL
Target Server Version : 50155
File Encoding         : 65001

Date: 2018-10-17 13:18:53
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_saas_bpo_accesscode
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_bpo_accesscode`;
CREATE TABLE `t_saas_bpo_accesscode` (
  `CC_ID` int(11) NOT NULL DEFAULT '0' COMMENT '呼叫中心ID',
  `ACCESSCODE` varchar(32) NOT NULL COMMENT '接入码',
  `MEDIA_TYPE` varchar(64) DEFAULT NULL COMMENT '接入码媒体类型',
  `STATUS` int(11) NOT NULL DEFAULT '0' COMMENT '''接入码使用状态:0-未用 1-锁定 2-已用',
  `TENANT_ID` varchar(64) NOT NULL COMMENT '租户ID',
  PRIMARY KEY(`ACCESSCODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_saas_bpo_called
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_bpo_called`;
CREATE TABLE `t_saas_bpo_called` (
  `CC_ID` int(11) NOT NULL,
  `VDN_ID` int(11) NOT NULL,
  `CALLED_NUMBER` varchar(24) NOT NULL COMMENT '被叫号码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_saas_bpo_callinfo
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_bpo_callinfo`;
CREATE TABLE `t_saas_bpo_callinfo` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `NODE_ID` varchar(64) NOT NULL COMMENT '节点ID',
  `CC_ID` int(11) NOT NULL COMMENT '呼叫中心ID',
  `VDN_ID` int(11) NOT NULL COMMENT 'VDN_ID',
  `CALLERNUM` varchar(32) DEFAULT NULL COMMENT '主叫号码',
  `CALLERINFO` varchar(128) DEFAULT '' COMMENT '号码描述',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_saas_bpo_skill
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_bpo_skill`;
CREATE TABLE `t_saas_bpo_skill` (
  `TENANT_ID` varchar(64) NOT NULL DEFAULT '' COMMENT '技能队列所属租户ID',
  `SKILL_ID` varchar(32) NOT NULL DEFAULT '' COMMENT '技能ID',
  `SKILL_NAME` varchar(128) DEFAULT NULL COMMENT '上层业务技能描述',
  `MEDIA_TYPE` varchar(64) DEFAULT NULL COMMENT '媒体类型',
  `CTI_SKILL_ID` varchar(32) DEFAULT NULL COMMENT '调用CTI接口生成的技能队列ID',
  `CTI_SKILL_NAME` varchar(128) DEFAULT NULL COMMENT '调用CTI接口生成的技能队列名称',
  `CREATE_TIME` datetime DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_saas_bpo_tenant
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_bpo_tenant`;
CREATE TABLE `t_saas_bpo_tenant` (
  `CC_ID` int(11) NOT NULL DEFAULT '-1' COMMENT '中心ID',
  `VDN_ID` int(11) DEFAULT NULL COMMENT 'VDN ID',
  `TENANT_ID` varchar(64) NOT NULL COMMENT '租户ID，多区域全局唯一',
  `TENANT_STATUS` varchar(32) DEFAULT NULL COMMENT '租户状态:INIT初始化 ACTIVE激活 PAUSE暂停 SUSPEND停机保号 CANCELLATION注销',
  `TENANT_NAME` varchar(128) DEFAULT NULL COMMENT '租户名称',
  `CREATE_TIME`  datetime NULL DEFAULT NULL ,
  `DELETE_TIME`  datetime NULL DEFAULT NULL ,
  `IS_TRIAL`  int(1) NOT NULL DEFAULT '0' COMMENT '是否试用，1表示试用' ,
  PRIMARY KEY (`TENANT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_saas_tenant_url
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_tenant_url`;
CREATE TABLE `t_saas_tenant_url` (
  `CC_ID` int(11) NOT NULL COMMENT '呼叫中心ID',
  `VDN_ID` varchar(200) NOT NULL COMMENT 'VDN ID',
  `URL_FLAG` varchar(32) NOT NULL COMMENT '每个激活或试用租户生成的唯一标识',
  PRIMARY KEY (`URL_FLAG`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_saas_vdn_agent_prop
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_vdn_agent_prop`;
CREATE TABLE `t_saas_vdn_agent_prop` (
  `ccId`  int(11) NOT NULL ,
  `vdnId`  int(11) NOT NULL ,
  `agentId`  int(11) NOT NULL ,
  `propType`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
  `propValue`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
   PRIMARY KEY (`ccId`, `vdnId`, `agentId`, `propType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_saas_vdn_clean_log
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_vdn_clean_log`;
CREATE TABLE `t_saas_vdn_clean_log` (
  `logdate` datetime DEFAULT NULL,
  `ccId` int(11) DEFAULT NULL,
  `vdnId` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_saas_vdn_ivr_menus
-- ----------------------------
DROP TABLE IF EXISTS `t_saas_vdn_ivr_menus`;
CREATE TABLE `t_saas_vdn_ivr_menus` (
  `menuId` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `ccId` int(11) DEFAULT NULL,
  `vdnId` int(11) DEFAULT NULL,
  `tenantId` varchar(128) DEFAULT NULL COMMENT '租户ID',
  `accessCode` varchar(32) NOT NULL COMMENT '接入码',
  `menuName` varchar(256) NOT NULL COMMENT '菜单名称',
  `beginTime` varchar(16) NOT NULL COMMENT '服务开始时间',
  `endTime` varchar(16) NOT NULL COMMENT '服务结束时间',
  `createTime` datetime DEFAULT NULL,
  `creator` varchar(255) DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `updator` varchar(255) DEFAULT NULL,
  `verification` int(1) DEFAULT NULL COMMENT '审核结果0：待审核；1：审核通过；2：审核不通过',
  `remark` varchar(256) DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (`menuId`),
  KEY `unionId` (`ccId`,`vdnId`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Procedure structure for P_SAAS_BPO_CLEAN_TENANT
-- ----------------------------
DROP PROCEDURE IF EXISTS `P_SAAS_BPO_CLEAN_TENANT`;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `P_SAAS_BPO_CLEAN_TENANT`(IN i_ccId int, IN i_vdnId int)
BEGIN
  DECLARE v_tenantId VARCHAR(64);
  SELECT TENANT_ID INTO v_tenantId FROM t_saas_bpo_tenant WHERE CC_ID = i_ccId AND VDN_ID = i_vdnId;
  DELETE FROM t_saas_bpo_tenant WHERE CC_ID = i_ccId AND VDN_ID = i_vdnId;
  DELETE FROM t_saas_bpo_accesscode WHERE CC_ID = i_ccId AND TENANT_ID = v_tenantId;
  DELETE FROM t_saas_bpo_called WHERE CC_ID = i_ccId AND VDN_ID = i_vdnId;
  DELETE FROM t_saas_bpo_callinfo WHERE CC_ID = i_ccId AND VDN_ID = i_vdnId;
  DELETE FROM t_saas_bpo_skill WHERE TENANT_ID = v_tenantId;
  DELETE FROM t_saas_vdn_ivr_menu_info WHERE CCID = i_ccId AND VDNID = i_vdnId;
  DELETE FROM t_saas_vdn_ivr_menus WHERE CCID = i_ccId AND VDNID = i_vdnId;
  DELETE FROM t_saas_vdn_ivr_voice_file WHERE CCID = i_ccId AND VDNID = i_vdnId;
  DELETE FROM t_saas_vdn_agent_prop WHERE CCID = i_ccId AND VDNID = i_vdnId;
  INSERT INTO t_saas_vdn_clean_log(logdate, ccId, vdnId, type, message) value (NOW(), i_ccId, i_vdnId, 1, 'clean the tenant information');
  COMMIT;
END
;;
DELIMITER ;
