/*

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `candidate`
-- ----------------------------
DROP TABLE IF EXISTS `candidate`;
CREATE TABLE `candidate` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT 'version',
  `service_id` varchar(36) DEFAULT NULL,
  `release_id` varchar(36) DEFAULT NULL COMMENT 'project_id',
  `image_id` varchar(36) DEFAULT NULL,
  `duration` int(255) DEFAULT NULL COMMENT '运行时长 单位秒 删除时才更新此字段',
  `pod_count` varchar(255) DEFAULT NULL COMMENT '预设pod总数',
  `match` varchar(2048) DEFAULT NULL COMMENT 'match中的东西，包括method，param，uri，head等都在这里写',
  `weight` int(32) DEFAULT NULL COMMENT '权重',
  `redirect` varchar(255) DEFAULT NULL COMMENT '转发uri',
  `rewrite` varchar(255) DEFAULT NULL COMMENT '重写uri',
  `timeout` int(255) DEFAULT NULL COMMENT '超时',
  `retries` int(32) DEFAULT NULL COMMENT '重试',
  `type` tinyint(64) DEFAULT NULL COMMENT '0 主节点 1 金丝雀节点 2 蓝绿节点 3 ABTEST节点',
  `status` tinyint(16) DEFAULT NULL COMMENT '0 启用 1 未启用',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(36) DEFAULT NULL COMMENT '创建人',
  `isdel` tinyint(1) DEFAULT '1' COMMENT '是否删除 0 否 1 是',
  `create_byid` varchar(36) DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for `certificate`
-- ----------------------------
DROP TABLE IF EXISTS `certificate`;
CREATE TABLE `certificate` (
  `id` varchar(36) NOT NULL,
  `service_group_id` varchar(36) DEFAULT NULL,
  `content` varchar(512) DEFAULT NULL COMMENT '内容（base64编码）',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(36) DEFAULT NULL COMMENT '创建人',
  `create_byid` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ----------------------------
-- Table structure for `channel_service_group`
-- ----------------------------
DROP TABLE IF EXISTS `channel_service_group`;
CREATE TABLE `channel_service_group` (
  `id` varchar(36) COLLATE utf8mb4_bin NOT NULL,
  `channel_id` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `servicegroup_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `channel_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `servicegroup_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- ----------------------------
-- Table structure for `cluster`
-- ----------------------------
DROP TABLE IF EXISTS `cluster`;
CREATE TABLE `cluster` (
  `Invite_code` varchar(100) DEFAULT NULL,
  `id` varchar(36) NOT NULL COMMENT '主键',
  `k8s_name` varchar(100) DEFAULT NULL COMMENT '集群名称',
  `k8s_kubeconfig` text COMMENT '集群配置文件',
  `isdel` tinyint(4) DEFAULT NULL COMMENT '删除标记',
  `create_by` varchar(36) DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `create_byid` varchar(36) DEFAULT NULL,
  `disable` varchar(10) DEFAULT NULL COMMENT '不可用标识（true），可用时为null',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `env`
-- ----------------------------
DROP TABLE IF EXISTS `env`;
CREATE TABLE `env` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `env_name` varchar(100) DEFAULT NULL COMMENT '环境名称',
  `cluster_id` varchar(36) DEFAULT NULL COMMENT '集群id',
  `namespace` varchar(40) DEFAULT NULL COMMENT '命名空间',
  `env_tag` varchar(10) DEFAULT NULL COMMENT 'test/dev/staging/prod',
  `audit` tinyint(4) DEFAULT NULL COMMENT '是否需要人工审核(才能部署到此环境',
  `cpu_request_unit` varchar(10) DEFAULT NULL COMMENT 'cpu限制',
  `cpu_request` int(11) DEFAULT NULL COMMENT 'cpu限制',
  `cpu_limit_unit` varchar(10) DEFAULT NULL COMMENT 'cpu限制',
  `cpu_limit` int(11) DEFAULT NULL COMMENT 'cpu限制',
  `mem_request_unit` varchar(10) DEFAULT NULL COMMENT '内存限制',
  `mem_request` int(11) DEFAULT NULL COMMENT '内存限制',
  `mem_limit_unit` varchar(10) DEFAULT NULL COMMENT '内存限制',
  `mem_limit` int(11) DEFAULT NULL COMMENT '内存限制',
  `resource_quota` tinyint(4) DEFAULT NULL COMMENT '流量限制',
  `network_limit` int(11) DEFAULT NULL COMMENT '流量限制',
  `visibility` tinyint(4) DEFAULT NULL COMMENT '10-public 20-private',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(200) DEFAULT NULL COMMENT '创建人',
  `modify_at` datetime DEFAULT NULL,
  `modify_by` varchar(36) DEFAULT NULL,
  `isdel` tinyint(4) DEFAULT NULL,
  `create_byid` varchar(36) DEFAULT NULL,
  `disable` varchar(10) DEFAULT NULL COMMENT '不可用标识（true），可用时为null',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `events`
-- ----------------------------
DROP TABLE IF EXISTS `events`;
CREATE TABLE `events` (
  `id` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL,
  `resource_uid` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL,
  `event_uid` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL,
  `reason` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL,
  `type` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL,
  `note` text COLLATE utf8mb4_bin,
  `kind` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `namespace` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `cluster_id` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for `heatmap`
-- ----------------------------
DROP TABLE IF EXISTS `heatmap`;
CREATE TABLE `heatmap` (
  `id` varchar(36) CHARACTER SET latin1 NOT NULL COMMENT '主键',
  `target_id` varchar(36) CHARACTER SET latin1 DEFAULT NULL COMMENT '热力对象id（目前都是service）',
  `num` int(10) DEFAULT NULL COMMENT '热力值',
  `date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for `image`
-- ----------------------------
DROP TABLE IF EXISTS `image`;
CREATE TABLE `image` (
  `id` varchar(36) NOT NULL,
  `service_id` varchar(36) DEFAULT NULL,
  `upload_source` varchar(50) DEFAULT NULL COMMENT '上传方标识',
  `image` varchar(255) DEFAULT NULL,
  `tag` varchar(50) DEFAULT NULL,
  `version` varchar(50) DEFAULT NULL COMMENT '默认与tag相同，平台中可以修改此字段',
  `refs` varchar(50) DEFAULT NULL,
  `commit_hash` varchar(255) DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `create_at` datetime DEFAULT NULL,
  `isdel` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for `message`
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` varchar(36) COLLATE utf8mb4_bin NOT NULL COMMENT '主键',
  `title` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '消息标题',
  `content` text COLLATE utf8mb4_bin COMMENT '消息内容',
  `create_at` datetime DEFAULT NULL COMMENT '消息推送时间',
  `type` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '消息类型',
  `target_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '消息目标对象id',
  `level` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '消息级别(NORMAL-运行级、WARN-潜在级、ERROR-报错级、FATAL-退出级)',
  `service_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '服务id',
  `operuser_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '操作人id',
  `service_group_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '服务组id',
  `message_type` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '消息标识 1-右上通知，2-服务通知',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for `message_state`
-- ----------------------------
DROP TABLE IF EXISTS `message_state`;
CREATE TABLE `message_state` (
  `id` varchar(36) COLLATE utf8mb4_bin NOT NULL COMMENT '主键',
  `message_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '消息id',
  `user_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '消息所属人id',
  `read_flag` tinyint(4) DEFAULT NULL COMMENT '是否已读标志(0-否，1-是)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


-- ----------------------------
-- Table structure for `operation`
-- ----------------------------
DROP TABLE IF EXISTS `operation`;
CREATE TABLE `operation` (
  `deploy_id` varchar(36) DEFAULT NULL,
  `target` varchar(20) DEFAULT NULL COMMENT '操作对象的类型 service / candidate / release / image / service_group 等',
  `target_id` varchar(36) DEFAULT NULL COMMENT '操作对象的id',
  `event_log` text COMMENT '操作内容，应详尽描述',
  `event_type` tinyint(4) DEFAULT NULL COMMENT '1 创建节点 2 删除节点 3 修改路由权重 4 修改匹配规则 5 修改策略 6 调整pod数量 (数量太多，待完善)',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(36) DEFAULT NULL COMMENT '创建人',
  `create_byid` varchar(36) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for `release`
-- ----------------------------
DROP TABLE IF EXISTS `release`;
CREATE TABLE `release` (
  `id` varchar(36) NOT NULL,
  `name` varchar(50) DEFAULT NULL COMMENT '冗余，一般情况下为候选人的版本号',
  `image_id` varchar(36) DEFAULT NULL,
  `service_id` varchar(36) DEFAULT NULL,
  `resource_type` varchar(10) DEFAULT NULL,
  `uid` varchar(36) DEFAULT NULL,
  `deploy_start_at` datetime DEFAULT NULL COMMENT '发布开始时间',
  `deploy_finish_at` datetime DEFAULT NULL COMMENT '发布完成时间',
  `service_start_at` datetime DEFAULT NULL COMMENT '服役开始时间',
  `service_finish_at` datetime DEFAULT NULL COMMENT '服役结束时间',
  `status` tinyint(4) DEFAULT NULL COMMENT '0 准备中 1 服役中 2 退役 3 异常',
  `type` tinyint(4) DEFAULT NULL COMMENT '1 金丝雀发布 2 蓝绿发布 4 ABTEST发布 8 滚动发布 每添加一种发布方式，此字段&上相应的值',
  `duration` int(64) DEFAULT NULL COMMENT '服役时长 单位秒 退役时才更新此字段 service_ finish_at减 service_start_at',
  `create_by` varchar(36) DEFAULT NULL,
  `create_byid` varchar(36) DEFAULT NULL,
  `oper_channel` varchar(20) DEFAULT NULL COMMENT '操作渠道：ladeit，slack',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for `role`
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` varchar(36) COLLATE utf8_unicode_ci NOT NULL COMMENT '角色所对应的访问级别',
  `role_type` varchar(20) CHARACTER SET latin1 DEFAULT NULL COMMENT '角色类型',
  `role_num` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '角色创建时间',
  `role_desc` varchar(30) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '角色描述信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for `service`
-- ----------------------------
DROP TABLE IF EXISTS `service`;
CREATE TABLE `service` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `service_group_id` varchar(36) DEFAULT NULL COMMENT '冗余',
  `cluster_id` varchar(36) DEFAULT NULL,
  `env_id` varchar(36) DEFAULT NULL,
  `image_version` varchar(50) DEFAULT NULL COMMENT '当前运行的镜像版本',
  `match` text COMMENT 'match json',
  `mapping` text COMMENT '匹配ip或者dns',
  `status` varchar(16) DEFAULT NULL COMMENT '状态 -1 尚未运行 0 正常运行 1 金丝雀发布中 2 蓝绿发布中 3 ABTEST发布中 4 滚动发布中 8 异常',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(36) DEFAULT NULL COMMENT '创建人',
  `modify_at` datetime DEFAULT NULL,
  `modify_by` varchar(36) DEFAULT NULL,
  `isdel` tinyint(4) DEFAULT NULL,
  `create_byid` varchar(36) DEFAULT NULL,
  `image_id` varchar(36) DEFAULT NULL,
  `token` varchar(100) DEFAULT NULL,
  `release_at` datetime DEFAULT NULL,
  `service_type` varchar(5) DEFAULT NULL COMMENT '1-k8s,2-istio',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for `service_group`
-- ----------------------------
DROP TABLE IF EXISTS `service_group`;
CREATE TABLE `service_group` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `env_id` varchar(36) DEFAULT NULL,
  `cluster_id` varchar(36) DEFAULT NULL,
  `gateway` varchar(255) DEFAULT NULL,
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(36) DEFAULT NULL COMMENT '创建人',
  `modify_at` datetime DEFAULT NULL,
  `modify_by` varchar(36) DEFAULT NULL,
  `isdel` tinyint(4) DEFAULT NULL,
  `create_byid` varchar(36) DEFAULT NULL,
  `Invite_code` varchar(100) DEFAULT NULL COMMENT '邀请码',
  `isdel_service` tinyint(4) DEFAULT NULL COMMENT '是否同时删除服务',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for `service_publish_bot`
-- ----------------------------
DROP TABLE IF EXISTS `service_publish_bot`;
CREATE TABLE `service_publish_bot` (
  `id` varchar(36) COLLATE utf8mb4_bin NOT NULL,
  `service_group_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `service_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `oper_type` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '发布方案（0-关闭、1-需人工审核、2-全自动）',
  `examine_type` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '审核方式（1-slack、2-飞书、0-站内）',
  `publish_type` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '发布方式（滚动发布、蓝绿发布...）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` varchar(36) COLLATE utf8_unicode_ci NOT NULL COMMENT '主键',
  `nick_name` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '用户昵称',
  `last_name` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `username` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '用户名',
  `email` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '邮箱地址',
  `password` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '用户密码',
  `salt` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '密码盐',
  `create_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '成员创建时间',
  `update_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '成员信息修改时间',
  `status` tinyint(1) DEFAULT '0' COMMENT '用户是否已激活  0：未激活 1：已激活',
  `isdel` tinyint(1) DEFAULT '1' COMMENT '用户是否删除 0 否 1 是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (UUID(), NULL, NULL, 'ladeit-bot', NULL, '1ed919b4fe39fb389562eb753560350f8b3fbb7853ed2c55f990db408273a2c4', '12345678901234567890123456789012', SYSDATE(), NULL, 0, 0);
INSERT INTO `user` VALUES (UUID(), NULL, NULL, 'admin', NULL, '1ed919b4fe39fb389562eb753560350f8b3fbb7853ed2c55f990db408273a2c4', '12345678901234567890123456789012', SYSDATE(), NULL, NULL, 0);

-- ----------------------------
-- Table structure for `user_cluster_relation`
-- ----------------------------
DROP TABLE IF EXISTS `user_cluster_relation`;
CREATE TABLE `user_cluster_relation` (
  `id` varchar(36) COLLATE utf8mb4_bin NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `cluster_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `access_level` varchar(5) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'RWX',
  `create_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for `user_env_relation`
-- ----------------------------
DROP TABLE IF EXISTS `user_env_relation`;
CREATE TABLE `user_env_relation` (
  `id` varchar(36) COLLATE utf8mb4_bin NOT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `cluster_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `env_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `access_level` varchar(5) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'RWX',
  `create_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for `user_service_group_relation`
-- ----------------------------
DROP TABLE IF EXISTS `user_service_group_relation`;
CREATE TABLE `user_service_group_relation` (
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `service_group_id` varchar(36) DEFAULT NULL,
  `access_level` varchar(5) DEFAULT NULL COMMENT 'RWX',
  `create_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ----------------------------
-- Table structure for `user_service_relation`
-- ----------------------------
DROP TABLE IF EXISTS `user_service_relation`;
CREATE TABLE `user_service_relation` (
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) DEFAULT NULL,
  `service_id` varchar(36) DEFAULT NULL,
  `access_level` varchar(5) DEFAULT NULL COMMENT 'RWX',
  `role_num` varchar(5) DEFAULT NULL COMMENT 'RWX',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ----------------------------
-- Table structure for `user_slack_relation`
-- ----------------------------
DROP TABLE IF EXISTS `user_slack_relation`;
CREATE TABLE `user_slack_relation` (
  `id` varchar(36) COLLATE utf8mb4_bin NOT NULL,
  `user_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `user_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `slack_user_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `slack_user_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;



-- ----------------------------
-- Table structure for `webhook`
-- ----------------------------
DROP TABLE IF EXISTS `webhook`;
CREATE TABLE `webhook` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `hook_uri` varchar(1024) DEFAULT NULL COMMENT 'hook路径',
  `method` varchar(32) DEFAULT NULL COMMENT 'GET POST PUT DELETE',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(36) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of webhook
-- ----------------------------

-- ----------------------------
-- Table structure for `yaml`
-- ----------------------------
DROP TABLE IF EXISTS `yaml`;
CREATE TABLE `yaml` (
  `id` varchar(36) COLLATE utf8mb4_bin NOT NULL,
  `service_group_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `service_id` varchar(36) COLLATE utf8mb4_bin DEFAULT NULL,
  `content` varchar(10000) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `type` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of yaml
-- ----------------------------
