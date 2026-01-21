CREATE TABLE `iot_device`
(
    `id`             INT                                                           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_no`      VARCHAR(100) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备编号',
    `port_no`        INT                                                                    DEFAULT NULL COMMENT '串口号',
    `meter_address`  INT                                                                    DEFAULT NULL COMMENT '电表通讯地址',
    `device_secret`  VARCHAR(128) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '设备密钥',
    `slave_address`  INT                                                           NOT NULL DEFAULT 0 COMMENT 'Modbus 从站地址',
    `product_code`   VARCHAR(50) CHARACTER SET UTF8MB4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '产品编码',
    `parent_id`      INT                                                                    DEFAULT NULL COMMENT '父设备ID(网关ID)',
    `last_online_at` DATETIME                                                               DEFAULT NULL COMMENT '最近在线时间',
    `create_time`    DATETIME                                                               DEFAULT NULL,
    `update_time`    DATETIME                                                               DEFAULT NULL,
    `is_deleted`     BIT(1)                                                        NOT NULL DEFAULT 0 COMMENT '0正常，1删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_iot_device_no` (`device_no`),
    UNIQUE KEY `uk_iot_device_parent_child` (`parent_id`, `port_no`, `meter_address`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8MB4
  COLLATE = utf8mb4_unicode_ci COMMENT ='IoT 设备表';
