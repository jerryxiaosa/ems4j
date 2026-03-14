SET NAMES utf8mb4;
### 空间
INSERT INTO sys_space
(id, name, pid, full_path, `type`, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(2, '1号楼', 1, '1,2', 2, NULL, 1, 0, 1, '管理员', '2026-02-26 15:21:43', 1, '管理员', '2026-02-26 15:21:43', 1);
INSERT INTO sys_space
(id, name, pid, full_path, `type`, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(3, '物业楼', 1, '1,3', 2, NULL, 2, 0, 1, '管理员', '2026-02-26 15:21:57', 1, '管理员', '2026-02-26 15:27:18', 1);
INSERT INTO sys_space
(id, name, pid, full_path, `type`, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(4, '1#一层', 2, '1,2,4', 2, NULL, 1, 0, 1, '管理员', '2026-02-26 15:22:30', 1, '管理员', '2026-02-26 15:22:30', 1);
INSERT INTO sys_space
(id, name, pid, full_path, `type`, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(5, '101室', 4, '1,2,4,5', 3, 72.00, 1, 0, 1, '管理员', '2026-02-26 15:25:40', 1, '管理员', '2026-02-26 15:25:40', 1);
INSERT INTO sys_space
(id, name, pid, full_path, `type`, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(6, '102室', 4, '1,2,4,6', 3, 78.00, 1, 0, 1, '管理员', '2026-02-26 15:25:49', 1, '管理员', '2026-02-26 15:25:49', 1);
INSERT INTO sys_space
(id, name, pid, full_path, `type`, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(7, '1#二层', 2, '1,2,7', 2, NULL, 1, 0, 1, '管理员', '2026-02-26 15:25:49', 1, '管理员', '2026-02-26 15:25:49', 1);
INSERT INTO sys_space
(id, name, pid, full_path, `type`, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(8, '201室', 7, '1,2,7,8', 3, 88.00, 1, 0, 1, '管理员', '2026-02-26 15:25:49', 1, '管理员', '2026-02-26 15:25:49', 1);
INSERT INTO sys_space
(id, name, pid, full_path, `type`, area, sort_index, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(9, '202室', 7, '1,2,7,9', 3, 88.00, 1, 0, 1, '管理员', '2026-02-26 15:25:49', 1, '管理员', '2026-02-26 15:25:49', 1);

### 机构
INSERT INTO sys_organization
(id, organization_name, organization_type, organization_address, manager_name, manager_phone, credit_code, entry_date, remark, is_deleted, create_user, create_user_name, create_time, update_user, update_user_name, update_time, own_area_id)
VALUES(1, '艾顿智能科技有限公司', 1, '浙江省杭州市', '张强', '15850976887', '91330100MA123456', '2026-01-01', '示例单位', 0, 1, '管理员', '2026-02-26 13:45:48', 1, '管理员', '2026-02-26 13:45:48', 1);

### 机构、空间关联
INSERT INTO energy_owner_space_rel
(id, owner_type, owner_id, space_id, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(1, 0, 1, 6, NULL, NULL, NULL, NULL, NULL, NULL);


### 电表
INSERT INTO energy_electric_meter
(id, account_id, space_id, meter_name, is_online, last_online_time, device_no, model_id, product_code, communicate_model, gateway_id, port_no, meter_address, imei, is_cut_off, remark, iot_id, is_calculate, calculate_type, is_prepay, protected_model, price_plan_id, warn_plan_id, warn_type, ct, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted, own_area_id)
VALUES(1, NULL, 6, '102室空调照明', 1, NULL, 'DE9821', 2, 'ACREL_DTSY_1352_4G', 'nb', NULL, NULL, NULL, 'SN0033', 0, NULL, '2', 0, NULL, 1, 0, NULL, NULL, NULL, 2, 1, '管理员', '2025-10-29 11:27:15', NULL, NULL, '2026-03-13 11:00:00', 0, 1);

### 预警方案
INSERT INTO energy_warn_plan
(id, name, first_level, second_level, auto_close, remark, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES(1, '默认预警方案', 10000.00000000, 500.00000000, 0, NULL, 1, '管理员', '2026-03-13 16:57:15', 1, '管理员', '2026-03-13 16:57:15', 0);

### 电费方案
INSERT INTO energy_electric_price_plan
(id, name, price_higher, price_high, price_low, price_lower, price_deep_low, is_step, step_price, is_custom_price, price_higher_base, price_high_base, price_low_base, price_lower_base, price_deep_low_base, price_higher_multiply, price_high_multiply, price_low_multiply, price_lower_multiply, price_deep_low_multiply, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES(1, '默认电价方案', 2.00000000, 1.00000000, 0.80000000, 0.50000000, 0.50000000, 1, '[{"start":0,"end":100,"value":1.5},{"start":100,"end":500,"value":2},{"start":500,"end":null,"value":2.5}]', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '管理员', '2026-03-13 16:58:02', 1, '管理员', '2026-03-13 16:58:02', 0);
