CREATE TABLE `energy_account`
(
    `id`                     INT    NOT NULL AUTO_INCREMENT,
    `owner_id`               INT                                                           DEFAULT NULL COMMENT '账户归属者id',
    `owner_type`             SMALLINT                                                      DEFAULT NULL COMMENT '账户类型，0企业1个人',
    `owner_name`             VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '企业/个人名称',
    `electric_account_type`  SMALLINT                                                      DEFAULT NULL COMMENT '电费账户类型：0按需、1包月、2合并计费',
    `monthly_pay_amount`     DECIMAL(20, 8)                                                DEFAULT NULL COMMENT '包月费用',
    `electric_price_plan_id` INT                                                           DEFAULT NULL COMMENT '计费方案id',
    `warn_plan_id`           INT                                                           DEFAULT NULL COMMENT '预警方案id',
    `electric_warn_type`     VARCHAR(255) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电费预警级别',
    `create_user`            INT UNSIGNED                                                  DEFAULT NULL,
    `create_user_name`       VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `create_time`            DATETIME                                                      DEFAULT NULL,
    `update_user`            INT UNSIGNED                                                  DEFAULT NULL,
    `update_user_name`       VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `update_time`            DATETIME                                                      DEFAULT NULL,
    `is_deleted`             BIT(1) NOT NULL                                               DEFAULT 0 COMMENT '1代表账户注销',
    `delete_time`            DATETIME                                                      DEFAULT NULL COMMENT '账户注销时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='能耗账户表';

CREATE TABLE `energy_open_meter_record`
(
    `id`               INT      NOT NULL AUTO_INCREMENT,
    `account_id`       INT      NOT NULL COMMENT '账户id',
    `meter_type`       SMALLINT NOT NULL COMMENT '1电2水',
    `meter_id`         INT      NOT NULL COMMENT '表id',
    `power`            DECIMAL(12, 2)                                               DEFAULT NULL COMMENT '表显示总读数',
    `power_higher`     DECIMAL(12, 2)                                               DEFAULT NULL COMMENT '尖读数',
    `power_high`       DECIMAL(12, 2)                                               DEFAULT NULL COMMENT '峰读数',
    `power_low`        DECIMAL(12, 2)                                               DEFAULT NULL COMMENT '平读数',
    `power_lower`      DECIMAL(12, 2)                                               DEFAULT NULL COMMENT '谷读数',
    `power_deep_low`   DECIMAL(12, 2)                                               DEFAULT NULL COMMENT '深谷读数',
    `show_time`        DATETIME                                                     DEFAULT NULL COMMENT '读表时间',
    `create_user`      INT UNSIGNED                                                 DEFAULT NULL,
    `create_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `create_time`      DATETIME                                                     DEFAULT NULL,
    `update_user`      INT UNSIGNED                                                 DEFAULT NULL,
    `update_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `update_time`      DATETIME                                                     DEFAULT NULL,
    `is_deleted`       BIT(1)   NOT NULL                                            DEFAULT 0 COMMENT '1代表账户注销',
    `delete_time`      DATETIME                                                     DEFAULT NULL COMMENT '账户注销时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='开户时刻表数据记录';

CREATE TABLE `energy_account_cancel_record`
(
    `id`                    INT    NOT NULL AUTO_INCREMENT,
    `cancel_no`             VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '销户编号',
    `account_id`            INT                                                           DEFAULT NULL,
    `owner_id`              INT                                                           DEFAULT NULL COMMENT '账户归属者id',
    `owner_type`            SMALLINT                                                      DEFAULT NULL COMMENT '账户类型，0企业1个人',
    `owner_name`            VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '企业/个人名称',
    `electric_account_type` SMALLINT                                                      DEFAULT NULL COMMENT '电费账户类型：0按需、1包月、2合并计费',
    `electric_meter_amount` INT                                                           DEFAULT NULL COMMENT '销户电表数量',
    `full_cancel`           BIT(1) NOT NULL                                               DEFAULT 0 COMMENT '1代表全部销户',
    `clean_balance_type`    SMALLINT                                                      DEFAULT NULL COMMENT '0:无处理;1:退款;2:补缴',
    `clean_balance_real`    DECIMAL(12, 2)                                                DEFAULT NULL COMMENT '真实结算余额',
    `clean_balance_ignore`  DECIMAL(10, 8)                                                DEFAULT NULL COMMENT '小数点后无法支付的金额',
    `remark`                VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '销户备注',
    `create_user`           INT UNSIGNED                                                  DEFAULT NULL,
    `create_user_name`      VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `create_time`           DATETIME                                                      DEFAULT NULL,
    `update_user`           INT UNSIGNED                                                  DEFAULT NULL,
    `update_user_name`      VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `update_time`           DATETIME                                                      DEFAULT NULL,
    `is_deleted`            BIT(1) NOT NULL                                               DEFAULT 0 COMMENT '1代表删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_cancel_no` (`cancel_no`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='账号销户';

CREATE TABLE `energy_meter_cancel_record`
(
    `id`                  INT                                                          NOT NULL AUTO_INCREMENT,
    `account_id`          INT                                                          NOT NULL,
    `cancel_no`           VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销户编号',
    `meter_type`          SMALLINT                                                     NOT NULL COMMENT '1电2水',
    `meter_id`            INT                                                          NOT NULL COMMENT '表id',
    `meter_name`          VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '表名称',
    `meter_no`            VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '表号',
    `space_id`            INT                                                                   DEFAULT NULL COMMENT '空间id',
    `space_name`          VARCHAR(100) COLLATE utf8mb4_unicode_ci                               DEFAULT NULL COMMENT '空间名称',
    `space_parent_ids`    VARCHAR(200) COLLATE utf8mb4_unicode_ci                               DEFAULT NULL COMMENT '父级id',
    `space_parent_names`  VARCHAR(1000) COLLATE utf8mb4_unicode_ci                              DEFAULT NULL COMMENT '父级名称',
    `is_online`           BIT(1)                                                                DEFAULT NULL COMMENT '是否在线：0不在线，1在线',
    `is_cut_off`          BIT(1)                                                                DEFAULT NULL COMMENT '是否断电：0未断闸，1断闸',
    `balance`             DECIMAL(12, 2)                                                        DEFAULT NULL COMMENT '余额',
    `power`               DECIMAL(12, 2)                                                        DEFAULT NULL COMMENT '读数',
    `power_higher`        DECIMAL(12, 2)                                                        DEFAULT NULL,
    `power_high`          DECIMAL(12, 2)                                                        DEFAULT NULL,
    `power_low`           DECIMAL(12, 2)                                                        DEFAULT NULL,
    `power_lower`         DECIMAL(12, 2)                                                        DEFAULT NULL,
    `power_deep_low`      DECIMAL(12, 2)                                                        DEFAULT NULL,
    `history_power_total` DECIMAL(12, 2)                                               NOT NULL DEFAULT 0 COMMENT '销户时累计总用电量',
    `show_time`           DATETIME                                                     NOT NULL COMMENT '读表时间',
    `create_user`         INT UNSIGNED                                                          DEFAULT NULL,
    `create_user_name`    VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL,
    `create_time`         DATETIME                                                              DEFAULT NULL,
    `update_user`         INT UNSIGNED                                                          DEFAULT NULL,
    `update_user_name`    VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL,
    `update_time`         DATETIME                                                              DEFAULT NULL,
    `is_deleted`          BIT(1)                                                       NOT NULL DEFAULT 0 COMMENT '1代表删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_cancel_no` (`cancel_no`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='销表信息';

CREATE TABLE `energy_account_order_flow`
(
    `id`                  INT                                                           NOT NULL AUTO_INCREMENT,
    `consume_id`          VARCHAR(255) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对应充值订单编号/消费编号',
    `balance_relation_id` INT                                                           NOT NULL COMMENT '账户关联id',
    `balance_type`        SMALLINT                                                      NOT NULL COMMENT '余额类型：1账户余额，2电表余额，3水表余额',
    `account_id`          INT                                                           NOT NULL COMMENT '能耗账户id',
    `amount`              DECIMAL(20, 8)                                                NOT NULL COMMENT '流水金额',
    `create_time`         DATETIME                                                      NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_consume_id` (`consume_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_account_id` (`account_id`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='能耗账户流水表';

CREATE TABLE `energy_account_balance`
(
    `id`                  INT            NOT NULL AUTO_INCREMENT COMMENT '主键',
    `balance_relation_id` INT            NOT NULL COMMENT '账户关联id',
    `balance_type`        SMALLINT       NOT NULL COMMENT '余额类型：0账户余额，1电表余额，2水表余额',
    `balance`             DECIMAL(20, 8) NOT NULL DEFAULT '0.00000000' COMMENT '余额金额',
    `account_id`          INT            NOT NULL COMMENT '账户id',
    `is_deleted`          BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除：0未删除，1已删除',
    `active_balance_key`  VARCHAR(64) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((IF(`is_deleted` = b'0',
                                                                                              CONCAT(
                                                                                                      `balance_relation_id`,
                                                                                                      '_',
                                                                                                      `balance_type`
                                                                                              ),
                                                                                              NULL))) STORED COMMENT '活跃余额唯一键（未删除时有效）',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_balance_id` (`balance_relation_id`, `balance_type`),
    UNIQUE KEY `uk_energy_account_balance_active_key` (`active_balance_key`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='能耗余额表';

CREATE TABLE `energy_account_meter_step`
(
    `id`                   INT            NOT NULL AUTO_INCREMENT,
    `account_id`           INT            NOT NULL,
    `meter_type`           SMALLINT                                                     DEFAULT NULL COMMENT '1电2水',
    `meter_id`             INT            NOT NULL COMMENT '表key',
    `step_start_value`     DECIMAL(12, 2) NOT NULL                                      DEFAULT 0 COMMENT '年度阶梯起始值',
    `history_power_offset` DECIMAL(12, 2) NOT NULL                                      DEFAULT 0 COMMENT '销户时累计总用电量',
    `current_year`         INT                                                          DEFAULT NULL COMMENT '年度',
    `is_latest`            BIT(1)         NOT NULL                                      DEFAULT 1 COMMENT '是否最新记录',
    `create_user`          INT UNSIGNED                                                 DEFAULT NULL,
    `create_user_name`     VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `create_time`          DATETIME                                                     DEFAULT NULL,
    `update_user`          INT UNSIGNED                                                 DEFAULT NULL,
    `update_user_name`     VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `update_time`          DATETIME                                                     DEFAULT NULL,
    `is_deleted`           BIT(1)         NOT NULL                                      DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='阶梯开始记录表';

CREATE TABLE `energy_warn_plan`
(
    `id`               INT    NOT NULL AUTO_INCREMENT,
    `name`             VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '名称',
    `first_level`      DECIMAL(20, 8)                                                DEFAULT NULL COMMENT '第一告警金额',
    `second_level`     DECIMAL(20, 8)                                                DEFAULT NULL COMMENT '第二告警金额',
    `auto_close`       BIT(1)                                                        DEFAULT NULL COMMENT '欠费自动断闸',
    `remark`           VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
    `create_user`      INT UNSIGNED                                                  DEFAULT NULL,
    `create_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `create_time`      DATETIME                                                      DEFAULT NULL,
    `update_user`      INT UNSIGNED                                                  DEFAULT NULL,
    `update_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `update_time`      DATETIME                                                      DEFAULT NULL,
    `is_deleted`       BIT(1) NOT NULL                                               DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='告警方案表';

CREATE TABLE `energy_electric_price_plan`
(
    `id`                      INT                                                          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`                    VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '电价方案名称',
    `price_higher`            DECIMAL(20, 8)                                               NOT NULL COMMENT '尖电价',
    `price_high`              DECIMAL(20, 8)                                               NOT NULL COMMENT '峰电价',
    `price_low`               DECIMAL(20, 8)                                               NOT NULL COMMENT '平电价',
    `price_lower`             DECIMAL(20, 8)                                               NOT NULL COMMENT '谷电价',
    `price_deep_low`          DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '深谷电价',
    `is_step`                 BIT(1)                                                                DEFAULT NULL COMMENT '是否启用阶梯计费',
    `step_price`              VARCHAR(1000) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci        DEFAULT NULL COMMENT '阶梯配置，json数组，{start:, end:, rate:},end =-1代表无上限',
    `is_custom_price`         BIT(1)                                                                DEFAULT b'0' COMMENT '是否自定义价格',
    `price_higher_base`       DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '尖标准电价',
    `price_high_base`         DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '峰标准电价',
    `price_low_base`          DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '平标准电价',
    `price_lower_base`        DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '谷标准电价',
    `price_deep_low_base`     DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '深谷标准电价',
    `price_higher_multiply`   DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '尖电价倍率',
    `price_high_multiply`     DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '峰电价倍率',
    `price_low_multiply`      DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '平电价倍率',
    `price_lower_multiply`    DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '谷电价倍率',
    `price_deep_low_multiply` DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '深谷电价倍率',
    `create_user`             INT UNSIGNED                                                          DEFAULT NULL,
    `create_user_name`        VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL,
    `create_time`             DATETIME                                                              DEFAULT NULL,
    `update_user`             INT UNSIGNED                                                          DEFAULT NULL,
    `update_user_name`        VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL,
    `update_time`             DATETIME                                                              DEFAULT NULL,
    `is_deleted`              BIT(1)                                                       NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='电价格方案表';

CREATE TABLE `energy_electric_meter`
(
    `id`                INT                                                           NOT NULL AUTO_INCREMENT,
    `space_id`          INT                                                           NOT NULL COMMENT '空间id',
    `meter_name`        VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '电表名称',
    `meter_no`          VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '电表编号，系统生成',
    `device_no`         VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备编号，设备上报标识',
    `model_id`          INT                                                           NOT NULL COMMENT '型号id',
    `product_code`      VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '产品唯一标识，冗余字段',
    `communicate_model` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '通信模式',
    `gateway_id`        INT                                                                    DEFAULT NULL COMMENT '网关id',
    `port_no`           INT                                                                    DEFAULT NULL COMMENT '串口号',
    `meter_address`     INT                                                                    DEFAULT NULL COMMENT '电表通讯地址',
    `imei`              VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '移动设备IMEI',
    `is_online`         BIT(1)                                                                 DEFAULT NULL COMMENT '是否在线：0不在线，1在线',
    `is_cut_off`        BIT(1)                                                        NOT NULL DEFAULT b'0' COMMENT '是否断电：0未断闸，1断闸',
    `remark`            VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '备注',
    `iot_id`            VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT 'iot服务里的id',
    `is_calculate`      BIT(1)                                                        NOT NULL DEFAULT b'0' COMMENT '汇总时是否计算在内，和calculate_type无关',
    `calculate_type`    INT                                                                    DEFAULT NULL COMMENT '用量类型，和is_calculate无关',
    `is_prepay`         BIT(1)                                                        NOT NULL DEFAULT b'0' COMMENT '是否预付费电表：0不是预付费，1是预付费',
    `protected_model`   BIT(1)                                                                 DEFAULT NULL COMMENT '是否保电，即欠费不断电：0不保电，1保电',
    `price_plan_id`     INT                                                                    DEFAULT NULL COMMENT '计费方案id',
    `warn_plan_id`      INT                                                                    DEFAULT NULL COMMENT '预警方案',
    `warn_type`         VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '当前预警级别',
    `account_id`        INT                                                                    DEFAULT NULL COMMENT '所属账户id',
    `ct`                INT                                                                    DEFAULT NULL COMMENT 'ct变比，可为null',
    `create_user`       INT UNSIGNED                                                           DEFAULT NULL,
    `create_user_name`  VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `create_time`       DATETIME                                                               DEFAULT NULL,
    `update_user`       INT UNSIGNED                                                           DEFAULT NULL,
    `update_user_name`  VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `update_time`       DATETIME                                                               DEFAULT NULL,
    `is_deleted`        BIT(1)                                                        NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    `own_area_id`       INT                                                                    DEFAULT NULL COMMENT '所属区域',
    `active_device_no`  VARCHAR(100) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((IF(`is_deleted` = b'0', `device_no`, NULL))) STORED COMMENT '活跃设备编号（未删除时等于设备编号）',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_energy_electric_meter_iot_id` (`iot_id`),
    UNIQUE KEY `uk_energy_electric_meter_active_device_no` (`active_device_no`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='电表';

CREATE TABLE `energy_electric_meter_power_record`
(
    `id`                 INT    NOT NULL AUTO_INCREMENT,
    `meter_id`           INT    NOT NULL,
    `meter_name`         VARCHAR(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电表名称',
    `meter_no`           VARCHAR(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电表编号',
    `account_id`         INT                                    DEFAULT NULL COMMENT '账户id',
    `is_prepay`          BIT(1)                                 DEFAULT NULL COMMENT '是否预付费电表：0不是预付费，1是预付费',
    `ct`                 INT                                    DEFAULT NULL,
    `power`              DECIMAL(12, 2)                         DEFAULT NULL COMMENT '电表度数',
    `power_higher`       DECIMAL(12, 2)                         DEFAULT NULL COMMENT '尖用电量',
    `power_high`         DECIMAL(12, 2)                         DEFAULT NULL COMMENT '峰用电量',
    `power_low`          DECIMAL(12, 2)                         DEFAULT NULL COMMENT '平用电量',
    `power_lower`        DECIMAL(12, 2)                         DEFAULT NULL COMMENT '谷用电量',
    `power_deep_low`     DECIMAL(12, 2)                         DEFAULT NULL COMMENT '深谷用电量',
    `original_report_id` VARCHAR(70)                            DEFAULT NULL COMMENT '电表上报数据记录id',
    `record_time`        DATETIME                               DEFAULT NULL COMMENT '抄表时间',
    `create_time`        DATETIME                               DEFAULT NULL,
    `is_deleted`         BIT(1) NOT NULL                        DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_record_time` (`record_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='电表电量数据记录';

CREATE TABLE `energy_electric_meter_power_relation`
(
    `id`                    INT    NOT NULL AUTO_INCREMENT,
    `record_id`             INT    NOT NULL COMMENT '电表电量数据记录关联id',
    `meter_id`              INT    NOT NULL,
    `is_calculate`          BIT(1)                                   DEFAULT NULL COMMENT '汇总时是否计算在内，不表示calculate_type的开关',
    `calculate_type`        INT                                      DEFAULT NULL COMMENT '用量类型，和is_calculate没关联',
    `calculate_type_name`   VARCHAR(50) COLLATE utf8mb4_unicode_ci   DEFAULT NULL COMMENT '用量类型，和is_calculate没关联',
    `space_id`              INT                                      DEFAULT NULL COMMENT '空间id',
    `space_name`            VARCHAR(100) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '空间名称',
    `space_parent_ids`      VARCHAR(200) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '父级id',
    `space_parent_names`    VARCHAR(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '父级名称',
    `account_id`            INT                                      DEFAULT NULL COMMENT '账户id',
    `electric_account_type` SMALLINT                                 DEFAULT NULL COMMENT '电费计量类型：0按需、1包月、2合并计费',
    `owner_id`              INT                                      DEFAULT NULL COMMENT '所有人id',
    `owner_type`            SMALLINT                                 DEFAULT NULL COMMENT '所有人类型：0企业，1个人',
    `owner_name`            VARCHAR(50) COLLATE utf8mb4_unicode_ci   DEFAULT NULL COMMENT '企业/个人名称',
    `record_time`           DATETIME                                 DEFAULT NULL COMMENT '抄表时间',
    `create_time`           DATETIME                                 DEFAULT NULL,
    `is_deleted`            BIT(1) NOT NULL                          DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_record_id` (`record_id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='电表电量关联信息';

CREATE TABLE `energy_electric_meter_power_consume_record`
(
    `id`                     INT      NOT NULL AUTO_INCREMENT,
    `meter_id`               INT      NOT NULL,
    `is_calculate`           BIT(1)         DEFAULT NULL COMMENT '汇总时是否计算在内，不表示calculate_type的开关',
    `calculate_type`         INT            DEFAULT NULL COMMENT '用量类型，和is_calculate没关联',
    `account_id`             INT            DEFAULT NULL COMMENT '账户id',
    `space_id`               INT            DEFAULT NULL COMMENT '空间id',
    `begin_record_id`        INT      NOT NULL COMMENT '起始电表记录id',
    `begin_power`            DECIMAL(12, 2) DEFAULT NULL,
    `begin_power_higher`     DECIMAL(12, 2) DEFAULT NULL,
    `begin_power_high`       DECIMAL(12, 2) DEFAULT NULL,
    `begin_power_low`        DECIMAL(12, 2) DEFAULT NULL,
    `begin_power_lower`      DECIMAL(12, 2) DEFAULT NULL,
    `begin_power_deep_low`   DECIMAL(12, 2) DEFAULT NULL,
    `begin_record_time`      DATETIME       DEFAULT NULL COMMENT '开始记录抄表时间',
    `end_record_id`          INT      NOT NULL COMMENT '截止电表记录id',
    `end_power`              DECIMAL(12, 2) DEFAULT NULL,
    `end_power_higher`       DECIMAL(12, 2) DEFAULT NULL,
    `end_power_high`         DECIMAL(12, 2) DEFAULT NULL,
    `end_power_low`          DECIMAL(12, 2) DEFAULT NULL,
    `end_power_lower`        DECIMAL(12, 2) DEFAULT NULL,
    `end_power_deep_low`     DECIMAL(12, 2) DEFAULT NULL,
    `end_record_time`        DATETIME       DEFAULT NULL COMMENT '截止记录抄表时间',
    `consume_power`          DECIMAL(12, 2) DEFAULT NULL COMMENT '总消耗电量',
    `consume_power_higher`   DECIMAL(12, 2) DEFAULT NULL COMMENT '尖用电量消耗电量',
    `consume_power_high`     DECIMAL(12, 2) DEFAULT NULL COMMENT '峰用电量消耗电量',
    `consume_power_low`      DECIMAL(12, 2) DEFAULT NULL COMMENT '平用电量消耗电量',
    `consume_power_lower`    DECIMAL(12, 2) DEFAULT NULL COMMENT '谷用电量消耗电量',
    `consume_power_deep_low` DECIMAL(12, 2) DEFAULT NULL COMMENT '深谷用电量消耗电量',
    `meter_consume_time`     DATETIME NOT NULL COMMENT '消费时间',
    `create_time`            DATETIME NOT NULL COMMENT '平台处理时间',
    `is_deleted`             BIT(1)         DEFAULT b'0',
    PRIMARY KEY (`id`),
    KEY `idx_meter_id` (`meter_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_meter_consume_time` (`meter_consume_time`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='电表用电消耗数据';

CREATE TABLE `energy_electric_meter_balance_consume_record`
(
    `id`                      INT                                                          NOT NULL AUTO_INCREMENT,
    `meter_consume_record_id` INT                                                          NOT NULL DEFAULT 0 COMMENT '水电表消耗记录id',
    `consume_no`              VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消费单号',
    `consume_type`            SMALLINT                                                     NOT NULL COMMENT '扣费类型，1用电消费，2补正消费',
    `meter_type`              SMALLINT                                                     NOT NULL COMMENT '表类型：1电、2水',
    `account_id`              INT                                                          NOT NULL COMMENT '账户id',
    `meter_id`                INT                                                                   DEFAULT NULL COMMENT '表id',
    `meter_name`              VARCHAR(100) COLLATE utf8mb4_unicode_ci                               DEFAULT NULL COMMENT '电表名称',
    `meter_no`                VARCHAR(50) COLLATE utf8mb4_unicode_ci                                DEFAULT NULL COMMENT '电表编号',
    `owner_id`                INT                                                                   DEFAULT NULL COMMENT '归属者id',
    `owner_type`              SMALLINT                                                              DEFAULT NULL COMMENT '归属者类型：0企业，1个人',
    `owner_name`              VARCHAR(50) COLLATE utf8mb4_unicode_ci                                DEFAULT NULL COMMENT '归属者名称',
    `electric_account_type`   SMALLINT                                                              DEFAULT NULL COMMENT '电费计量类型：0按需、1包月、2合并计费',
    `space_id`                INT                                                                   DEFAULT NULL COMMENT '空间id',
    `space_name`              VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '空间名称',
    `price_plan_id`           INT                                                                   DEFAULT NULL COMMENT '计费方案id',
    `price_plan_name`         VARCHAR(50) COLLATE utf8mb4_unicode_ci                                DEFAULT NULL COMMENT '计费方案名称',
    `step_start_value`        DECIMAL(12, 2)                                                        DEFAULT NULL COMMENT '年度阶梯起始值',
    `history_power_offset`    DECIMAL(12, 2)                                                        DEFAULT NULL COMMENT '销户时累计总用电量',
    `step_rate`               DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '阶梯倍率',
    `consume_amount`          DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '总消耗金额',
    `consume_amount_higher`   DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '尖用电量消耗金额',
    `consume_amount_high`     DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '峰用电量消耗金额',
    `consume_amount_low`      DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '平用电量消耗金额',
    `consume_amount_lower`    DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '谷用电量消耗金额',
    `consume_amount_deep_low` DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '深谷用电量消耗金额',
    `price_higher`            DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '尖单价',
    `price_high`              DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '峰单价',
    `price_low`               DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '平单价',
    `price_lower`             DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '谷单价',
    `price_deep_low`          DECIMAL(20, 8)                                                        DEFAULT NULL COMMENT '深谷单价',
    `begin_balance`           DECIMAL(20, 8)                                               NOT NULL COMMENT '起始余额',
    `end_balance`             DECIMAL(20, 8)                                               NOT NULL COMMENT '截止余额',
    `remark`                  VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '备注',
    `meter_consume_time`      DATETIME                                                     NOT NULL COMMENT '消费时间',
    `create_time`             DATETIME                                                     NOT NULL COMMENT '平台处理时间',
    `is_deleted`              BIT(1)                                                                DEFAULT b'0',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_meter_consume_record_id` (`meter_consume_record_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_meter_id` (`meter_id`),
    KEY `idx_account_id` (`account_id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='能耗表余额消费记录';

CREATE TABLE `energy_account_balance_consume_record`
(
    `id`            INT                                                          NOT NULL AUTO_INCREMENT,
    `consume_no`    VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消费单号',
    `consume_type`  SMALLINT                                                     NOT NULL COMMENT '扣费类型，0包月费用',
    `account_id`    INT                                                          NOT NULL COMMENT '账户id',
    `owner_id`      INT                                                           DEFAULT NULL COMMENT '账户归属者id',
    `owner_type`    SMALLINT                                                      DEFAULT NULL COMMENT '账户类型，0企业1个人',
    `owner_name`    VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '企业/个人名称',
    `pay_amount`    DECIMAL(20, 8)                                               NOT NULL COMMENT '支付金额',
    `begin_balance` DECIMAL(20, 8)                                               NOT NULL COMMENT '起始余额',
    `end_balance`   DECIMAL(20, 8)                                               NOT NULL COMMENT '截止余额',
    `remark`        VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
    `consume_time`  DATETIME                                                     NOT NULL COMMENT '消费时间',
    `create_time`   DATETIME                                                     NOT NULL COMMENT '平台处理时间',
    `is_deleted`    BIT(1)                                                        DEFAULT b'0',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_account_id` (`account_id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='能耗账户消费记录';

CREATE TABLE `energy_gateway`
(
    `id`                INT                                                           NOT NULL AUTO_INCREMENT,
    `space_id`          INT                                                                    DEFAULT NULL COMMENT '空间id',
    `gateway_name`      VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '网关名称',
    `gateway_no`        VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '网关编号',
    `device_no`         VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备编号，设备上报标识',
    `model_id`          INT                                                           NOT NULL COMMENT '型号id',
    `product_code`      VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '产品唯一标识',
    `communicate_model` VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '通信模式',
    `sn`                VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '序列号',
    `imei`              VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '移动设备IMEI',
    `is_online`         BIT(1)                                                                 DEFAULT NULL COMMENT '是否在线：0不在线，1在线',
    `config_info`       text CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci COMMENT '网关配置信息,json字符串',
    `remark`            VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '备注',
    `iot_id`            VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT 'iot服务里的id',
    `create_user`       INT UNSIGNED                                                           DEFAULT NULL,
    `create_user_name`  VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `create_time`       DATETIME                                                               DEFAULT NULL,
    `update_user`       INT UNSIGNED                                                           DEFAULT NULL,
    `update_user_name`  VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `update_time`       DATETIME                                                               DEFAULT NULL,
    `is_deleted`        BIT(1)                                                        NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    `own_area_id`       INT                                                                    DEFAULT NULL COMMENT '所属区域',
    `active_device_no`  VARCHAR(100) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((IF(`is_deleted` = b'0', `device_no`, NULL))) STORED COMMENT '活跃设备编号（未删除时等于设备编号）',
    PRIMARY KEY (`id`),
    KEY `idx_energy_gateway_iot_id` (`iot_id`),
    UNIQUE KEY `uk_energy_gateway_active_device_no` (`active_device_no`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='智能网关';

CREATE TABLE `device_type`
(
    `id`               INT                                                          NOT NULL AUTO_INCREMENT,
    `pid`              INT                                                          NOT NULL COMMENT '父级id',
    `ancestor_id`      VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '完整的祖级id，不包括自身',
    `type_name`        VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品类名',
    `type_key`         VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品类key，对应DeviceTypeEnum的key',
    `level`            SMALLINT                                                     NOT NULL COMMENT '层级',
    `create_user`      INT UNSIGNED                                                          DEFAULT NULL,
    `create_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL,
    `create_time`      DATETIME                                                              DEFAULT NULL,
    `update_user`      INT UNSIGNED                                                          DEFAULT NULL,
    `update_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL,
    `update_time`      DATETIME                                                              DEFAULT NULL,
    `is_deleted`       BIT(1)                                                       NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_type_key` (`type_key`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='智能设备品类表';

INSERT INTO device_type
    (id, pid, ancestor_id, type_name, type_key, `level`)
VALUES (1, 0, '0', '能源', 'energy', 1);
INSERT INTO device_type
    (id, pid, ancestor_id, type_name, type_key, `level`)
VALUES (2, 1, '0,1', '计量设备', 'metering', 2);
INSERT INTO device_type
    (id, pid, ancestor_id, type_name, type_key, `level`)
VALUES (3, 2, '0,1,2', '智能电表', 'electricMeter', 3);
INSERT INTO device_type
    (id, pid, ancestor_id, type_name, type_key, `level`)
VALUES (4, 1, '0,1', '网络设备', 'energy_network', 2);
INSERT INTO device_type
    (id, pid, ancestor_id, type_name, type_key, `level`)
VALUES (5, 4, '0,1,4', '网关', 'gateway', 3);

CREATE TABLE `device_command_record`
(
    `id`                INT                                                            NOT NULL AUTO_INCREMENT,
    `device_type_key`   VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '设备类型',
    `device_id`         INT                                                            NOT NULL COMMENT '设备id',
    `device_name`       VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '设备名称',
    `device_no`         VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '设备编号',
    `device_iot_id`     VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '设备对接的iot平台的id',
    `space_id`          INT                                                                     DEFAULT NULL COMMENT '设备所在空间id',
    `space_name`        VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '设备所在空间名称',
    `account_id`        INT                                                                     DEFAULT NULL COMMENT '账户id',
    `command_type`      SMALLINT                                                       NOT NULL COMMENT '命令类型',
    `command_source`    SMALLINT                                                       NOT NULL COMMENT '命令来源：0系统；1用户',
    `command_data`      VARCHAR(5000) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '命令内容',
    `success`           BIT(1)                                                         NOT NULL DEFAULT b'0' COMMENT '是否成功执行：0失败，1成功',
    `success_time`      DATETIME                                                                DEFAULT NULL COMMENT '成功的时间',
    `last_execute_time` DATETIME                                                                DEFAULT NULL COMMENT '最后执行时间',
    `ensure_success`    BIT(1)                                                         NOT NULL DEFAULT b'0' COMMENT '是否需要确保命令执行成功，为1时的命令会重试',
    `execute_times`     SMALLINT                                                       NOT NULL DEFAULT 0 COMMENT '运行次数',
    `remark`            VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '备注，任务描述',
    `create_user`       INT UNSIGNED                                                            DEFAULT NULL,
    `create_user_name`  VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci            DEFAULT NULL,
    `create_time`       DATETIME                                                                DEFAULT NULL,
    `update_user`       INT UNSIGNED                                                            DEFAULT NULL,
    `update_user_name`  VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci            DEFAULT NULL,
    `update_time`       DATETIME                                                                DEFAULT NULL,
    `is_deleted`        BIT(1)                                                         NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    `own_area_id`       INT                                                                     DEFAULT NULL COMMENT '所属区域',
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='设备命令记录';

CREATE TABLE `device_command_execute_record`
(
    `id`               INT                                                           NOT NULL AUTO_INCREMENT,
    `command_id`       INT                                                           NOT NULL COMMENT '命令id',
    `success`          BIT(1)                                                        NOT NULL DEFAULT b'0' COMMENT '是否成功执行：0失败，1成功',
    `reason`           VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '失败原因',
    `execute_time`     DATETIME                                                               DEFAULT NULL COMMENT '运行时间',
    `command_source`   SMALLINT                                                               DEFAULT '0' COMMENT '命令来源：0系统；1用户',
    `create_user`      INT UNSIGNED                                                           DEFAULT NULL,
    `create_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `create_time`      DATETIME                                                               DEFAULT NULL,
    `update_user`      INT UNSIGNED                                                           DEFAULT NULL,
    `update_user_name` VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `update_time`      DATETIME                                                               DEFAULT NULL,
    `is_deleted`       BIT(1)                                                        NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`),
    KEY `idx_command_id` (`command_id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC COMMENT ='设备命令执行记录';

CREATE TABLE `device_model`
(
    `id`                INT                                                           NOT NULL AUTO_INCREMENT,
    `type_id`           INT                                                           NOT NULL COMMENT '设备品类id',
    `type_key`          VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    `manufacturer_name` VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '厂商名称',
    `model_name`        VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '型号名称',
    `product_code`      VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '产品唯一标识',
    `model_property`    text CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         NOT NULL COMMENT '各型号设备的个性化属性',
    `create_user`       INT UNSIGNED                                                           DEFAULT NULL,
    `create_user_name`  VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `create_time`       DATETIME                                                               DEFAULT NULL,
    `update_user`       INT UNSIGNED                                                           DEFAULT NULL,
    `update_user_name`  VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL,
    `update_time`       DATETIME                                                               DEFAULT NULL,
    `is_deleted`        BIT(1)                                                        NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    UNIQUE KEY `uk_device_model_code` (`product_code`) USING BTREE,
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='智能设备厂商型号';

INSERT INTO device_model
(id, type_id, type_key, manufacturer_name, model_name, product_code, model_property)
VALUES (1, 3, 'electricMeter', '安科瑞', 'ddsy1352', 'ddsy1352',
        '{"communicateModel":"tcp","isCt":true,"isPrepay":true}');
INSERT INTO device_model
(id, type_id, type_key, manufacturer_name, model_name, product_code, model_property)
VALUES (2, 3, 'electricMeter', '安科瑞', 'dtsy1352-4g', 'dtsy1352-4g',
        '{"communicateModel":"nb","isCt":true,"isPrepay":true}');
INSERT INTO device_model
(id, type_id, type_key, manufacturer_name, model_name, product_code, model_property)
VALUES (3, 5, 'gateway', '安科瑞', 'tcp智能网关', 'AWT100_4G_MQTT', '{"communicateModel":"tcpClient模式"}');



CREATE TABLE `purchase_order`
(
    `id`                  INT UNSIGNED                                                 NOT NULL AUTO_INCREMENT COMMENT '订单id',
    `order_sn`            VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '订单号',
    `user_id`             INT UNSIGNED                                                 NOT NULL COMMENT '用户id',
    `user_real_name`      VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户真实名称',
    `user_phone`          VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户联系方式',
    `third_party_user_id` VARCHAR(64) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方用户id',
    `order_type`          SMALLINT                                                     NOT NULL COMMENT '订单分类',
    `order_amount`        DECIMAL(20, 2)                                               NOT NULL COMMENT '订单金额',
    `currency`            CHAR(10)                                                              DEFAULT 'CNY' COMMENT '币种',
    `service_rate`        DECIMAL(10, 4)                                                        DEFAULT NULL COMMENT '服务费比例(%)',
    `service_amount`      DECIMAL(18, 2)                                                        DEFAULT NULL COMMENT '服务费金额',
    `user_pay_amount`     DECIMAL(20, 2)                                               NOT NULL COMMENT '用户实际支付金额',
    `payment_channel`     VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付渠道',
    `order_status`        VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单状态',
    `order_create_time`   DATETIME                                                     NOT NULL COMMENT '订单生成时间',
    `order_pay_stop_time` DATETIME                                                     NOT NULL COMMENT '订单截止支付时间',
    `order_success_time`  DATETIME                                                              DEFAULT NULL COMMENT '支付完成时间',
    `remark`              VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '备注',
    `ticket_no`           VARCHAR(64) COLLATE utf8mb4_unicode_ci                                DEFAULT NULL COMMENT '票据号',
    `is_deleted`          BIT(1)                                                       NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `tp_order_order_sn_IDX` (`order_sn`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单表';

CREATE TABLE `order_detail_energy_top_up`
(
    `id`                    INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单id',
    `order_sn`              VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单号',
    `owner_id`              INT                                                          DEFAULT NULL COMMENT '账户归属者id',
    `owner_type`            SMALLINT                                                     DEFAULT NULL COMMENT '账户类型，0企业1个人',
    `owner_name`            VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '企业/个人名称',
    `account_id`            INT          NOT NULL COMMENT '账户id',
    `electric_account_type` SMALLINT                                                     DEFAULT NULL COMMENT '电费账户类型：0按需、1包月、2合并计费',
    `meter_type`            SMALLINT     NOT NULL COMMENT '1电2水',
    `meter_id`              INT          NOT NULL COMMENT '表id',
    `meter_name`            VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电表名称',
    `meter_no`              VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电表编号，系统生成',
    `space_id`              INT                                                          DEFAULT NULL COMMENT '空间id',
    `space_name`            VARCHAR(100) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '空间名称',
    `space_parent_ids`      VARCHAR(200) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '父级id',
    `space_parent_names`    VARCHAR(1000) COLLATE utf8mb4_unicode_ci                     DEFAULT NULL COMMENT '父级名称',
    `balance_type`          SMALLINT                                                     DEFAULT NULL COMMENT '余额类型：0账户余额，1电表余额',
    `create_time`           DATETIME     NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_order_sn` (`order_sn`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='能耗充值订单详情表';

CREATE TABLE `order_detail_termination`
(
    `id`                    INT UNSIGNED                                                 NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `order_sn`              VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
    `cancel_no`             VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销户编号',
    `account_id`            INT                                                          NOT NULL COMMENT '账户id',
    `owner_id`              INT                                                                   DEFAULT NULL COMMENT '账户归属者id',
    `owner_type`            SMALLINT                                                              DEFAULT NULL COMMENT '账户类型，0企业1个人',
    `owner_name`            VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '企业/个人名称',
    `settlement_amount`     DECIMAL(20, 8)                                               NOT NULL DEFAULT '0.00000000' COMMENT '结算金额',
    `electric_account_type` SMALLINT                                                     NOT NULL COMMENT '电费账户类型：0按需、1包月、2合并计费',
    `electric_meter_amount` INT                                                          NOT NULL COMMENT '销户电表数量',
    `full_cancel`           BIT(1)                                                       NOT NULL COMMENT '1代表全部销户',
    `close_reason`          VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '终止原因',
    `snapshot_payload`      text CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci                 DEFAULT NULL COMMENT '结算快照明细(JSON)',
    `create_time`           DATETIME                                                     NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_detail_account_termination_sn` (`order_sn`),
    KEY `idx_order_detail_account_termination_cancel_no` (`cancel_no`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='销户/销表结算订单详情表';

CREATE TABLE `order_third_party_prepay`
(
    `id`                  INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT,
    `order_sn`            VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '订单号',
    `prepay_id`           VARCHAR(64) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '预订单id',
    `third_party_user_id` VARCHAR(128) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方用户号',
    `third_party_sn`      VARCHAR(64) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '第三方订单编号',
    `prepay_at`           DATETIME                                                      NOT NULL COMMENT '预订单生成时间',
    `is_deleted`          BIT(1)                                                        NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `tp_order_third_part_prepay_order_sn_IDX` (`order_sn`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='第三方支付预订单';

CREATE TABLE `order_refund`
(
    `id`                       INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT COMMENT '退款id',
    `refund_sn`                VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '退款号',
    `order_sn`                 VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '订单号',
    `refund_type`              VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '退款方式：线下；微信',
    `refund_amount`            DECIMAL(20, 2)                                                NOT NULL COMMENT '退款金额',
    `status`                   VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '退款状态',
    `reason`                   VARCHAR(300) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '退款原因',
    `user_received_account`    VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '退款入账账户',
    `applicant_user_id`        INT                                                           NOT NULL COMMENT '申请人ID',
    `user_real_name`           VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '申请人真实名称',
    `user_phone`               VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '申请人联系方式',
    `refund_success_time`      DATETIME                                                               DEFAULT NULL COMMENT '退款完成时间',
    `third_party_success_time` VARCHAR(64) COLLATE utf8mb4_unicode_ci                                 DEFAULT NULL COMMENT '第三方的退款完成时间',
    `is_deleted`               BIT(1)                                                        NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_refund_sn` (`refund_sn`) USING BTREE,
    KEY `index_order_sn` (`order_sn`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='退款记录表';

CREATE TABLE `sys_config`
(
    `id`                 INT UNSIGNED                                                 NOT NULL AUTO_INCREMENT,
    `config_module_name` VARCHAR(30) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置所属模块name',
    `config_key`         VARCHAR(30) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置key',
    `config_name`        VARCHAR(30) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置name',
    `config_value`       text CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci        NOT NULL COMMENT '配置value',
    `is_system`          BIT(1)                                                       NOT NULL DEFAULT b'1' COMMENT '是否内置，内置配置项可在系统配置列表修改',
    `is_deleted`         BIT(1)                                                       NOT NULL DEFAULT b'0' COMMENT '是否被删除：0未删除；1已删除',
    `create_user`        INT UNSIGNED                                                          DEFAULT NULL COMMENT '创建人',
    `create_user_name`   VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '创建人姓名',
    `create_time`        DATETIME                                                              DEFAULT NULL COMMENT '创建时间',
    `update_user`        INT UNSIGNED                                                          DEFAULT NULL COMMENT '修改人',
    `update_user_name`   VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '修改人姓名',
    `update_time`        DATETIME                                                              DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `config_key` (`config_key`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统参数配置';
INSERT INTO sys_config (config_module_name, config_key, config_name, config_value, is_system, is_deleted, create_time)
VALUES ('finance', 'service_rate', '默认服务费', '0.1', TRUE, FALSE, '2025-01-02 03:04:05'),
       ('finance', 'wx_pay_config', '微信支付配置', '{}', TRUE, FALSE, '2025-01-02 03:04:05'),
       ('device', 'device_config', '设备配置',
        '[{"areaId":1,"deviceConfigList":[{"moduleServiceName":"EnergyService","implName":"defaultEnergyServiceImpl","configValue":{"addressUrl":"http://127.0.0.1:8899"}}]}]',
        TRUE, FALSE, '2025-01-02 03:04:05'),
       ('plan', 'electric_price_time', '尖峰平谷深谷时间段配置',
        '[{"start":[0,0,0],"type":4},{"start":[6,0,0],"type":3},{"start":[11,0,0],"type":4},{"start":[13,0,0],"type":3},{"start":[14,0,0],"type":2},{"start":[22,0,0],"type":3}]',
        TRUE, FALSE, '2025-01-02 03:04:05'),
       ('plan', 'electric_step_price', '默认阶梯电价配置', '{}', TRUE, FALSE, '2025-01-02 03:04:05'),
       ('plan', 'electric_price_type', '默认尖峰平谷深谷电价配置', '[{"type":1,"price":0.12}]', TRUE, FALSE,
        '2025-01-02 03:04:05');


CREATE TABLE `sys_transaction_message`
(
    `id`            INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT,
    `business_type` VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '业务类型名称',
    `sn`            VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编号',
    `destination`   VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息目标地址',
    `route`         VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息路由',
    `payload`       text CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci         NOT NULL COMMENT '消息载荷',
    `payload_type`  VARCHAR(200) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '消息载荷类型',
    `last_run_at`   DATETIME                                                               DEFAULT NULL COMMENT '最后运行时间',
    `try_times`     INT UNSIGNED                                                  NOT NULL DEFAULT '0' COMMENT '尝试次数',
    `create_time`   DATETIME                                                      NOT NULL COMMENT '创建时间',
    `is_success`    BIT(1)                                                        NOT NULL DEFAULT b'0' COMMENT '状态：0未成功，1成功',
    PRIMARY KEY (`id`),
    UNIQUE KEY `message_unique_key` (`sn`, `business_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单事务消息';

CREATE TABLE `sys_space`
(
    `id`               INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT,
    `name`             VARCHAR(32) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '名称',
    `pid`              INT UNSIGNED                                                  NOT NULL COMMENT '父id',
    `full_path`        VARCHAR(255) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT ',隔开全路径，包括自己',
    `type`             SMALLINT                                                      NOT NULL COMMENT '空间类型：1主区域、2内部区域、房间、自定义区域',
    `area`             DECIMAL(10, 2)                                                         DEFAULT NULL COMMENT '面积',
    `sort_index`       INT                                                           NOT NULL DEFAULT '0' COMMENT '排序',
    `is_deleted`       BIT(1)                                                        NOT NULL DEFAULT b'0' COMMENT '是否被删除：0未删除；1已删除',
    `create_user`      INT UNSIGNED                                                           DEFAULT NULL COMMENT '创建人',
    `create_user_name` VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '创建人姓名',
    `create_time`      DATETIME                                                               DEFAULT NULL COMMENT '创建时间',
    `update_user`      INT UNSIGNED                                                           DEFAULT NULL COMMENT '修改人',
    `update_user_name` VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '修改人姓名',
    `update_time`      DATETIME                                                               DEFAULT NULL COMMENT '修改时间',
    `own_area_id`      INT                                                                    DEFAULT NULL COMMENT '所属区域',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='空间表';

CREATE TABLE `sys_organization`
(
    `id`                   INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT COMMENT '机构id',
    `organization_name`    VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机构名称',
    `organization_type`    SMALLINT                                                      NOT NULL COMMENT '机构类型  1、企业',
    `organization_address` VARCHAR(255) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '机构地址',
    `manager_name`         VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '责任人名称',
    `manager_phone`        VARCHAR(40) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '负责人电话',
    `credit_code`          VARCHAR(64) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '机构编码',
    `entry_date`           DATE                                                                   DEFAULT NULL COMMENT '入驻时间 ',
    `remark`               VARCHAR(500) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT '' COMMENT '详述',
    `is_deleted`           BIT(1)                                                        NOT NULL DEFAULT b'0' COMMENT '是否被删除：0未删除；1已删除',
    `create_user`          INT UNSIGNED                                                           DEFAULT NULL COMMENT '创建人',
    `create_user_name`     VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '创建人姓名',
    `create_time`          DATETIME                                                               DEFAULT NULL COMMENT '创建时间',
    `update_user`          INT UNSIGNED                                                           DEFAULT NULL COMMENT '修改人',
    `update_user_name`     VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '修改人姓名',
    `update_time`          DATETIME                                                               DEFAULT NULL COMMENT '修改时间',
    `own_area_id`          INT                                                                    DEFAULT NULL COMMENT '所属区域',
    `active_org_name`      VARCHAR(100) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((IF(`is_deleted` = b'0', `organization_name`, NULL))) STORED COMMENT '活跃机构名称（未删除时等于机构名）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_active_org_name` (`active_org_name`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='机构表';

CREATE TABLE `sys_user`
(
    `id`                INT UNSIGNED                            NOT NULL AUTO_INCREMENT,
    `user_name`         VARCHAR(40) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '用户账号',
    `password`          VARCHAR(64) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '密码',
    `real_name`         VARCHAR(30) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '姓名',
    `user_phone`        VARCHAR(20) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '手机号',
    `user_gender`       SMALLINT                                NOT NULL             DEFAULT '1' COMMENT '性别：1男 2女',
    `certificates_type` SMALLINT                                NOT NULL             DEFAULT '1' COMMENT '证件类型,系统配置',
    `certificates_no`   VARCHAR(20) COLLATE utf8mb4_unicode_ci  NOT NULL             DEFAULT '' COMMENT '证件号',
    `avatar`            VARCHAR(500) COLLATE utf8mb4_unicode_ci NOT NULL             DEFAULT '' COMMENT '用户头像key',
    `remark`            VARCHAR(500) COLLATE utf8mb4_unicode_ci NOT NULL             DEFAULT '' COMMENT '备注',
    `organization_id`   INT UNSIGNED                                                 DEFAULT NULL COMMENT '机构id',
    `is_deleted`        BIT(1)                                  NOT NULL             DEFAULT b'0' COMMENT '是否被删除：0未删除；1已删除',
    `create_user`       INT UNSIGNED                                                 DEFAULT NULL COMMENT '创建人',
    `create_user_name`  VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人姓名',
    `create_time`       DATETIME                                                     DEFAULT NULL COMMENT '创建时间',
    `update_user`       INT UNSIGNED                                                 DEFAULT NULL COMMENT '修改人',
    `update_user_name`  VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '修改人姓名',
    `update_time`       DATETIME                                                     DEFAULT NULL COMMENT '修改时间',
    `active_user_name`  VARCHAR(40) GENERATED ALWAYS AS (IF(`is_deleted` = b'0', `user_name`, NULL)) STORED COMMENT '活跃用户名（未删除时等于用户名）',
    `active_user_phone` VARCHAR(20) GENERATED ALWAYS AS (IF(`is_deleted` = b'0', `user_phone`, NULL)) STORED COMMENT '活跃用户手机号（未删除时等于手机号）',
    PRIMARY KEY (`id`),
    KEY `idx_real_name` (`real_name`) USING BTREE,
    KEY `idx_user_phone` (`user_phone`) USING BTREE,
    KEY `idx_create_time` (`create_time`) USING BTREE,
    UNIQUE INDEX `uk_active_user_name` (`active_user_name`),
    UNIQUE INDEX `uk_active_user_phone` (`active_user_phone`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci;

## 超管密码 Abc123!@#
INSERT INTO sys_user
(id, user_name, password, real_name, user_phone, user_gender, certificates_type, certificates_no, avatar, remark,
 organization_id, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (1, 'admin', '$2a$10$mbHaMa9IRuGgeZ0RL/JSNer61E.84Ua8DMZabOXypvsY8iX2NVWzW', '管理员', '13919929939', 1, 1, '',
        '', '超管', NULL, 0, NULL, '', NULL, NULL, '', NULL);

CREATE TABLE sys_menu
(
    `id`               INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '菜单主键',
    `menu_name`        VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    `menu_key`         VARCHAR(64)  NOT NULL COMMENT '菜单唯一键',
    `pid`              INT          NOT NULL                                        DEFAULT 0 COMMENT '父级菜单ID（0表示顶级）',
    `sort_num`         INT          NOT NULL                                        DEFAULT 0 COMMENT '显示排序号',
    `path`             VARCHAR(128) NULL                                            DEFAULT '' COMMENT '前端访问路径',
    `menu_source`      SMALLINT(1)  NOT NULL COMMENT '1 web, 2 mobile',
    `menu_type`        SMALLINT(1)  NOT NULL COMMENT '1 菜单, 2 按钮',
    `icon`             VARCHAR(64)  NOT NULL                                        DEFAULT '' COMMENT '菜单图标',
    `remark`           VARCHAR(255) NOT NULL                                        DEFAULT '' COMMENT '菜单备注',
    `is_hidden`        BIT(1)       NOT NULL                                        DEFAULT b'0' COMMENT '是否隐藏 0否 1是',
    `is_deleted`       BIT(1)       NOT NULL                                        DEFAULT b'0' COMMENT '是否删除 0未删除 1已删除',
    `active_menu_key`  VARCHAR(64) GENERATED ALWAYS AS (IF(`is_deleted` = b'0', `menu_key`, NULL)) STORED COMMENT '有效菜单唯一键',
    `create_user`      INT UNSIGNED                                                 DEFAULT NULL COMMENT '创建人',
    `create_user_name` VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人姓名',
    `create_time`      DATETIME                                                     DEFAULT NULL COMMENT '创建时间',
    `update_user`      INT UNSIGNED                                                 DEFAULT NULL COMMENT '修改人',
    `update_user_name` VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '修改人姓名',
    `update_time`      DATETIME                                                     DEFAULT NULL COMMENT '修改时间',
    UNIQUE KEY idx_uq_menu_key (active_menu_key),
    KEY idx_sort (sort_num)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_menu_auth
(
    `id`              INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `menu_id`         INT          NOT NULL COMMENT '关联菜单ID',
    `permission_code` VARCHAR(128) NOT NULL COMMENT '接口权限标识',
    `create_time`     DATETIME DEFAULT NULL COMMENT '创建时间',
    UNIQUE KEY idx_uq_menu_auth (menu_id, permission_code)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_role
(
    `id`               INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '角色主键',
    `role_name`        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    `role_key`         VARCHAR(64)  NOT NULL COMMENT '角色唯一标识',
    `sort_num`         INT          NOT NULL                                        DEFAULT 0 COMMENT '显示排序号',
    `remark`           VARCHAR(255) NOT NULL                                        DEFAULT '' COMMENT '角色备注',
    `is_system`        BIT(1)       NOT NULL                                        DEFAULT b'0' COMMENT '是否系统内置 0否 1是',
    `is_disabled`      BIT(1)       NOT NULL                                        DEFAULT b'0' COMMENT '是否禁用 0否 1是',
    `is_deleted`       BIT(1)       NOT NULL                                        DEFAULT b'0' COMMENT '是否删除 0未删除 1已删除',
    `active_role_key`  VARCHAR(64) GENERATED ALWAYS AS (IF(`is_deleted` = b'0', `role_key`, NULL)) STORED COMMENT '有效角色标识',
    `create_user`      INT UNSIGNED                                                 DEFAULT NULL COMMENT '创建人',
    `create_user_name` VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人姓名',
    `create_time`      DATETIME                                                     DEFAULT NULL COMMENT '创建时间',
    `update_user`      INT UNSIGNED                                                 DEFAULT NULL COMMENT '修改人',
    `update_user_name` VARCHAR(20) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '修改人姓名',
    `update_time`      DATETIME                                                     DEFAULT NULL COMMENT '修改时间',
    UNIQUE KEY idx_uq_role_key (active_role_key),
    KEY idx_sort (sort_num)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO sys_role
(id, role_name, role_key, sort_num, remark, is_system, is_disabled, is_deleted, create_user, create_user_name,
 create_time, update_user, update_user_name, update_time)
VALUES (1, '超级管理', 'super_admin', 0, '', 1, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);

CREATE TABLE sys_role_menu
(
    `id`          INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `role_id`     INT NOT NULL COMMENT '角色ID',
    `menu_id`     INT NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    UNIQUE KEY idx_uq_role_menu (role_id, menu_id),
    KEY idx_menu_id (menu_id)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_menu_path
(
    `id`            INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `ancestor_id`   INT      NOT NULL COMMENT '祖先菜单ID',
    `descendant_id` INT      NOT NULL COMMENT '后代菜单ID',
    `depth`         SMALLINT NOT NULL COMMENT '祖先到后代的距离（自环为 0）',
    UNIQUE KEY uk_closure (ancestor_id, descendant_id),
    KEY idx_descendant (descendant_id),
    KEY idx_depth (depth)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sys_user_role
(
    `id`          INT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     INT UNSIGNED NOT NULL COMMENT '用户ID',
    `role_id`     INT UNSIGNED NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    UNIQUE KEY idx_uq_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO sys_user_role
    (id, user_id, role_id, create_time)
VALUES (1, 1, 1, NULL);
