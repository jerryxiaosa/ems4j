-- H2数据库初始化脚本
-- 基于ems.sql转换而来，适用于Spring Boot集成测试

-- 能耗账户表
DROP TABLE IF EXISTS energy_account;
CREATE TABLE energy_account
(
    id                     INTEGER NOT NULL AUTO_INCREMENT,
    owner_id               INTEGER          DEFAULT NULL,
    owner_type             SMALLINT         DEFAULT NULL,
    owner_name             VARCHAR(50)      DEFAULT NULL,
    contact_name           VARCHAR(50)      DEFAULT NULL,
    contact_phone          VARCHAR(40)      DEFAULT NULL,
    electric_account_type  SMALLINT         DEFAULT NULL,
    monthly_pay_amount     DECIMAL(20, 8)   DEFAULT NULL,
    electric_price_plan_id INTEGER          DEFAULT NULL,
    warn_plan_id           INTEGER          DEFAULT NULL,
    electric_warn_type     VARCHAR(255)     DEFAULT NULL,
    create_user            INTEGER          DEFAULT NULL,
    create_user_name       VARCHAR(50)      DEFAULT NULL,
    create_time            TIMESTAMP        DEFAULT NULL,
    update_user            INTEGER          DEFAULT NULL,
    update_user_name       VARCHAR(50)      DEFAULT NULL,
    update_time            TIMESTAMP        DEFAULT NULL,
    is_deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    delete_time            TIMESTAMP        DEFAULT NULL,
    PRIMARY KEY (id)
);

-- 主体空间租赁关系表
DROP TABLE IF EXISTS energy_owner_space_rel;
CREATE TABLE energy_owner_space_rel
(
    id               INTEGER NOT NULL AUTO_INCREMENT,
    owner_type       SMALLINT NOT NULL,
    owner_id         INTEGER NOT NULL,
    space_id         INTEGER NOT NULL,
    create_user      INTEGER          DEFAULT NULL,
    create_user_name VARCHAR(50)      DEFAULT NULL,
    create_time      TIMESTAMP        DEFAULT NULL,
    update_user      INTEGER          DEFAULT NULL,
    update_user_name VARCHAR(50)      DEFAULT NULL,
    update_time      TIMESTAMP        DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_space_id ON energy_owner_space_rel (space_id);
CREATE INDEX idx_owner_type_owner_id_owner_space_rel ON energy_owner_space_rel (owner_type, owner_id);

-- 开户时刻表数据记录
DROP TABLE IF EXISTS energy_open_meter_record;
CREATE TABLE energy_open_meter_record
(
    id               INTEGER  NOT NULL AUTO_INCREMENT,
    account_id       INTEGER  NOT NULL,
    meter_type       SMALLINT NOT NULL,
    meter_id         INTEGER  NOT NULL,
    power            DECIMAL(12, 2)    DEFAULT NULL,
    power_higher     DECIMAL(12, 2)    DEFAULT NULL,
    power_high       DECIMAL(12, 2)    DEFAULT NULL,
    power_low        DECIMAL(12, 2)    DEFAULT NULL,
    power_lower      DECIMAL(12, 2)    DEFAULT NULL,
    power_deep_low   DECIMAL(12, 2)    DEFAULT NULL,
    show_time        TIMESTAMP         DEFAULT NULL,
    create_user      INTEGER           DEFAULT NULL,
    create_user_name VARCHAR(50)       DEFAULT NULL,
    create_time      TIMESTAMP         DEFAULT NULL,
    update_user      INTEGER           DEFAULT NULL,
    update_user_name VARCHAR(50)       DEFAULT NULL,
    update_time      TIMESTAMP         DEFAULT NULL,
    is_deleted       BOOLEAN  NOT NULL DEFAULT FALSE,
    delete_time      TIMESTAMP         DEFAULT NULL,
    PRIMARY KEY (id)
);

-- 能耗账户流水表
DROP TABLE IF EXISTS energy_account_order_flow;
CREATE TABLE energy_account_order_flow
(
    id                  INTEGER        NOT NULL AUTO_INCREMENT,
    consume_id          VARCHAR(255)   NOT NULL,
    account_id          INTEGER        NOT NULL,
    balance_relation_id INTEGER        NOT NULL,
    balance_type        SMALLINT       NOT NULL,
    amount              DECIMAL(20, 8) NOT NULL,
    create_time         TIMESTAMP      NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_consume_id ON energy_account_order_flow (consume_id);
CREATE INDEX idx_create_time ON energy_account_order_flow (create_time);
CREATE INDEX idx_account_id ON energy_account_order_flow (account_id);

-- 阶梯开始记录表
DROP TABLE IF EXISTS energy_account_meter_step;
CREATE TABLE energy_account_meter_step
(
    id                   INTEGER        NOT NULL AUTO_INCREMENT,
    account_id           INTEGER        NOT NULL,
    meter_type           SMALLINT                DEFAULT NULL,
    meter_id             INTEGER        NOT NULL,
    step_start_value     DECIMAL(12, 2) NOT NULL DEFAULT 0,
    history_power_offset DECIMAL(12, 2) NOT NULL DEFAULT 0,
    current_year         INTEGER                 DEFAULT NULL,
    is_latest           BOOLEAN        NOT NULL DEFAULT TRUE,
    create_user          INTEGER                 DEFAULT NULL,
    create_user_name     VARCHAR(50)             DEFAULT NULL,
    create_time          TIMESTAMP               DEFAULT NULL,
    update_user          INTEGER                 DEFAULT NULL,
    update_user_name     VARCHAR(50)             DEFAULT NULL,
    update_time          TIMESTAMP               DEFAULT NULL,
    is_deleted           BOOLEAN        NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

-- 告警方案表
DROP TABLE IF EXISTS energy_warn_plan;
CREATE TABLE energy_warn_plan
(
    id               INTEGER NOT NULL AUTO_INCREMENT,
    name             VARCHAR(50)      DEFAULT NULL,
    first_level      DECIMAL(20, 8)   DEFAULT NULL,
    second_level     DECIMAL(20, 8)   DEFAULT NULL,
    auto_close       BOOLEAN          DEFAULT NULL,
    remark           VARCHAR(500)     DEFAULT NULL,
    create_user      INTEGER          DEFAULT NULL,
    create_user_name VARCHAR(50)      DEFAULT NULL,
    create_time      TIMESTAMP        DEFAULT NULL,
    update_user      INTEGER          DEFAULT NULL,
    update_user_name VARCHAR(50)      DEFAULT NULL,
    update_time      TIMESTAMP        DEFAULT NULL,
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

-- 电价格方案表
DROP TABLE IF EXISTS energy_electric_price_plan;
CREATE TABLE energy_electric_price_plan
(
    id                      INTEGER        NOT NULL AUTO_INCREMENT,
    name                    VARCHAR(50)    NOT NULL,
    price_higher            DECIMAL(20, 8) NOT NULL,
    price_high              DECIMAL(20, 8) NOT NULL,
    price_low               DECIMAL(20, 8) NOT NULL,
    price_lower             DECIMAL(20, 8) NOT NULL,
    price_deep_low          DECIMAL(20, 8) NOT NULL DEFAULT 0.00000000,
    is_step                 BOOLEAN                 DEFAULT NULL,
    step_price              VARCHAR(1000)           DEFAULT NULL,
    is_custom_price         BOOLEAN                 DEFAULT FALSE,
    price_higher_base       DECIMAL(20, 8)          DEFAULT NULL,
    price_high_base         DECIMAL(20, 8)          DEFAULT NULL,
    price_low_base          DECIMAL(20, 8)          DEFAULT NULL,
    price_lower_base        DECIMAL(20, 8)          DEFAULT NULL,
    price_deep_low_base     DECIMAL(20, 8)          DEFAULT 0.00000000,
    price_higher_multiply   DECIMAL(20, 8)          DEFAULT NULL,
    price_high_multiply     DECIMAL(20, 8)          DEFAULT NULL,
    price_low_multiply      DECIMAL(20, 8)          DEFAULT NULL,
    price_lower_multiply    DECIMAL(20, 8)          DEFAULT NULL,
    price_deep_low_multiply DECIMAL(20, 8)          DEFAULT 0.00000000,
    create_user             INTEGER                 DEFAULT NULL,
    create_user_name        VARCHAR(50)             DEFAULT NULL,
    create_time             TIMESTAMP               DEFAULT NULL,
    update_user             INTEGER                 DEFAULT NULL,
    update_user_name        VARCHAR(50)             DEFAULT NULL,
    update_time             TIMESTAMP               DEFAULT NULL,
    is_deleted              BOOLEAN        NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

-- 电表
DROP TABLE IF EXISTS energy_electric_meter;
CREATE TABLE energy_electric_meter
(
    id                INTEGER     NOT NULL AUTO_INCREMENT,
    space_id          INTEGER     NOT NULL,
    meter_name        VARCHAR(50) NOT NULL,
    meter_no          VARCHAR(50) NOT NULL,
    device_no         VARCHAR(100) NOT NULL,
    model_id          INTEGER     NOT NULL,
    product_code       VARCHAR(50) NOT NULL,
    communicate_model VARCHAR(50) NOT NULL,
    gateway_id        INTEGER              DEFAULT NULL,
    port_no           INTEGER              DEFAULT NULL,
    meter_address     INTEGER              DEFAULT NULL,
    imei              VARCHAR(50)          DEFAULT NULL,
    is_online         BOOLEAN              DEFAULT NULL,
    last_online_time  TIMESTAMP            DEFAULT NULL,
    is_cut_off        BOOLEAN     NOT NULL DEFAULT FALSE,
    remark            VARCHAR(500)         DEFAULT NULL,
    iot_id            VARCHAR(100)         DEFAULT NULL,
    is_calculate      BOOLEAN     NOT NULL DEFAULT FALSE,
    calculate_type    INTEGER              DEFAULT NULL,
    is_prepay         BOOLEAN     NOT NULL DEFAULT FALSE,
    protected_model   BOOLEAN              DEFAULT NULL,
    price_plan_id     INTEGER              DEFAULT NULL,
    warn_plan_id      INTEGER              DEFAULT NULL,
    warn_type         VARCHAR(50)          DEFAULT NULL,
    account_id        INTEGER              DEFAULT NULL,
    ct                INTEGER              DEFAULT NULL,
    create_user       INTEGER              DEFAULT NULL,
    create_user_name  VARCHAR(50)          DEFAULT NULL,
    create_time       TIMESTAMP            DEFAULT NULL,
    update_user       INTEGER              DEFAULT NULL,
    update_user_name  VARCHAR(50)          DEFAULT NULL,
    update_time       TIMESTAMP            DEFAULT NULL,
    is_deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    own_area_id       INTEGER              DEFAULT NULL,
    active_device_no  VARCHAR(100) GENERATED ALWAYS AS (CASE WHEN is_deleted = FALSE THEN device_no ELSE NULL END),
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_energy_electric_meter_active_device_no ON energy_electric_meter (active_device_no);
CREATE INDEX idx_energy_electric_meter_iot_id ON energy_electric_meter (iot_id);

-- 智能网关
DROP TABLE IF EXISTS energy_gateway;
CREATE TABLE energy_gateway
(
    id                INTEGER NOT NULL AUTO_INCREMENT,
    space_id          int              DEFAULT NULL,
    gateway_name      VARCHAR(50)      DEFAULT NULL,
    gateway_no        VARCHAR(50)      DEFAULT NULL,
    device_no         VARCHAR(100)     NOT NULL,
    model_id          INTEGER          DEFAULT NULL,
    product_code       VARCHAR(50)      DEFAULT NULL,
    communicate_model varchar(100)     DEFAULT NULL,
    sn                varchar(50)      DEFAULT NULL,
    imei              VARCHAR(50)      DEFAULT NULL,
    is_online         BOOLEAN          DEFAULT NULL,
    config_info       text,
    remark            varchar(500),
    iot_id            VARCHAR(100)     DEFAULT NULL,
    create_user       INTEGER          DEFAULT NULL,
    create_user_name  VARCHAR(50)      DEFAULT NULL,
    create_time       TIMESTAMP        DEFAULT NULL,
    update_user       INTEGER          DEFAULT NULL,
    update_user_name  VARCHAR(50)      DEFAULT NULL,
    update_time       TIMESTAMP        DEFAULT NULL,
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    own_area_id       INTEGER          DEFAULT NULL,
    active_device_no  VARCHAR(100) GENERATED ALWAYS AS (CASE WHEN is_deleted = FALSE THEN device_no ELSE NULL END),
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_energy_gateway_active_device_no ON energy_gateway (active_device_no);
CREATE INDEX idx_energy_gateway_iot_id ON energy_gateway (iot_id);

-- 智能设备品类表
DROP TABLE IF EXISTS device_type;
CREATE TABLE device_type
(
    id               INTEGER     NOT NULL AUTO_INCREMENT,
    pid              int         NOT NULL COMMENT '父级id',
    ancestor_id      varchar(50) NOT NULL COMMENT '完整的祖级id，不包括自身',
    type_name        VARCHAR(50) NOT NULL,
    type_key         VARCHAR(50) NOT NULL,
    level            smallint    NOT NULL COMMENT '层级',
    create_user      INTEGER              DEFAULT NULL,
    create_user_name VARCHAR(50)          DEFAULT NULL,
    create_time      TIMESTAMP            DEFAULT NULL,
    update_user      INTEGER              DEFAULT NULL,
    update_user_name VARCHAR(50)          DEFAULT NULL,
    update_time      TIMESTAMP            DEFAULT NULL,
    is_deleted       BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_type_key ON device_type (type_key);

-- 智能设备厂商型号
DROP TABLE IF EXISTS device_model;
CREATE TABLE device_model
(
    id                INTEGER      NOT NULL AUTO_INCREMENT,
    type_id           INTEGER      NOT NULL,
    type_key          VARCHAR(50)  NOT NULL,
    manufacturer_name VARCHAR(100) NOT NULL,
    model_name        VARCHAR(100) NOT NULL,
    product_code       VARCHAR(50)  NOT NULL,
    model_property    TEXT         NOT NULL,
    create_user       INTEGER               DEFAULT NULL,
    create_user_name  VARCHAR(50)           DEFAULT NULL,
    create_time       TIMESTAMP             DEFAULT NULL,
    update_user       INTEGER               DEFAULT NULL,
    update_user_name  VARCHAR(50)           DEFAULT NULL,
    update_time       TIMESTAMP             DEFAULT NULL,
    is_deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_device_model_code ON device_model (product_code);

-- 设备命令记录
DROP TABLE IF EXISTS device_command_record;
CREATE TABLE device_command_record
(
    id                INTEGER       NOT NULL AUTO_INCREMENT,
    device_type_key   VARCHAR(100)  NOT NULL,
    device_id         INTEGER       NOT NULL,
    device_name       VARCHAR(100)  NOT NULL,
    device_no         VARCHAR(100)  NOT NULL,
    device_iot_id     VARCHAR(100)  NOT NULL,
    space_id          INTEGER       NOT NULL,
    space_name        VARCHAR(100)  NOT NULL,
    account_id        INTEGER                DEFAULT NULL,
    command_type      SMALLINT      NOT NULL,
    command_source    SMALLINT      NOT NULL,
    command_data      VARCHAR(5000) NOT NULL DEFAULT '',
    success           BOOLEAN       NOT NULL DEFAULT FALSE,
    success_time      TIMESTAMP              DEFAULT NULL,
    last_execute_time TIMESTAMP              DEFAULT NULL,
    ensure_success    BOOLEAN       NOT NULL DEFAULT FALSE,
    execute_times     SMALLINT      NOT NULL DEFAULT 0,
    remark            VARCHAR(500)           DEFAULT NULL,
    create_user       INTEGER                DEFAULT NULL,
    create_user_name  VARCHAR(50)            DEFAULT NULL,
    create_time       TIMESTAMP              DEFAULT NULL,
    update_user       INTEGER                DEFAULT NULL,
    update_user_name  VARCHAR(50)            DEFAULT NULL,
    update_time       TIMESTAMP              DEFAULT NULL,
    is_deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
    own_area_id       INTEGER                DEFAULT NULL,
    PRIMARY KEY (id)
);

-- 设备命令执行记录
DROP TABLE IF EXISTS device_command_execute_record;
CREATE TABLE device_command_execute_record
(
    id               INTEGER      NOT NULL AUTO_INCREMENT,
    command_id       INTEGER      NOT NULL,
    success          BOOLEAN      NOT NULL DEFAULT FALSE,
    reason           VARCHAR(500) NOT NULL DEFAULT '',
    execute_time     TIMESTAMP             DEFAULT NULL,
    command_source   SMALLINT              DEFAULT 0,
    create_user      INTEGER               DEFAULT NULL,
    create_user_name VARCHAR(50)           DEFAULT NULL,
    create_time      TIMESTAMP             DEFAULT NULL,
    update_user      INTEGER               DEFAULT NULL,
    update_user_name VARCHAR(50)           DEFAULT NULL,
    update_time      TIMESTAMP             DEFAULT NULL,
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_command_id ON device_command_execute_record (command_id);

-- 电表电量数据记录表
DROP TABLE IF EXISTS energy_electric_meter_power_record;
CREATE TABLE energy_electric_meter_power_record
(
    id                 INT     NOT NULL AUTO_INCREMENT,
    meter_id           INT     NOT NULL,
    meter_name         VARCHAR(50)      DEFAULT NULL COMMENT '电表名称',
    meter_no           VARCHAR(50)      DEFAULT NULL COMMENT '电表编号',
    account_id         INT              DEFAULT NULL COMMENT '账户id',
    is_prepay          BOOLEAN          DEFAULT NULL COMMENT '是否预付费电表：0不是预付费，1是预付费',
    ct                 INTEGER          DEFAULT NULL,
    power              DECIMAL(12, 2)   DEFAULT NULL COMMENT '电表度数',
    power_higher       DECIMAL(12, 2)   DEFAULT NULL COMMENT '尖用电量',
    power_high         DECIMAL(12, 2)   DEFAULT NULL COMMENT '峰用电量',
    power_low          DECIMAL(12, 2)   DEFAULT NULL COMMENT '平用电量',
    power_lower        DECIMAL(12, 2)   DEFAULT NULL COMMENT '谷用电量',
    power_deep_low     DECIMAL(12, 2)   DEFAULT NULL COMMENT '深谷用电量',
    original_report_id VARCHAR(70)      DEFAULT NULL COMMENT '电表上报数据记录id',
    record_time        TIMESTAMP        DEFAULT NULL COMMENT '抄表时间',
    create_time        TIMESTAMP        DEFAULT NULL,
    is_deleted         BOOLEAN NOT NULL DEFAULT FALSE COMMENT '0正常，1删除',
    PRIMARY KEY (id)
);

CREATE INDEX idx_record_time ON energy_electric_meter_power_record (record_time);
CREATE INDEX idx_create_time_power_record ON energy_electric_meter_power_record (create_time);

-- 电表电量关联信息表
DROP TABLE IF EXISTS energy_electric_meter_power_relation;
CREATE TABLE energy_electric_meter_power_relation
(
    id                    INT     NOT NULL AUTO_INCREMENT,
    record_id             INT     NOT NULL COMMENT '电表电量数据记录关联id',
    meter_id              INT     NOT NULL,
    is_calculate          BOOLEAN          DEFAULT NULL COMMENT '汇总时是否计算在内，不表示calculate_type的开关',
    calculate_type        INT              DEFAULT NULL COMMENT '用量类型，和is_calculate没关联',
    calculate_type_name   VARCHAR(50)      DEFAULT NULL COMMENT '用量类型，和is_calculate没关联',
    space_id              INT              DEFAULT NULL COMMENT '空间id',
    space_name            VARCHAR(100)     DEFAULT NULL COMMENT '空间名称',
    space_parent_ids      VARCHAR(200)     DEFAULT NULL COMMENT '父级id',
    space_parent_names    VARCHAR(1000)    DEFAULT NULL COMMENT '父级名称',
    account_id            INT              DEFAULT NULL COMMENT '账户id',
    electric_account_type INT              DEFAULT NULL COMMENT '电费账户类型：0按需、1包月、2合并',
    owner_id              INT              DEFAULT NULL COMMENT '所有人id',
    owner_type            SMALLINT         DEFAULT NULL COMMENT '所有人类型：0企业，1个人',
    owner_name            VARCHAR(50)      DEFAULT NULL COMMENT '所有名称',
    record_time           TIMESTAMP        DEFAULT NULL COMMENT '抄表时间',
    create_time           TIMESTAMP        DEFAULT NULL,
    is_deleted            BOOLEAN NOT NULL DEFAULT FALSE COMMENT '0正常，1删除',
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_record_id_unique ON energy_electric_meter_power_relation (record_id);

-- 账号销户表
DROP TABLE IF EXISTS energy_account_cancel_record;
CREATE TABLE energy_account_cancel_record
(
    id                    INTEGER NOT NULL AUTO_INCREMENT,
    cancel_no             VARCHAR(50)      DEFAULT NULL,
    account_id            INTEGER          DEFAULT NULL,
    owner_id              INTEGER          DEFAULT NULL,
    owner_type            SMALLINT         DEFAULT NULL,
    owner_name            VARCHAR(50)      DEFAULT NULL,
    electric_account_type SMALLINT         DEFAULT NULL,
    electric_meter_amount INTEGER          DEFAULT NULL,
    full_cancel           BOOLEAN NOT NULL DEFAULT FALSE,
    clean_balance_type    SMALLINT         DEFAULT NULL,
    clean_balance_real    DECIMAL(12, 2)   DEFAULT NULL,
    clean_balance_ignore  DECIMAL(10, 8)   DEFAULT NULL,
    remark                VARCHAR(500)     DEFAULT NULL,
    create_user           INTEGER          DEFAULT NULL,
    create_user_name      VARCHAR(50)      DEFAULT NULL,
    create_time           TIMESTAMP        DEFAULT NULL,
    update_user           INTEGER          DEFAULT NULL,
    update_user_name      VARCHAR(50)      DEFAULT NULL,
    update_time           TIMESTAMP        DEFAULT NULL,
    is_deleted            BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

-- 销表信息表
DROP TABLE IF EXISTS energy_meter_cancel_record;
CREATE TABLE energy_meter_cancel_record
(
    id                  INTEGER        NOT NULL AUTO_INCREMENT,
    cancel_no           VARCHAR(50)             DEFAULT NULL,
    account_id          INTEGER        NOT NULL,
    meter_type          SMALLINT       NOT NULL,
    meter_id            INTEGER        NOT NULL,
    meter_name          VARCHAR(50)    NOT NULL,
    meter_no            VARCHAR(50)    NOT NULL,
    space_id            INTEGER                 DEFAULT NULL,
    space_name          VARCHAR(100)            DEFAULT NULL,
    space_parent_ids    VARCHAR(200)            DEFAULT NULL,
    space_parent_names  VARCHAR(1000)           DEFAULT NULL,
    is_online           BOOLEAN                 DEFAULT NULL,
    is_cut_off          BOOLEAN                 DEFAULT NULL,
    balance             DECIMAL(12, 2)          DEFAULT NULL,
    power               DECIMAL(12, 2)          DEFAULT NULL,
    power_higher        DECIMAL(12, 2)          DEFAULT NULL,
    power_high          DECIMAL(12, 2)          DEFAULT NULL,
    power_low           DECIMAL(12, 2)          DEFAULT NULL,
    power_lower         DECIMAL(12, 2)          DEFAULT NULL,
    power_deep_low      DECIMAL(12, 2)          DEFAULT NULL,
    history_power_total DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '销户时累计总用电量',
    show_time           TIMESTAMP      NOT NULL,
    create_user         INTEGER                 DEFAULT NULL,
    create_user_name    VARCHAR(50)             DEFAULT NULL,
    create_time         TIMESTAMP               DEFAULT NULL,
    update_user         INTEGER                 DEFAULT NULL,
    update_user_name    VARCHAR(50)             DEFAULT NULL,
    update_time         TIMESTAMP               DEFAULT NULL,
    is_deleted          BOOLEAN        NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_cancel_no ON energy_meter_cancel_record (cancel_no);

-- 电表用电消耗数据表
DROP TABLE IF EXISTS energy_electric_meter_power_consume_record;
CREATE TABLE energy_electric_meter_power_consume_record
(
    id                     INT       NOT NULL AUTO_INCREMENT,
    meter_id               INT       NOT NULL,
    is_calculate           BOOLEAN        DEFAULT NULL COMMENT '汇总时是否计算在内，不表示calculate_type的开关',
    calculate_type         INT            DEFAULT NULL COMMENT '用量类型，和is_calculate没关联',
    account_id             INT            DEFAULT NULL COMMENT '账户id',
    space_id               INT            DEFAULT NULL COMMENT '空间id',
    begin_record_id        INT       NOT NULL COMMENT '起始电表记录id',
    begin_power            DECIMAL(12, 2) DEFAULT NULL,
    begin_power_higher     DECIMAL(12, 2) DEFAULT NULL,
    begin_power_high       DECIMAL(12, 2) DEFAULT NULL,
    begin_power_low        DECIMAL(12, 2) DEFAULT NULL,
    begin_power_lower      DECIMAL(12, 2) DEFAULT NULL,
    begin_power_deep_low   DECIMAL(12, 2) DEFAULT NULL,
    begin_record_time      TIMESTAMP      DEFAULT NULL COMMENT '开始记录抄表时间',
    end_record_id          INT       NOT NULL COMMENT '截止电表记录id',
    end_power              DECIMAL(12, 2) DEFAULT NULL,
    end_power_higher       DECIMAL(12, 2) DEFAULT NULL,
    end_power_high         DECIMAL(12, 2) DEFAULT NULL,
    end_power_low          DECIMAL(12, 2) DEFAULT NULL,
    end_power_lower        DECIMAL(12, 2) DEFAULT NULL,
    end_power_deep_low     DECIMAL(12, 2) DEFAULT NULL,
    end_record_time        TIMESTAMP      DEFAULT NULL COMMENT '截止记录抄表时间',
    consume_power          DECIMAL(12, 2) DEFAULT NULL COMMENT '总消耗电量',
    consume_power_higher   DECIMAL(12, 2) DEFAULT NULL COMMENT '尖用电量消耗电量',
    consume_power_high     DECIMAL(12, 2) DEFAULT NULL COMMENT '峰用电量消耗电量',
    consume_power_low      DECIMAL(12, 2) DEFAULT NULL COMMENT '平用电量消耗电量',
    consume_power_lower    DECIMAL(12, 2) DEFAULT NULL COMMENT '谷用电量消耗电量',
    consume_power_deep_low DECIMAL(12, 2) DEFAULT NULL COMMENT '深谷用电量消耗电量',
    meter_consume_time     TIMESTAMP NOT NULL COMMENT '消费时间',
    create_time            TIMESTAMP NOT NULL COMMENT '平台处理时间',
    is_deleted             BOOLEAN        DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_meter_id_consume ON energy_electric_meter_power_consume_record (meter_id);
CREATE INDEX idx_create_time_consume ON energy_electric_meter_power_consume_record (create_time);
CREATE INDEX idx_meter_consume_time ON energy_electric_meter_power_consume_record (meter_consume_time);

-- 能耗余额消费记录表
DROP TABLE IF EXISTS energy_electric_meter_balance_consume_record;
CREATE TABLE energy_electric_meter_balance_consume_record
(
    id                      INT         NOT NULL AUTO_INCREMENT,
    meter_consume_record_id INT            DEFAULT NULL COMMENT '水电表消耗记录id',
    consume_no              VARCHAR(50) NOT NULL COMMENT '消费单号',
    consume_type            SMALLINT       DEFAULT NULL COMMENT '扣费类型，1账户2表',
    meter_type              SMALLINT       DEFAULT NULL COMMENT '表类型：1电、2水',
    account_id              INT         NOT NULL COMMENT '账户id',
    electric_account_type   SMALLINT       DEFAULT NULL COMMENT '电费账户类型：0按需、1包月、2合并',
    owner_id                INT            DEFAULT NULL COMMENT '所有人id',
    owner_type              SMALLINT       DEFAULT NULL COMMENT '所有人类型：0企业，1个人',
    owner_name              VARCHAR(50)    DEFAULT NULL COMMENT '所有人名称',
    meter_id                INT            DEFAULT NULL COMMENT '表id',
    meter_name              VARCHAR(50)    DEFAULT NULL COMMENT '电表名称',
    meter_no                VARCHAR(50)    DEFAULT NULL COMMENT '电表编号',
    space_id                INT            DEFAULT NULL COMMENT '空间id',
    space_name              VARCHAR(100)   DEFAULT NULL COMMENT '空间名称',
    price_plan_id           INT            DEFAULT NULL COMMENT '计费方案id',
    price_plan_name         VARCHAR(50)    DEFAULT NULL COMMENT '计费方案名称',
    step_start_value        DECIMAL(12, 2) DEFAULT NULL COMMENT '年度阶梯起始值',
    history_power_offset    DECIMAL(12, 2) DEFAULT NULL COMMENT '销户时累计总用电量',
    step_rate               DECIMAL(20, 8) DEFAULT NULL COMMENT '阶梯倍率',
    consume_amount          DECIMAL(20, 8) DEFAULT NULL COMMENT '总消耗金额',
    consume_amount_higher   DECIMAL(20, 8) DEFAULT NULL COMMENT '尖用电量消耗金额',
    consume_amount_high     DECIMAL(20, 8) DEFAULT NULL COMMENT '峰用电量消耗金额',
    consume_amount_low      DECIMAL(20, 8) DEFAULT NULL COMMENT '平用电量消耗金额',
    consume_amount_lower    DECIMAL(20, 8) DEFAULT NULL COMMENT '谷用电量消耗金额',
    consume_amount_deep_low DECIMAL(20, 8) DEFAULT 0.00000000 COMMENT '深谷用电量消耗金额',
    price_higher            DECIMAL(20, 8) DEFAULT NULL COMMENT '尖单价',
    price_high              DECIMAL(20, 8) DEFAULT NULL COMMENT '峰单价',
    price_low               DECIMAL(20, 8) DEFAULT NULL COMMENT '平单价',
    price_lower             DECIMAL(20, 8) DEFAULT NULL COMMENT '谷单价',
    price_deep_low          DECIMAL(20, 8) DEFAULT NULL COMMENT '深谷单价',
    begin_balance           DECIMAL(20, 8) DEFAULT NULL COMMENT '起始余额',
    end_balance             DECIMAL(20, 8) DEFAULT NULL COMMENT '截止余额',
    remark                  VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    meter_consume_time      TIMESTAMP      DEFAULT NULL COMMENT '消费时间',
    create_time             TIMESTAMP   NOT NULL COMMENT '平台处理时间',
    is_deleted              BOOLEAN        DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_meter_consume_record_id ON energy_electric_meter_balance_consume_record (meter_consume_record_id);
CREATE INDEX idx_create_time_balance ON energy_electric_meter_balance_consume_record (create_time);
CREATE INDEX idx_meter_id_balance ON energy_electric_meter_balance_consume_record (meter_id);
CREATE INDEX idx_account_id_balance ON energy_electric_meter_balance_consume_record (account_id);

-- 系统配置表
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config
(
    id                 INTEGER      NOT NULL AUTO_INCREMENT,
    config_module_name VARCHAR(50)  NOT NULL,
    config_key         VARCHAR(50)  NOT NULL,
    config_name        VARCHAR(100) NOT NULL,
    config_value       VARCHAR(500) NOT NULL,
    is_system          BOOLEAN      NOT NULL DEFAULT FALSE,
    is_deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    `create_user`      int                   DEFAULT NULL COMMENT '创建人',
    `create_user_name` varchar(20)           DEFAULT '' COMMENT '创建人姓名',
    `create_time`      datetime              DEFAULT NULL COMMENT '创建时间',
    `update_user`      int                   DEFAULT NULL COMMENT '修改人',
    `update_user_name` varchar(20)           DEFAULT '' COMMENT '修改人姓名',
    `update_time`      datetime              DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (id)
);

-- 能耗余额表
DROP TABLE IF EXISTS energy_account_balance;
CREATE TABLE energy_account_balance
(
    id                  INTEGER        NOT NULL AUTO_INCREMENT,
    balance_relation_id INTEGER        NOT NULL,
    balance_type        SMALLINT       NOT NULL,
    balance             DECIMAL(20, 8) NOT NULL DEFAULT 0.00000000,
    account_id          INTEGER        NOT NULL,
    is_deleted          BOOLEAN        NOT NULL DEFAULT FALSE,
    active_balance_key  VARCHAR(64) GENERATED ALWAYS AS (
        CASE WHEN is_deleted = FALSE THEN CAST(balance_relation_id AS VARCHAR) || '_' || CAST(balance_type AS VARCHAR) ELSE NULL END
    ),
    PRIMARY KEY (id)
);

CREATE INDEX idx_balance_id ON energy_account_balance (balance_relation_id, balance_type);
CREATE INDEX idx_account_id_is_deleted_balance ON energy_account_balance (account_id, is_deleted);
CREATE UNIQUE INDEX uk_energy_account_balance_active_key ON energy_account_balance (active_balance_key);
CREATE UNIQUE INDEX config_key ON sys_config (config_key);

-- 能耗包月消费记录表
DROP TABLE IF EXISTS energy_account_balance_consume_record;
CREATE TABLE energy_account_balance_consume_record
(
    id            INTEGER     NOT NULL AUTO_INCREMENT,
    consume_no    VARCHAR(50) NOT NULL COMMENT '消费单号',
    consume_type  INTEGER              DEFAULT NULL COMMENT '消费类型',
    account_id    INTEGER     NOT NULL COMMENT '账户ID',
    owner_id      INTEGER              DEFAULT NULL COMMENT '所有者ID',
    owner_type    INTEGER              DEFAULT NULL COMMENT '所有者类型',
    owner_name    VARCHAR(50)          DEFAULT NULL COMMENT '所有者名称',
    pay_amount    DECIMAL(20, 8)       DEFAULT NULL COMMENT '支付金额',
    begin_balance DECIMAL(20, 8)       DEFAULT NULL COMMENT '期初余额',
    end_balance   DECIMAL(20, 8)       DEFAULT NULL COMMENT '期末余额',
    remark        VARCHAR(500)         DEFAULT NULL COMMENT '备注',
    consume_time  TIMESTAMP            DEFAULT NULL COMMENT '消费时间',
    create_time   TIMESTAMP   NOT NULL COMMENT '创建时间',
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '是否删除',
    PRIMARY KEY (id)
);

CREATE INDEX idx_consume_no_account_balance ON energy_account_balance_consume_record (consume_no);
CREATE INDEX idx_account_id_account_balance ON energy_account_balance_consume_record (account_id);
CREATE INDEX idx_create_time_account_balance ON energy_account_balance_consume_record (create_time);
CREATE INDEX idx_consume_time_account_balance ON energy_account_balance_consume_record (consume_time);

-- 订单事务消息
DROP TABLE IF EXISTS sys_transaction_message;
CREATE TABLE sys_transaction_message
(
    id            INTEGER      NOT NULL AUTO_INCREMENT,
    business_type VARCHAR(20)  NOT NULL,
    sn            VARCHAR(100) NOT NULL,
    destination   VARCHAR(100) NOT NULL,
    route         VARCHAR(100) NOT NULL,
    payload       TEXT         NOT NULL,
    payload_type  VARCHAR(200),
    last_run_at   TIMESTAMP             DEFAULT NULL,
    try_times     INTEGER      NOT NULL DEFAULT 0,
    create_time   TIMESTAMP    NOT NULL,
    is_success    BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX tx_message_unique_key ON sys_transaction_message (sn, business_type);
CREATE INDEX tx_message_idx_create_time ON sys_transaction_message (create_time);

-- 订单表
DROP TABLE IF EXISTS purchase_order;
CREATE TABLE purchase_order
(
    id                  INTEGER PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_sn            VARCHAR(50)    NOT NULL COMMENT '订单号',
    user_id             INTEGER        NOT NULL COMMENT '用户id',
    user_real_name      VARCHAR(50)    NOT NULL COMMENT '用户真实名称',
    user_phone          VARCHAR(20)    NOT NULL COMMENT '用户联系方式',
    third_party_user_id varchar(64)    NOT NULL COMMENT '第三方用户id',
    order_type          INTEGER        NOT NULL COMMENT '订单分类',
    order_amount        DECIMAL(20, 8) NOT NULL COMMENT '订单金额',
    currency            VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
    service_rate        DECIMAL(20, 8) COMMENT '服务费比例',
    service_amount      DECIMAL(20, 8) COMMENT '服务费金额',
    user_pay_amount     DECIMAL(20, 8) COMMENT '用户实际支付金额',
    payment_channel     VARCHAR(20) COMMENT '支付渠道',
    order_status        VARCHAR(20)    NOT NULL COMMENT '订单状态',
    order_create_time   TIMESTAMP      NOT NULL COMMENT '订单生成时间',
    order_pay_stop_time TIMESTAMP COMMENT '订单截止支付时间',
    order_success_time  TIMESTAMP COMMENT '支付完成时间',
    remark              VARCHAR(500) COMMENT '订单备注',
    ticket_no           VARCHAR(50) COMMENT '票据号',
    is_deleted          BOOLEAN     DEFAULT FALSE COMMENT '是否删除'
);

-- 订单表索引
CREATE INDEX idx_purchase_order_order_sn ON purchase_order (order_sn);

-- 充值订单详情表
DROP TABLE IF EXISTS order_detail_energy_top_up;
CREATE TABLE order_detail_energy_top_up
(
    id                    INTEGER   NOT NULL AUTO_INCREMENT,
    order_sn              VARCHAR(20)   DEFAULT NULL,
    owner_id              INTEGER       DEFAULT NULL,
    owner_type            SMALLINT      DEFAULT NULL,
    owner_name            VARCHAR(50)   DEFAULT NULL,
    account_id            INTEGER   NOT NULL,
    electric_account_type SMALLINT      DEFAULT NULL,
    meter_type            SMALLINT  NOT NULL,
    meter_id              INTEGER   NOT NULL,
    meter_name            VARCHAR(50)   DEFAULT NULL,
    meter_no              VARCHAR(50)   DEFAULT NULL,
    space_id              INTEGER       DEFAULT NULL,
    space_name            VARCHAR(100)  DEFAULT NULL,
    space_parent_ids      VARCHAR(200)  DEFAULT NULL,
    space_parent_names    VARCHAR(1000) DEFAULT NULL,
    balance_type          SMALLINT      DEFAULT NULL COMMENT '余额类型：0账户余额，1电表余额，参照BalanceTypeEnum',
    create_time           TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_order_order_sn ON order_detail_energy_top_up (order_sn);

-- 第三方支付预订单
DROP TABLE IF EXISTS order_third_party_prepay;
CREATE TABLE order_third_party_prepay
(
    id                  INTEGER      NOT NULL AUTO_INCREMENT,
    order_sn            VARCHAR(20)  NOT NULL,
    prepay_id           VARCHAR(64)  NOT NULL,
    third_party_user_id VARCHAR(128) NOT NULL,
    third_party_sn      VARCHAR(64)           DEFAULT NULL,
    prepay_at           TIMESTAMP    NOT NULL,
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_order_sn_third_party_prepay ON order_third_party_prepay (order_sn);

-- 销户/销表结算订单详情表
DROP TABLE IF EXISTS order_detail_termination;
CREATE TABLE order_detail_termination
(
    id                    INTEGER        NOT NULL AUTO_INCREMENT COMMENT '主键id',
    order_sn              VARCHAR(20)    NOT NULL COMMENT '订单号',
    cancel_no             VARCHAR(50)    NOT NULL COMMENT '销户编号',
    account_id            INTEGER        NOT NULL COMMENT '账户id',
    owner_id              INTEGER                 DEFAULT NULL COMMENT '账户归属者id',
    owner_type            SMALLINT                DEFAULT NULL COMMENT '账户类型，0企业1个人',
    owner_name            VARCHAR(50)             DEFAULT NULL COMMENT '所有人名称',
    settlement_amount     DECIMAL(20, 8) NOT NULL DEFAULT 0.00000000 COMMENT '结算金额',
    electric_account_type smallint       NOT NULL COMMENT '电费账户类型：0按需、1包月、2合并计费',
    electric_meter_amount INTEGER        NOT NULL COMMENT '销户电表数量',
    full_cancel           BOOLEAN        NOT NULL COMMENT '1代表全部销户',
    close_reason          VARCHAR(500)            DEFAULT NULL COMMENT '终止原因',
    snapshot_payload      TEXT                    DEFAULT NULL COMMENT '结算快照明细(JSON)',
    create_time           TIMESTAMP      NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_order_detail_account_termination_sn ON order_detail_termination (order_sn);
CREATE INDEX idx_order_detail_account_termination_cancel_no ON order_detail_termination (cancel_no);

-- 机构信息表
DROP TABLE IF EXISTS sys_organization;
CREATE TABLE sys_organization
(
    id                   INTEGER      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    organization_name    VARCHAR(100) NOT NULL COMMENT '机构名称',
    organization_type    INTEGER               DEFAULT NULL COMMENT '机构类型',
    organization_address VARCHAR(200)          DEFAULT NULL COMMENT '机构地址',
    manager_name         VARCHAR(50)           DEFAULT NULL COMMENT '负责人姓名',
    manager_phone        VARCHAR(20)           DEFAULT NULL COMMENT '负责人电话',
    credit_code          VARCHAR(64)           DEFAULT NULL COMMENT '统一社会信用代码/机构编码',
    entry_date           DATE                  DEFAULT NULL COMMENT '入驻日期',
    remark               VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    is_deleted           BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否删除',
    create_user          INTEGER               DEFAULT NULL COMMENT '创建人',
    create_user_name     VARCHAR(50)           DEFAULT NULL COMMENT '创建人姓名',
    create_time          TIMESTAMP             DEFAULT NULL COMMENT '创建时间',
    update_user          INTEGER               DEFAULT NULL COMMENT '更新人',
    update_user_name     VARCHAR(50)           DEFAULT NULL COMMENT '更新人姓名',
    update_time          TIMESTAMP             DEFAULT NULL COMMENT '更新时间',
    own_area_id          INTEGER               DEFAULT NULL COMMENT '所属区域ID',
    active_org_name      VARCHAR(100) GENERATED ALWAYS AS (CASE WHEN is_deleted = FALSE THEN organization_name ELSE NULL END),
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_organization_name ON sys_organization (active_org_name);

-- 空间表
DROP TABLE IF EXISTS sys_space;
CREATE TABLE sys_space
(
    id               INTEGER      NOT NULL AUTO_INCREMENT,
    name             VARCHAR(32)  NOT NULL COMMENT '名称',
    pid              INTEGER      NOT NULL COMMENT '父id',
    full_path        VARCHAR(255) NOT NULL DEFAULT '' COMMENT ',隔开全路径，包括自己',
    type             SMALLINT     NOT NULL COMMENT '空间类型：1主区域、2内部区域、房间、自定义区域',
    area             DECIMAL(10, 2)        DEFAULT NULL COMMENT '面积',
    sort_index       INTEGER      NOT NULL DEFAULT 0 COMMENT '排序',
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否被删除：0未删除；1已删除',
    create_user      INTEGER               DEFAULT NULL COMMENT '创建人',
    create_user_name VARCHAR(20)           DEFAULT '' COMMENT '创建人姓名',
    create_time      TIMESTAMP             DEFAULT NULL COMMENT '创建时间',
    update_user      INTEGER               DEFAULT NULL COMMENT '修改人',
    update_user_name VARCHAR(20)           DEFAULT '' COMMENT '修改人姓名',
    update_time      TIMESTAMP             DEFAULT NULL COMMENT '修改时间',
    own_area_id      INTEGER               DEFAULT NULL COMMENT '所属区域',
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user
(
    id                INTEGER     NOT NULL AUTO_INCREMENT,
    user_name         VARCHAR(40) NOT NULL,
    password          VARCHAR(64) NOT NULL,
    real_name         VARCHAR(30) NOT NULL,
    user_phone        VARCHAR(20) NOT NULL,
    user_gender       SMALLINT    NOT NULL DEFAULT 1,
    certificates_type SMALLINT    NOT NULL DEFAULT 1,
    certificates_no   VARCHAR(20)          DEFAULT '',
    avatar            VARCHAR(500)         DEFAULT '',
    remark            VARCHAR(500)         DEFAULT '',
    organization_id   INTEGER              DEFAULT NULL,
    is_deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    create_user       INTEGER              DEFAULT NULL,
    create_user_name  VARCHAR(20)          DEFAULT '',
    create_time       TIMESTAMP            DEFAULT NULL,
    update_user       INTEGER              DEFAULT NULL,
    update_user_name  VARCHAR(20)          DEFAULT '',
    update_time       TIMESTAMP            DEFAULT NULL,
    active_user_name  VARCHAR(40) GENERATED ALWAYS AS (CASE WHEN is_deleted = FALSE THEN user_name ELSE NULL END),
    active_user_phone VARCHAR(20) GENERATED ALWAYS AS (CASE WHEN is_deleted = FALSE THEN user_phone ELSE NULL END),
    PRIMARY KEY (id)
);
CREATE INDEX idx_sys_user_real_name ON sys_user (real_name);
CREATE INDEX idx_sys_user_user_phone ON sys_user (user_phone);
CREATE INDEX idx_sys_create_time ON sys_user (create_time);
CREATE UNIQUE INDEX uk_sys_user_active_user_name ON sys_user (active_user_name);
CREATE UNIQUE INDEX uk_sys_user_active_user_phone ON sys_user (active_user_phone);

-- 菜单和角色管理相关表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu
(
    id               INTEGER      NOT NULL AUTO_INCREMENT COMMENT '菜单主键',
    menu_name        VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    menu_key         VARCHAR(64)  NOT NULL COMMENT '菜单唯一键',
    pid              INTEGER      NOT NULL DEFAULT 0 COMMENT '父级菜单ID（0表示顶级）',
    sort_num         INTEGER      NOT NULL DEFAULT 0 COMMENT '显示排序号',
    path             VARCHAR(128)          DEFAULT '' COMMENT '前端访问路径',
    menu_source      SMALLINT     NOT NULL COMMENT '1 web, 2 mobile',
    menu_type        SMALLINT     NOT NULL COMMENT '1 菜单, 2 按钮',
    icon             VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '菜单图标',
    remark           VARCHAR(255) NOT NULL DEFAULT '' COMMENT '菜单备注',
    is_hidden        BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否隐藏 0否 1是',
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否删除 0未删除 1已删除',
    create_user      INTEGER               DEFAULT NULL COMMENT '创建人',
    create_user_name VARCHAR(20)           DEFAULT NULL COMMENT '创建人姓名',
    create_time      TIMESTAMP             DEFAULT NULL COMMENT '创建时间',
    update_user      INTEGER               DEFAULT NULL COMMENT '修改人',
    update_user_name VARCHAR(20)           DEFAULT NULL COMMENT '修改人姓名',
    update_time      TIMESTAMP             DEFAULT NULL COMMENT '修改时间',
    active_menu_key  VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN is_deleted = FALSE THEN menu_key ELSE NULL END),
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_sys_menu_uq_menu_key ON sys_menu (active_menu_key);
CREATE INDEX idx_sys_menu_sort ON sys_menu (sort_num);

-- 菜单API权限表
DROP TABLE IF EXISTS sys_menu_auth;
CREATE TABLE sys_menu_auth
(
    id              INTEGER      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    menu_id         INTEGER      NOT NULL COMMENT '关联菜单ID',
    permission_code VARCHAR(128) NOT NULL COMMENT '接口权限标识',
    create_time     TIMESTAMP DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_uq_menu_auth_menu_auth ON sys_menu_auth (menu_id, permission_code);

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role
(
    id               INTEGER      NOT NULL AUTO_INCREMENT COMMENT '角色主键',
    role_name        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    role_key         VARCHAR(64)  NOT NULL COMMENT '角色唯一标识',
    sort_num         INTEGER      NOT NULL DEFAULT 0 COMMENT '显示排序号',
    remark           VARCHAR(255) NOT NULL DEFAULT '' COMMENT '角色备注',
    is_system        BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否系统内置 0否 1是',
    is_disabled      BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否禁用 0否 1是',
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否删除 0未删除 1已删除',
    create_user      INTEGER               DEFAULT NULL COMMENT '创建人',
    create_user_name VARCHAR(20)           DEFAULT NULL COMMENT '创建人姓名',
    create_time      TIMESTAMP             DEFAULT NULL COMMENT '创建时间',
    update_user      INTEGER               DEFAULT NULL COMMENT '修改人',
    update_user_name VARCHAR(20)           DEFAULT NULL COMMENT '修改人姓名',
    update_time      TIMESTAMP             DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_uq_sys_role_role_key ON sys_role (role_key, is_deleted);
CREATE INDEX idx_sys_role_sort_role ON sys_role (sort_num);

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu
(
    id          INTEGER NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id     INTEGER NOT NULL COMMENT '角色ID',
    menu_id     INTEGER NOT NULL COMMENT '菜单ID',
    create_time TIMESTAMP DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_uq_role_menu_role_menu ON sys_role_menu (role_id, menu_id);
CREATE INDEX idx_role_menu_menu_id ON sys_role_menu (menu_id);

-- 菜单路径表（闭包表）
DROP TABLE IF EXISTS sys_menu_path;
CREATE TABLE sys_menu_path
(
    id            INTEGER  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    ancestor_id   INTEGER  NOT NULL COMMENT '祖先菜单ID',
    descendant_id INTEGER  NOT NULL COMMENT '后代菜单ID',
    depth         SMALLINT NOT NULL COMMENT '祖先到后代的距离（自环为 0）',
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_closure ON sys_menu_path (ancestor_id, descendant_id);
CREATE INDEX idx_menu_path_descendant ON sys_menu_path (descendant_id);
CREATE INDEX idx_menu_path_depth ON sys_menu_path (depth);

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role
(
    id          INTEGER NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     INTEGER NOT NULL COMMENT '用户ID',
    role_id     INTEGER NOT NULL COMMENT '角色ID',
    create_time TIMESTAMP DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_uq_user_role_user_role ON sys_user_role (user_id, role_id);
CREATE INDEX idx_user_role_role_id ON sys_user_role (role_id);
