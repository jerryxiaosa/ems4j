CREATE TABLE IF NOT EXISTS `sys_user_third_party_bind`
(
    `id`                       INT UNSIGNED                             NOT NULL AUTO_INCREMENT,
    `user_id`                  INT UNSIGNED                             NOT NULL COMMENT 'EMS用户ID',
    `platform`                 VARCHAR(40) COLLATE utf8mb4_unicode_ci   NOT NULL COMMENT '第三方平台标识',
    `app_id`                   VARCHAR(128) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '第三方应用ID',
    `third_party_user_id`      VARCHAR(128) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '第三方平台用户ID',
    `third_party_union_id`     VARCHAR(128) COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '第三方平台联合用户ID',
    `phone`                    VARCHAR(20) COLLATE utf8mb4_unicode_ci            DEFAULT NULL COMMENT '第三方返回手机号',
    `last_login_time`          DATETIME                                          DEFAULT NULL COMMENT '最近登录时间',
    `is_deleted`               BIT(1)                                   NOT NULL DEFAULT b'0' COMMENT '是否被删除：0未删除；1已删除',
    `create_user`              INT UNSIGNED                                      DEFAULT NULL COMMENT '创建人',
    `create_user_name`         VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人姓名',
    `create_time`              DATETIME                                          DEFAULT NULL COMMENT '创建时间',
    `update_user`              INT UNSIGNED                                      DEFAULT NULL COMMENT '修改人',
    `update_user_name`         VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '修改人姓名',
    `update_time`              DATETIME                                          DEFAULT NULL COMMENT '修改时间',
    `active_identity_key`      VARCHAR(320) GENERATED ALWAYS AS (IF(`is_deleted` = b'0', CONCAT(`platform`, '#', `app_id`, '#', `third_party_user_id`), NULL)) STORED COMMENT '活跃第三方身份唯一键',
    `active_user_platform_key` VARCHAR(220) GENERATED ALWAYS AS (IF(`is_deleted` = b'0', CONCAT(`user_id`, '#', `platform`, '#', `app_id`), NULL)) STORED COMMENT '活跃用户平台应用唯一键',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_phone` (`phone`),
    UNIQUE INDEX `uk_active_identity` (`active_identity_key`),
    UNIQUE INDEX `uk_active_user_platform` (`active_user_platform_key`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户第三方身份绑定表';

INSERT INTO sys_config (config_module_name, config_key, config_name, config_value, is_system, is_deleted, create_time)
SELECT 'mini', 'mini_account', '小程序账号配置', '{"appId":"","appSecret":""}', TRUE, FALSE, now()
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_config
    WHERE config_key = 'mini_account'
);
