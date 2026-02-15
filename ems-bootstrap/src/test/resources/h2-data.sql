-- 插入基础测试数据

-- 插入设备品类数据
INSERT INTO device_type (id, pid, ancestor_id, type_name, type_key, level, create_time, is_deleted )
VALUES (1, 0, '0', '电表', 'ELECTRIC_METER', 1, '2025-01-02 03:04:05', FALSE);
INSERT INTO device_type (id, pid, ancestor_id, type_name, type_key, level, create_time, is_deleted)
VALUES (2, 0, '0', '网关', 'GATEWAY', 1, '2025-01-02 03:04:05', FALSE);

-- 插入设备型号数据
INSERT INTO device_model (id, type_id, type_key, manufacturer_name, model_name, product_code, model_property, create_time, is_deleted)
VALUES (1, 1, 'electricMeter', '华立科技', 'DDS102', 'DDS102', '{"communicateModel":"tcp", "isCt": true,"isPrepay": true,"phases":"single","accuracy":"1.0","max_current":"60A"}', '2025-01-02 03:04:05', FALSE);
INSERT INTO device_model (id, type_id, type_key, manufacturer_name, model_name, product_code, model_property, create_time, is_deleted)
VALUES (2, 1, 'electricMeter', '长沙威胜', 'DSSD331', 'DSSD331', '{"communicateModel":"nb", "phases":"three","accuracy":"0.5S","max_current":"100A"}', '2025-01-02 03:04:05', FALSE);
INSERT INTO device_model (id, type_id, type_key, manufacturer_name, model_name, product_code, model_property, create_time, is_deleted)
VALUES (3, 1, 'electricMeter', '宁波三星', 'DTSD341', 'DTSD341', '{"communicateModel":"tcp", "phases":"three","accuracy":"1.0","max_current":"80A"}', '2025-01-02 03:04:05', FALSE);
INSERT INTO device_model (id, type_id, type_key, manufacturer_name, model_name, product_code, model_property, create_time, is_deleted)
VALUES (4, 2, 'gateway', '华为', 'AR151', 'AR151', '{"protocol":["Modbus","DL/T645"],"ports":4,"communication":"4G/Ethernet"}', '2025-01-02 03:04:05', FALSE);
INSERT INTO device_model (id, type_id, type_key, manufacturer_name, model_name, product_code, model_property, create_time, is_deleted)
VALUES (5, 2, 'gateway', '中兴', 'ZXR10 1800', 'ZXR10-1800', '{"protocol":["Modbus","DL/T645","CJ/T188"],"ports":8,"communication":"4G/WiFi/Ethernet"}', '2025-01-02 03:04:05', FALSE);

-- 插入告警方案数据
INSERT INTO energy_warn_plan (id, name, first_level, second_level, auto_close, remark, create_time, is_deleted)
VALUES (1, '默认告警方案', 10.00, 5.00, FALSE, '系统默认告警方案', '2025-01-02 03:04:05', FALSE);

-- 插入电价方案数据
INSERT INTO energy_electric_price_plan (id, name, price_higher, price_high, price_low, price_lower, price_deep_low, is_step, is_custom_price, price_higher_base, price_high_base, price_low_base, price_lower_base, price_deep_low_base, price_higher_multiply, price_high_multiply, price_low_multiply, price_lower_multiply, price_deep_low_multiply, create_time, is_deleted)
VALUES (1, '默认电价方案', 1.2000, 1.0000, 0.8000, 0.6000, 0.4000, FALSE, FALSE, 1.2000, 2.0000, 0.8000, 0.6000, 0.4000, 1, 1, 1, 1,1,  '2025-01-02 03:04:05', FALSE);

-- 插入测试网关数据
INSERT INTO energy_gateway (id, space_id, gateway_name, gateway_no, device_no, model_id, product_code, communicate_model, sn, is_online, iot_id, create_time, is_deleted, own_area_id)
VALUES (1, 100, '测试网关1', 'GW001', 'SN001', 200, '测试网关型号', 'TCP', 'SN001', TRUE, '1', '2025-01-02 03:04:05', FALSE, 1);

-- 插入系统配置数据
INSERT INTO sys_config (id, config_module_name, config_key, config_name, config_value, is_system, is_deleted, create_time)
VALUES (1, 'system', 'default_area_id', '默认区域ID', '1', TRUE, FALSE, '2025-01-02 03:04:05');
INSERT INTO sys_config (id, config_module_name, config_key, config_name, config_value, is_system, is_deleted, create_time)
VALUES (2, 'device', 'device_config', '设备配置', '[{"areaId":1,"deviceConfigList":[{"moduleServiceName":"EnergyService","implName":"AcrelService","configValue":{"protocol":"TCP","timeout":5000}}]}]', TRUE, FALSE, '2025-01-02 03:04:05');
INSERT INTO sys_config (id, config_module_name, config_key, config_name, config_value, is_system, is_deleted, create_time)
VALUES (7, 'electric', 'electric_price_time', '默认电价时间段','[{"type":5,"start":"00:00:00"},{"type":4,"start":"06:00:00"},{"type":3,"start":"08:00:00"},{"type":2,"start":"10:00:00"},{"type":1,"start":"14:00:00"},{"type":2,"start":"17:00:00"},{"type":1,"start":"19:00:00"},{"type":3,"start":"22:00:00"}]',  TRUE, FALSE, '2023-01-01 00:00:00');
INSERT INTO sys_config (id, config_module_name, config_key, config_name, config_value, is_system, is_deleted, create_time)
VALUES (8, 'electric', 'electric_step_price', '默认阶梯电价', '[{"startValue":0,"endValue":100,"priceRate":1.0},{"startValue":100,"endValue":200,"priceRate":1.2},{"startValue":200,"endValue":null,"priceRate":1.5}]', TRUE, FALSE, '2023-01-01 00:00:00');
INSERT INTO sys_config (id, config_module_name, config_key, config_name, config_value, is_system, is_deleted, create_time)
VALUES (9, 'electric', 'electric_price_type', '默认尖峰平谷电价', '[{"type":1,"price":1.2},{"type":2,"price":1.0},{"type":3,"price":0.8},{"type":4,"price":0.6},{"type":5,"price":0.4}]', TRUE, FALSE, '2023-01-01 00:00:00');

-- 插入能耗账户测试数据
INSERT INTO energy_account (id, owner_id, owner_type, owner_name, contact_name, contact_phone, electric_account_type, monthly_pay_amount, warn_plan_id, electric_warn_type, create_user, create_user_name, create_time, is_deleted)
VALUES (1, 1001, 0, '账户1', '联系人1', '13800000001', 1, 500.00000000, 1, 'NONE', 1, 'admin', '2025-01-02 03:04:05', FALSE);
INSERT INTO energy_account (id, owner_id, owner_type, owner_name, contact_name, contact_phone, electric_account_type, monthly_pay_amount, warn_plan_id, electric_warn_type, create_user, create_user_name, create_time, is_deleted)
VALUES (2, 1002, 1, '账户2', '联系人2', '13800000002', 2, null, 1, 'NORMAL', 1, 'admin', '2025-01-02 03:04:05', FALSE);
INSERT INTO energy_account (id, owner_id, owner_type, owner_name, contact_name, contact_phone, electric_account_type, monthly_pay_amount, warn_plan_id, electric_warn_type, create_user, create_user_name, create_time, is_deleted)
VALUES (3, 1003, 1, '账户3', '联系人3', '13800000003', 0, null, 1, 'BALANCE_CRITICAL', 1, 'admin', '2025-01-02 03:04:05', FALSE);
INSERT INTO energy_account (id, owner_id, owner_type, owner_name, contact_name, contact_phone, electric_account_type, monthly_pay_amount, warn_plan_id, electric_warn_type, create_user, create_user_name, create_time, is_deleted)
VALUES (4, 1004, 0, '账户4', '联系人4', '13800000004', 1, 1000.00000000, 1, 'NORMAL', 1, 'admin', '2025-01-02 03:04:05', FALSE);

-- 插入电表测试数据
INSERT INTO energy_electric_meter (id, space_id, iot_id, meter_name, meter_no, device_no, model_id, product_code, communicate_model, gateway_id, port_no, meter_address, is_online, is_cut_off, is_calculate, calculate_type, is_prepay, protected_model, price_plan_id, warn_plan_id, warn_type, account_id, ct, create_user, create_user_name, create_time, is_deleted, own_area_id)
VALUES (1, 101, '1001', '1号楼电表', 'EM001', 'SN001:1:1', 1, 'DDS102', 'Modbus', 1, 1, 1, TRUE, FALSE, TRUE, 1, TRUE, FALSE, 1, 1, 'NONE', 1, 1, 1, 'admin', '2025-01-02 03:04:05', FALSE, 1);
INSERT INTO energy_electric_meter (id, space_id, iot_id, meter_name, meter_no, device_no, model_id, product_code, communicate_model, gateway_id, port_no, meter_address, is_online, is_cut_off, is_calculate, calculate_type, is_prepay, protected_model, price_plan_id, warn_plan_id, warn_type, account_id, ct, create_user, create_user_name, create_time, is_deleted, own_area_id)
VALUES (2, 102, '1002', '2号楼电表',  'EM002', 'SN001:2:2', 2, 'DSSD331', 'DL/T645', 1, 2, 2, TRUE,  FALSE, TRUE, 2, TRUE, FALSE, 1, 1, 1, 2, 5, 1, 'admin', '2025-01-02 03:04:05', FALSE, 1);
INSERT INTO energy_electric_meter (id, space_id, meter_name, meter_no, device_no, model_id, product_code, communicate_model, gateway_id, port_no, meter_address, is_online,  is_cut_off, is_calculate, calculate_type, is_prepay, protected_model, price_plan_id, warn_plan_id, warn_type, account_id, ct, create_user, create_user_name, create_time, is_deleted, own_area_id)
VALUES (3, 103, '3号楼电表', 'EM003', 'SN001:3:3', 3, 'DTSD341', 'Modbus', 1, 3, 3, FALSE, TRUE, TRUE, 1, TRUE, TRUE, 1, 1, 'BALANCE_LOW', 3, 1, 1, 'admin', '2025-01-02 03:04:05', FALSE, 1);
INSERT INTO energy_electric_meter (id, space_id, meter_name, meter_no, device_no, model_id, product_code, communicate_model, gateway_id, port_no, meter_address, is_online, is_cut_off, is_calculate, calculate_type, is_prepay, protected_model, price_plan_id, warn_plan_id, warn_type, account_id, ct, create_user, create_user_name, create_time, is_deleted, own_area_id)
VALUES (4, 104, '办公楼电表', 'EM004', 'SN001:4:4', 1, 'DDS102', 'TCP', 1, 4, 4, TRUE, FALSE, TRUE, 3, FALSE, FALSE, 1, 1, 'NORMAL', 4, 10, 1, 'admin', '2025-01-02 03:04:05', FALSE, 1);
INSERT INTO energy_electric_meter (id, space_id, meter_name, meter_no, device_no, model_id, product_code, communicate_model, gateway_id, port_no, meter_address, is_online,  is_cut_off, is_calculate, calculate_type, is_prepay, protected_model, price_plan_id, warn_plan_id, warn_type, account_id, ct, create_user, create_user_name, create_time, is_deleted, own_area_id)
VALUES (5, 105, '车间电表A', 'EM005', 'SN001:5:5', 2, 'DSSD331', 'Modbus', 1, 5, 5, TRUE, FALSE, FALSE, 1, TRUE, FALSE, 1, 1, 'NORMAL', NULL, 20, 1, 'admin', '2025-01-02 03:04:05', FALSE, 1);
INSERT INTO energy_electric_meter (id, space_id, meter_name, meter_no, device_no, model_id, product_code, communicate_model, gateway_id, port_no, meter_address, is_online, is_cut_off, is_calculate, calculate_type, is_prepay, protected_model, price_plan_id, warn_plan_id, warn_type, account_id, ct, create_user, create_user_name, create_time, is_deleted, own_area_id)
VALUES (6, 106, '车间电表B', 'EM006', 'SN001:6:6', 3, 'DTSD341', 'DL/T645', 1, 6, 6, FALSE, TRUE, TRUE, 2, FALSE, TRUE, 1, 1, 'OFFLINE', NULL,  1, 1, 'admin', '2025-01-02 03:04:05', FALSE, 1);
INSERT INTO energy_electric_meter (id, space_id, iot_id, meter_name, meter_no, device_no, model_id, product_code, communicate_model, gateway_id, port_no, meter_address, is_online,  is_cut_off, is_calculate, calculate_type, is_prepay, protected_model, price_plan_id, warn_plan_id, warn_type, account_id, ct, create_user, create_user_name, create_time, is_deleted, own_area_id)
VALUES (7, 107, '1007', '宿舍电表', 'EM007', 'SN001:7:7', 1, 'DDS102', 'Modbus', 1, 7, 7, TRUE, FALSE, TRUE, 1, TRUE, FALSE, 1, 1, 'NORMAL', 1, 1, 1, 'admin', '2025-01-02 03:04:05', FALSE, 1);

-- 添加测试用的电表电量记录数据
INSERT INTO energy_electric_meter_power_record (id, meter_id, meter_name, meter_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted) VALUES
(1, 1, '1号楼电表', 'EM001', 1, TRUE, 1, 100.50, 10.0, 20.0, 30.0, 40.0, 0.50, 'REPORT001', '2025-01-02 10:00:00', '2025-01-02 10:00:00', FALSE),
(2, 1, '1号楼电表', 'EM001', 1, TRUE, 1, 150.75, 15.0, 25.0, 35.0, 45.0, 0.75, 'REPORT002', '2025-01-02 11:00:00', '2025-01-02 11:00:00', FALSE),
(3, 2, '2号楼电表', 'EM002', 2, TRUE, 5, 200.25, 20.0, 30.0, 40.0, 50.0, 0.25, 'REPORT003', '2025-01-02 10:30:00', '2025-01-02 10:30:00', FALSE),
(4, 3, '3号楼电表', 'EM003', 3, TRUE, 1, 200.25, 20.0, 400.0, 40.0, 200.0, 0.25, 'REPORT004', '2025-01-02 10:30:00', '2025-01-02 10:30:00', FALSE);

-- 添加测试用的电表电量关联信息数据
INSERT INTO energy_electric_meter_power_relation (id, record_id, meter_id, is_calculate, calculate_type, calculate_type_name, space_id, space_name, space_parent_ids, space_parent_names, account_id, owner_id, owner_type, owner_name, record_time, create_time, is_deleted) VALUES
(1, 1, 1, TRUE, 1, '正常用电',  101, '1号楼', '0', '根空间', 1, 1001, 1, '测试用户1', '2025-01-02 10:00:00', '2025-01-02 10:00:00', FALSE),
(2, 2, 1, TRUE, 1, '正常用电',  101, '1号楼', '0', '根空间', 1, 1001, 1, '测试用户1',  '2025-01-02 11:00:00', '2025-01-02 11:00:00', FALSE),
(3, 3, 2, TRUE, 2, '正常用电',  102, '2号楼', '0', '根空间', 2, 1002, 2, '测试企业1',  '2025-01-02 10:30:00', '2025-01-02 10:30:00', FALSE);

-- 添加能耗余额测试数据
INSERT INTO energy_account_balance (id, balance_relation_id, balance_type, balance, account_id)
VALUES (1, 777, 1, 1200.50000000, 1);
INSERT INTO energy_account_balance (id, balance_relation_id, balance_type, balance, account_id)
VALUES (2, 1, 0, 3400.50000000, 1);
INSERT INTO energy_account_balance (id, balance_relation_id, balance_type, balance, account_id)
VALUES (3, 3, 1, -67.00000000, 3);

-- 添加测试用的电表用电消耗数据记录
INSERT INTO energy_electric_meter_power_consume_record (id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted) VALUES
(1, 555, TRUE, 1, 1, 101, 1, 100.50000000, 10.00000000, 20.00000000, 30.00000000, 40.00000000, 0.50000000, '2025-01-02 10:00:00', 2, 150.75000000, 15.00000000, 25.00000000, 35.00000000, 45.00000000, 0.75000000, '2025-01-02 11:00:00', 50.25000000, 5.00000000, 5.00000000, 5.00000000, 5.00000000, 0.25000000, '2025-01-02 11:00:00', '2025-01-02 11:05:00', FALSE),
(2, 666, TRUE, 2, 2, 102, 3, 200.25000000, 20.00000000, 30.00000000, 40.00000000, 50.00000000, 0.25000000, '2025-01-02 10:30:00', 4, 280.50000000, 28.00000000, 42.00000000, 56.00000000, 70.00000000, 0.50000000, '2025-01-02 12:30:00', 80.25000000, 8.00000000, 12.00000000, 16.00000000, 20.00000000, 0.25000000, '2025-01-02 12:30:00', '2025-01-02 12:35:00', FALSE),
(3, 777, TRUE, 1, 3, 103, 5, 300.00000000, 30.00000000, 45.00000000, 60.00000000, 75.00000000, 0.00000000, '2025-01-02 09:00:00', 6, 420.75000000, 42.00000000, 63.00000000, 84.00000000, 105.00000000, 0.75000000, '2025-01-02 15:00:00', 120.75000000, 12.00000000, 18.00000000, 24.00000000, 30.00000000, 0.75000000, '2025-01-02 15:00:00', '2025-01-02 15:05:00', FALSE);

-- 添加 sys_transaction_message 表的测试数据
INSERT INTO sys_transaction_message (id, business_type, sn, destination, route, payload, payload_type, last_run_at, try_times, create_time, is_success)
VALUES (1, 'ORDER_PAYMENT', 'TXN001', 'order-topic', 'order-created', '{"orderId":"ORD001","amount":100.50}', 'java.util.LinkedHashMap', '2025-01-02 10:00:00', 1, '2030-01-02 09:00:00', TRUE);
INSERT INTO sys_transaction_message (id, business_type, sn, destination, route, payload, payload_type, last_run_at, try_times, create_time, is_success)
VALUES (2, 'ORDER_PAYMENT', 'TXN002', 'order-topic', 'order-created', '{"messageDestination":"test-topic","routingIdentifier":"test-key","payload":"test-body"}', 'info.zhihui.ems.mq.api.model.MqMessage', '2025-01-02 11:30:00', 3, '2030-01-02 11:00:00', FALSE);
INSERT INTO sys_transaction_message (id, business_type, sn, destination, route, payload, payload_type, last_run_at, try_times, create_time, is_success)
VALUES (3, 'ORDER_PAYMENT', 'TXN003', 'order-topic', 'order-created', '{"messageDestination":"test-topic","routingIdentifier":"test-key","payload":"test-body"}', 'info.zhihui.ems.mq.api.model.MqMessage', '2025-01-02 15:45:00', 2, '2030-01-02 15:00:00', FALSE);
INSERT INTO sys_transaction_message (id, business_type, sn, destination, route, payload, payload_type, last_run_at, try_times, create_time, is_success)
VALUES (4, 'ORDER_PAYMENT', 'TXN004', 'order-topic', 'order-created', '{"orderId":"ORD004","amount":300.00}', 'java.util.LinkedHashMap', '2025-01-02 08:20:00', 1, '2030-01-02 08:00:00', TRUE);

-- 添加默认服务费配置
INSERT INTO sys_config (id, config_module_name, config_key, config_name, config_value, is_system, is_deleted, create_time)
VALUES (10, 'finance', 'service_rate', '默认服务费率', '0.05', TRUE, FALSE, '2025-01-02 03:04:05');

-- 添加销户记录测试数据
INSERT INTO energy_account_cancel_record (id, cancel_no, account_id, owner_id, owner_type, owner_name, electric_account_type, electric_meter_amount, full_cancel, clean_balance_type, clean_balance_real, clean_balance_ignore, remark, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES (1, 'CANCEL001', 1, 1001, 0, '测试企业1', 1, 2, TRUE, 1, 150.50, 0.00000000, '正常销户', 1, 'admin', '2025-01-02 14:00:00', 1, 'admin', '2025-01-02 14:00:00', FALSE);

INSERT INTO energy_account_cancel_record (id, cancel_no, account_id, owner_id, owner_type, owner_name, electric_account_type, electric_meter_amount, full_cancel, clean_balance_type, clean_balance_real, clean_balance_ignore, remark, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES (2, 'CANCEL002', 2, 1002, 1, '测试用户1', 2, 1, FALSE, 2, -50.25, 5.00000000, '部分销户', 1, 'admin', '2025-01-02 15:00:00', 1, 'admin', '2025-01-02 15:00:00', FALSE);

INSERT INTO energy_account_cancel_record (id, cancel_no, account_id, owner_id, owner_type, owner_name, electric_account_type, electric_meter_amount, full_cancel, clean_balance_type, clean_balance_real, clean_balance_ignore, remark, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES (3, 'CANCEL003', 3, 1003, 1, '测试企业2', 0, 1, TRUE, 0, 0.00, 0.00000000, '跳过结算销户', 1, 'admin', '2025-01-02 16:00:00', 1, 'admin', '2025-01-02 16:00:00', FALSE);

-- 添加销表记录测试数据
INSERT INTO energy_meter_cancel_record (id, cancel_no, account_id, meter_type, meter_id, meter_name, meter_no, space_id, space_name, space_parent_ids, space_parent_names, is_online, is_cut_off, balance, power, power_higher, power_high, power_low, power_lower, power_deep_low, show_time, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES (1, 'CANCEL001', 1, 1, 1, '1号楼电表', 'EM001', 101, '1号楼', '0,100', '根空间,园区', TRUE, FALSE, 1200.50, 1250.50, 800.25, 300.75, 100.50, 50.25, 0.00000000, '2025-01-02 14:00:00', 1, 'admin', '2025-01-02 14:00:00', 1, 'admin', '2025-01-02 14:00:00', FALSE);

INSERT INTO energy_meter_cancel_record (id, cancel_no, account_id, meter_type, meter_id, meter_name, meter_no, space_id, space_name, space_parent_ids, space_parent_names, is_online, is_cut_off, balance, power, power_higher, power_high, power_low, power_lower, power_deep_low, show_time, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES (2, 'CANCEL002', 2, 1, 2, '2号楼电表', 'EM002', 102, '2号楼', '0,100', '根空间,园区', TRUE, FALSE, 800.25, 2800.75, 1500.50, 800.25, 400.75, 100.25, 0.00000000, '2025-01-02 15:00:00', 1, 'admin', '2025-01-02 15:00:00', 1, 'admin', '2025-01-02 15:00:00', FALSE);

-- 添加空间测试数据
INSERT INTO sys_space (id, name, pid, full_path, type, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES (1, '测试空间', 0, '1', 3, 100.50, 1, FALSE, 1, 'admin', '2025-01-02 10:00:00', 1, 'admin', '2025-01-02 10:00:00', 1);
INSERT INTO sys_space (id, name, pid, full_path, type, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES (101, '测试空间2', 0, '101', 3, 87.50, 1, FALSE, 1, 'admin', '2025-01-02 10:00:00', 1, 'admin', '2025-01-02 10:00:00', 1);
INSERT INTO sys_space (id, name, pid, full_path, type, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES (103, '测试空间3', 0, '103', 3, 37.50, 1, FALSE, 1, 'admin', '2025-01-02 10:00:00', 1, 'admin', '2025-01-02 10:00:00', 1);

-- 添加机构测试数据
INSERT INTO sys_organization (id, organization_name, organization_type, organization_address, manager_name, manager_phone, credit_code, entry_date, remark, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES (1001, '测试科技有限公司', 1, '北京市海淀区中关村大街1号', '张三', '13800138001', '91110108MA01234567', '2025-01-01', '测试机构数据', FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05', 1);
INSERT INTO sys_organization (id, organization_name, organization_type, organization_address, manager_name, manager_phone, credit_code, entry_date, remark, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES (2001, '创新智能科技集团', 1, '上海市浦东新区陆家嘴环路1000号', '李四', '13900139002', '91310115MA02345678', '2024-12-15', '高新技术企业', FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05', 1);
INSERT INTO sys_organization (id, organization_name, organization_type, organization_address, manager_name, manager_phone, credit_code, entry_date, remark, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES (1, '华夏实业发展有限公司', 1, '广州市天河区珠江新城花城大道88号', '王五', '13700137003', '91440106MA03456789', '2024-11-20', '传统制造业企业', FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05', 1);


-- 添加用户测试数据 - 支持 UserService 集成测试
INSERT INTO sys_user (id, user_name, password, real_name, user_phone, user_gender, certificates_type, certificates_no, avatar, remark, organization_id, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (1, 'testuser1', '$2a$10$TcMsASV9MWR.gNTf7Q8.FuOPMeIeZ8XRbh7b2fqC7Dn0ckchgGtG.', '测试用户1', '13800138001', 1, 1, '110101199001011234', '', '测试用户1备注', 1001, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_user (id, user_name, password, real_name, user_phone, user_gender, certificates_type, certificates_no, avatar, remark, organization_id, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (2, 'testuser2', '$2a$10$TcMsASV9MWR.gNTf7Q8.FuOPMeIeZ8XRbh7b2fqC7Dn0ckchgGtG.', '测试用户2', '13800138002', 2, 1, '110101199001011235', '', '测试用户2备注', 1, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_user (id, user_name, password, real_name, user_phone, user_gender, certificates_type, certificates_no, avatar, remark, organization_id, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (3, 'deleteduser', '$2a$10$TcMsASV9MWR.gNTf7Q8.FuOPMeIeZ8XRbh7b2fqC7Dn0ckchgGtG.', '已删除用户', '13800138003', 1, 1, '110101199001011236', '', '已删除用户备注', 1, TRUE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

-- 添加菜单测试数据 - 支持 MenuService 集成测试
INSERT INTO sys_menu (id, menu_name, menu_key, pid, sort_num, path, menu_source, menu_type, icon, remark, is_hidden, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (1, '系统管理', 'system', 0, 1, '/system', 1, 1, 'setting', '系统管理菜单', FALSE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_menu (id, menu_name, menu_key, pid, sort_num, path, menu_source, menu_type, icon, remark, is_hidden, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (2, '用户管理', 'user-manage', 1, 1, '/system/user', 1, 1, 'user', '用户管理菜单', FALSE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_menu (id, menu_name, menu_key, pid, sort_num, path, menu_source, menu_type, icon, remark, is_hidden, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (3, '角色管理', 'role-manage', 1, 2, '/system/role', 1, 1, 'team', '角色管理菜单', FALSE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_menu (id, menu_name, menu_key, pid, sort_num, path, menu_source, menu_type, icon, remark, is_hidden, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (4, '新增用户', 'user-add', 2, 1, '', 1, 2, '', '新增用户按钮', FALSE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_menu (id, menu_name, menu_key, pid, sort_num, path, menu_source, menu_type, icon, remark, is_hidden, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (5, '编辑用户', 'user-edit', 2, 2, '', 1, 2, '', '编辑用户按钮', FALSE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_menu (id, menu_name, menu_key, pid, sort_num, path, menu_source, menu_type, icon, remark, is_hidden, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (6, '已删除菜单', 'deleted-menu', 0, 99, '/deleted', 1, 1, 'delete', '已删除的菜单', FALSE, TRUE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_menu (id, menu_name, menu_key, pid, sort_num, path, menu_source, menu_type, icon, remark, is_hidden, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (7, '移动首页', 'mobile-home', 0, 1, '/mobile/home', 2, 1, 'mobile', '移动端菜单', FALSE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

-- 添加菜单API权限测试数据
INSERT INTO sys_menu_auth (id, menu_id, permission_code, create_time)
VALUES (1, 2, 'user:list', '2025-01-02 03:04:05');

INSERT INTO sys_menu_auth (id, menu_id, permission_code, create_time)
VALUES (2, 4, 'user:add', '2025-01-02 03:04:05');

INSERT INTO sys_menu_auth (id, menu_id, permission_code, create_time)
VALUES (3, 5, 'user:edit', '2025-01-02 03:04:05');

-- 添加角色测试数据 - 支持 RoleService 集成测试
INSERT INTO sys_role (id, role_name, role_key, sort_num, remark, is_system, is_disabled, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (1, '超级管理员', 'admin', 1, '系统超级管理员角色', TRUE, FALSE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_role (id, role_name, role_key, sort_num, remark, is_system, is_disabled, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (2, '普通用户', 'user', 2, '普通用户角色', FALSE, FALSE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_role (id, role_name, role_key, sort_num, remark, is_system, is_disabled, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (3, '禁用角色', 'disabled', 3, '已禁用的角色', FALSE, TRUE, FALSE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

INSERT INTO sys_role (id, role_name, role_key, sort_num, remark, is_system, is_disabled, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES (4, '已删除角色', 'deleted', 4, '已删除的角色', FALSE, FALSE, TRUE, 1, 'admin', '2025-01-02 03:04:05', 1, 'admin', '2025-01-02 03:04:05');

-- 添加角色菜单关联测试数据
INSERT INTO sys_role_menu (id, role_id, menu_id, create_time)
VALUES (1, 1, 1, '2025-01-02 03:04:05');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_time)
VALUES (2, 1, 2, '2025-01-02 03:04:05');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_time)
VALUES (3, 1, 3, '2025-01-02 03:04:05');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_time)
VALUES (4, 1, 4, '2025-01-02 03:04:05');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_time)
VALUES (5, 1, 5, '2025-01-02 03:04:05');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_time)
VALUES (6, 2, 2, '2025-01-02 03:04:05');

-- 添加菜单路径测试数据（闭包表）
INSERT INTO sys_menu_path (ancestor_id, descendant_id, depth)
VALUES (1, 1, 0), (1, 2, 1), (1, 3, 1), (2, 2, 0), (2, 4, 1), (2, 5, 1), (3, 3, 0), (4, 4, 0), (5, 5, 0), (6, 6, 0), (7, 7, 0);

-- 添加用户角色关联测试数据 - 支持 UserService 集成测试
INSERT INTO sys_user_role (id, user_id, role_id, create_time)
VALUES (1, 1, 1, '2025-01-02 03:04:05');

INSERT INTO sys_user_role (id, user_id, role_id, create_time)
VALUES (2, 1, 2, '2025-01-02 03:04:05');

INSERT INTO sys_user_role (id, user_id, role_id, create_time)
VALUES (3, 2, 2, '2025-01-02 03:04:05');
