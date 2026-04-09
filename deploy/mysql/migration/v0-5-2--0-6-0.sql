-- 日报模块初始化表
-- 用途：
-- 0. 新增账户开户快照表
-- 1. 新增电表日报快照表
-- 2. 新增账户日报快照表
-- 3. 新增日报任务日志表

CREATE TABLE `energy_account_open_record`
(
    `id`                   INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `account_id`           INT                                                           NOT NULL COMMENT '账户ID',
    `owner_id`             INT                                                                    DEFAULT NULL COMMENT '主体ID',
    `owner_type`           SMALLINT                                                               DEFAULT NULL COMMENT '主体类型',
    `owner_name`           VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '主体名称',
    `electric_account_type` SMALLINT                                                              DEFAULT NULL COMMENT '电费账户类型',
    `open_time`            DATETIME                                                               DEFAULT NULL COMMENT '开户时间',
    `create_user`          INT UNSIGNED                                                            DEFAULT NULL,
    `create_user_name`     VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `create_time`          DATETIME                                                                DEFAULT NULL,
    `update_user`          INT UNSIGNED                                                            DEFAULT NULL,
    `update_user_name`     VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `update_time`          DATETIME                                                                DEFAULT NULL,
    `is_deleted`           BIT(1)                                                         NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_energy_account_open_record_account_id` (`account_id`),
    KEY `idx_energy_account_open_record_open_time` (`open_time`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='账户开户快照表';

CREATE TABLE `energy_report_daily_meter`
(
    `id`                            INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_date`                   DATE                                                          NOT NULL COMMENT '报表日期',
    `account_id`                    INT                                                           NOT NULL COMMENT '账户ID',
    `owner_id`                      INT                                                                    DEFAULT NULL COMMENT '主体ID',
    `owner_type`                    SMALLINT                                                               DEFAULT NULL COMMENT '主体类型',
    `owner_name`                    VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '主体名称',
    `meter_id`                      INT                                                           NOT NULL COMMENT '电表ID',
    `meter_name`                    VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '电表名称',
    `device_no`                     VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '设备编号',
    `space_id`                      INT                                                                    DEFAULT NULL COMMENT '空间ID',
    `space_name`                    VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '空间名称',
    `electric_account_type`         SMALLINT                                                               DEFAULT NULL COMMENT '电费账户类型',
    `generate_type`                 SMALLINT                                                      NOT NULL COMMENT '生成类型',
    `begin_power`                   DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期初总读数',
    `begin_power_higher`            DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期初尖读数',
    `begin_power_high`              DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期初峰读数',
    `begin_power_low`               DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期初平读数',
    `begin_power_lower`             DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期初谷读数',
    `begin_power_deep_low`          DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期初深谷读数',
    `end_power`                     DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期末总读数',
    `end_power_higher`              DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期末尖读数',
    `end_power_high`                DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期末峰读数',
    `end_power_low`                 DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期末平读数',
    `end_power_lower`               DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期末谷读数',
    `end_power_deep_low`            DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期末深谷读数',
    `consume_power`                 DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '总用电量',
    `consume_power_higher`          DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '尖用电量',
    `consume_power_high`            DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '峰用电量',
    `consume_power_low`             DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '平用电量',
    `consume_power_lower`           DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '谷用电量',
    `consume_power_deep_low`        DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '深谷用电量',
    `electric_charge_amount`        DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '总电费',
    `electric_charge_amount_higher` DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '尖电费',
    `electric_charge_amount_high`   DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '峰电费',
    `electric_charge_amount_low`    DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '平电费',
    `electric_charge_amount_lower`  DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '谷电费',
    `electric_charge_amount_deep_low` DECIMAL(20, 8)                                                       DEFAULT NULL COMMENT '深谷电费',
    `display_price_higher`          DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '尖展示单价',
    `display_price_high`            DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '峰展示单价',
    `display_price_low`             DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '平展示单价',
    `display_price_lower`           DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '谷展示单价',
    `display_price_deep_low`        DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '深谷展示单价',
    `correction_pay_amount`         DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '补缴金额',
    `correction_refund_amount`      DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '退费金额',
    `correction_net_amount`         DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '补正净额',
    `begin_balance`                 DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期初余额',
    `end_balance`                   DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期末余额',
    `recharge_amount`               DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '充值金额',
    `create_user`                   INT UNSIGNED                                                             DEFAULT NULL,
    `create_user_name`              VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `create_time`                   DATETIME                                                                 DEFAULT NULL,
    `update_user`                   INT UNSIGNED                                                             DEFAULT NULL,
    `update_user_name`              VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `update_time`                   DATETIME                                                                 DEFAULT NULL,
    `is_deleted`                    BIT(1)                                                          NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_energy_report_daily_meter_date_account_meter` (`report_date`, `account_id`, `meter_id`),
    KEY `idx_energy_report_daily_meter_report_date` (`report_date`),
    KEY `idx_energy_report_daily_meter_account_id` (`account_id`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='电表日报表';

CREATE TABLE `energy_report_daily_account`
(
    `id`                                  INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_date`                         DATE                                                          NOT NULL COMMENT '报表日期',
    `account_id`                          INT                                                           NOT NULL COMMENT '账户ID',
    `owner_id`                            INT                                                                    DEFAULT NULL COMMENT '主体ID',
    `owner_type`                          SMALLINT                                                               DEFAULT NULL COMMENT '主体类型',
    `owner_name`                          VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '主体名称',
    `electric_account_type`               SMALLINT                                                               DEFAULT NULL COMMENT '电费账户类型',
    `meter_count`                         INT                                                           NOT NULL DEFAULT 0 COMMENT '电表数量',
    `consume_power`                       DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '总用电量',
    `consume_power_higher`                DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '尖用电量',
    `consume_power_high`                  DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '峰用电量',
    `consume_power_low`                   DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '平用电量',
    `consume_power_lower`                 DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '谷用电量',
    `consume_power_deep_low`              DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '深谷用电量',
    `electric_charge_amount`              DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '总电费',
    `electric_charge_amount_higher`       DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '尖电费',
    `electric_charge_amount_high`         DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '峰电费',
    `electric_charge_amount_low`          DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '平电费',
    `electric_charge_amount_lower`        DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '谷电费',
    `electric_charge_amount_deep_low`     DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '深谷电费',
    `monthly_charge_amount`               DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '包月费用',
    `correction_pay_amount`               DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '补缴金额',
    `correction_refund_amount`            DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '退费金额',
    `correction_net_amount`               DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '补正净额',
    `recharge_amount`                     DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '充值金额',
    `recharge_service_fee_amount`         DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '充值服务费',
    `total_debit_amount`                  DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '总支出金额',
    `begin_balance`                       DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期初余额',
    `end_balance`                         DECIMAL(20, 8)                                                         DEFAULT NULL COMMENT '期末余额',
    `accumulate_consume_power`            DECIMAL(20, 8)                                                NOT NULL DEFAULT 0 COMMENT '累计用电量',
    `accumulate_electric_charge_amount`   DECIMAL(20, 8)                                                NOT NULL DEFAULT 0 COMMENT '累计电费',
    `accumulate_monthly_charge_amount`    DECIMAL(20, 8)                                                NOT NULL DEFAULT 0 COMMENT '累计包月费用',
    `accumulate_correction_pay_amount`    DECIMAL(20, 8)                                                NOT NULL DEFAULT 0 COMMENT '累计补缴金额',
    `accumulate_correction_refund_amount` DECIMAL(20, 8)                                                NOT NULL DEFAULT 0 COMMENT '累计退费金额',
    `accumulate_recharge_amount`          DECIMAL(20, 8)                                                NOT NULL DEFAULT 0 COMMENT '累计充值金额',
    `accumulate_recharge_service_fee_amount` DECIMAL(20, 8)                                             NOT NULL DEFAULT 0 COMMENT '累计充值服务费',
    `accumulate_total_debit_amount`       DECIMAL(20, 8)                                                NOT NULL DEFAULT 0 COMMENT '累计总支出金额',
    `create_user`                         INT UNSIGNED                                                            DEFAULT NULL,
    `create_user_name`                    VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `create_time`                         DATETIME                                                                DEFAULT NULL,
    `update_user`                         INT UNSIGNED                                                            DEFAULT NULL,
    `update_user_name`                    VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `update_time`                         DATETIME                                                                DEFAULT NULL,
    `is_deleted`                          BIT(1)                                                         NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_energy_report_daily_account_date_account` (`report_date`, `account_id`),
    KEY `idx_energy_report_daily_account_report_date` (`report_date`),
    KEY `idx_energy_report_daily_account_account_id` (`account_id`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='账户日报表';

CREATE TABLE `energy_report_job_log`
(
    `id`               INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trigger_type`     SMALLINT                                                      NOT NULL COMMENT '触发方式',
    `start_date`       DATE                                                          NOT NULL COMMENT '开始日期',
    `end_date`         DATE                                                          NOT NULL COMMENT '结束日期',
    `status`           SMALLINT                                                      NOT NULL COMMENT '执行状态',
    `current_report_date` DATE                                                                DEFAULT NULL COMMENT '当前处理日期',
    `error_message`    VARCHAR(1000) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '错误信息',
    `trigger_by`       VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '触发人',
    `finish_time`      DATETIME                                                               DEFAULT NULL COMMENT '完成时间',
    `create_user`      INT UNSIGNED                                                           DEFAULT NULL,
    `create_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `create_time`      DATETIME                                                               DEFAULT NULL,
    `update_user`      INT UNSIGNED                                                           DEFAULT NULL,
    `update_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `update_time`      DATETIME                                                               DEFAULT NULL,
    `is_deleted`       BIT(1)                                                        NOT NULL DEFAULT b'0',
    PRIMARY KEY (`id`),
    KEY `idx_energy_report_job_log_date_range` (`start_date`, `end_date`),
    KEY `idx_energy_report_job_log_status` (`status`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='报表任务日志表';

-- 日报查询相关索引优化
-- 说明：
-- 1. 按账户批次构建日报后，主路径查询统一按账户与时间范围读取
-- 2. 删除已被新复合索引覆盖的旧索引
-- 3. power_record / power_consume 需要兼容 v0-3-0--0-4-0.sql 的历史索引状态

ALTER TABLE `energy_electric_meter_power_record`
    DROP INDEX `idx_meter_record_time`,
    ADD INDEX `idx_power_record_time_account_meter` (`record_time`, `account_id`, `meter_id`);

ALTER TABLE `energy_open_meter_record`
    ADD INDEX `idx_open_meter_show_time_account_id` (`show_time`, `account_id`);

ALTER TABLE `energy_meter_cancel_record`
    ADD INDEX `idx_meter_cancel_show_time_account_id` (`show_time`, `account_id`);

ALTER TABLE `energy_account_order_flow`
    DROP INDEX `idx_create_time`,
    ADD INDEX `idx_order_flow_create_time_account_balance_relation` (`create_time`, `account_id`, `balance_type`,
                                                                     `balance_relation_id`);

ALTER TABLE `energy_electric_meter_power_consume_record`
    DROP INDEX `idx_meter_meter_consume_time`,
    ADD INDEX `idx_meter_id` (`meter_id`),
    ADD INDEX `idx_meter_consume_time` (`meter_consume_time`),
    ADD INDEX `idx_power_consume_account_time_meter` (`account_id`, `meter_consume_time`, `meter_id`);

ALTER TABLE `energy_electric_meter_balance_consume_record`
    DROP INDEX `idx_account_id`,
    ADD INDEX `idx_balance_consume_account_time_meter` (`account_id`, `meter_consume_time`, `meter_id`);

ALTER TABLE `energy_account_balance_consume_record`
    DROP INDEX `idx_account_id`,
    ADD INDEX `idx_account_balance_consume_account_time` (`account_id`, `consume_time`);

-- 调整报表统计菜单显示，并移除“每日用电统计”菜单
UPDATE `sys_menu`
SET `is_hidden` = b'0',
    `update_time` = NOW()
WHERE `id` = 26;

DELETE FROM `sys_role_menu`
WHERE `menu_id` = 28;

DELETE FROM `sys_menu_auth`
WHERE `menu_id` = 28;

DELETE FROM `sys_menu_path`
WHERE `ancestor_id` = 28
   OR `descendant_id` = 28;

DELETE FROM `sys_menu`
WHERE `id` = 28;
