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
VALUES(1, NULL, 6, '102室空调照明', 1, NULL, 'DEV001', 2, 'ACREL_DTSY_1352_4G', 'nb', NULL, NULL, NULL, 'SN0033', 0, NULL, '2', 0, NULL, 1, 0, NULL, NULL, NULL, 2, 1, '管理员', '2025-10-29 11:27:15', NULL, NULL, '2026-03-13 11:00:00', 0, 1);

### 预警方案
INSERT INTO energy_warn_plan
(id, name, first_level, second_level, auto_close, remark, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES(1, '默认预警方案', 10000.00000000, 500.00000000, 0, NULL, 1, '管理员', '2026-03-13 16:57:15', 1, '管理员', '2026-03-13 16:57:15', 0);

### 电费方案
INSERT INTO energy_electric_price_plan
(id, name, price_higher, price_high, price_low, price_lower, price_deep_low, is_step, step_price, is_custom_price, price_higher_base, price_high_base, price_low_base, price_lower_base, price_deep_low_base, price_higher_multiply, price_high_multiply, price_low_multiply, price_lower_multiply, price_deep_low_multiply, create_user, create_user_name, create_time, update_user, update_user_name, update_time, is_deleted)
VALUES(1, '默认电价方案', 2.00000000, 1.00000000, 0.80000000, 0.50000000, 0.50000000, 1, '[{"start":0,"end":100,"value":1.5},{"start":100,"end":500,"value":2},{"start":500,"end":null,"value":2.5}]', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '管理员', '2026-03-13 16:58:02', 1, '管理员', '2026-03-13 16:58:02', 0);

### 模拟电量
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(1, 1, '102室电表', 'DEV001', NULL, 1, NULL, 0.45, 0.00, 0.45, 0.00, 0.00, 0.00, 'f4955af58e59c4100d5ba1a90df24ade7edd6c36f445eab03ef8768d6013210a', '2026-03-01 17:00:08', '2026-03-29 15:28:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(2, 1, '102室电表', 'DEV001', NULL, 1, NULL, 0.83, 0.00, 0.45, 0.38, 0.00, 0.00, 'a836d8e11a54d1266b5a87071facc2c85d033518a57a66ba7868ba03a851a2a1', '2026-03-01 18:00:08', '2026-03-29 15:28:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(3, 1, '102室电表', 'DEV001', NULL, 1, NULL, 1.24, 0.00, 0.45, 0.79, 0.00, 0.00, '7b40b84a65bd1978ca77b4e1f44ce919576229b5830a84a7acac76d1fd8bd7b9', '2026-03-01 19:00:08', '2026-03-29 15:28:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(4, 1, '102室电表', 'DEV001', NULL, 1, NULL, 1.64, 0.00, 0.45, 1.19, 0.00, 0.00, 'b7d613435315034d0c2095a7a8adf07fd6fea8f52aee3b1231c1b4ed5f8cabab', '2026-03-01 20:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(5, 1, '102室电表', 'DEV001', NULL, 1, NULL, 2.06, 0.00, 0.45, 1.61, 0.00, 0.00, '3392ba00f7fe8b980ae4d43f72abfdf470f2d95f405c2204969ce874f0290b90', '2026-03-01 21:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(6, 1, '102室电表', 'DEV001', NULL, 1, NULL, 2.41, 0.00, 0.45, 1.61, 0.35, 0.00, '2b180f9be7245f5a64373e83e90834e5ad6523292c1c0950ed89179a0d482729', '2026-03-01 22:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(7, 1, '102室电表', 'DEV001', NULL, 1, NULL, 2.78, 0.00, 0.45, 1.61, 0.72, 0.00, '393144b506a383ffac3c461bc98942d1574c7ddf1952f132a59d57f81232472f', '2026-03-01 23:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(8, 1, '102室电表', 'DEV001', NULL, 1, NULL, 2.92, 0.00, 0.45, 1.61, 0.72, 0.14, '356e5c5664e4b209d0b60738e55a5437150acd1e5fff5af33645d2f1bed073e2', '2026-03-02 00:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(9, 1, '102室电表', 'DEV001', NULL, 1, NULL, 3.07, 0.00, 0.45, 1.61, 0.72, 0.29, 'a61d94f1764e79107ac5a7e62751c34e6c3a498fe394befc78baecce5b24b45e', '2026-03-02 01:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(10, 1, '102室电表', 'DEV001', NULL, 1, NULL, 3.24, 0.00, 0.45, 1.61, 0.72, 0.46, 'f8ceaa809c2a1513b1efa6b91a28f726c2ac7c96dae8f75a7f8197d0787cb934', '2026-03-02 02:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(11, 1, '102室电表', 'DEV001', NULL, 1, NULL, 3.37, 0.00, 0.45, 1.61, 0.72, 0.59, '2ad8efd4340e0aa6ada39fe6ab7c7cc250a9bfdb6a9ddc827833ff3ff0423311', '2026-03-02 03:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(12, 1, '102室电表', 'DEV001', NULL, 1, NULL, 3.50, 0.00, 0.45, 1.61, 0.72, 0.72, '005d126efaff4702070250f26c7b8c55959436f64def009607d376ecbb4ec428', '2026-03-02 04:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(13, 1, '102室电表', 'DEV001', NULL, 1, NULL, 3.64, 0.00, 0.45, 1.61, 0.72, 0.86, 'd799336ff47ad504d760db6ecc20363948a6b16f1b52c5a35b606d8b255810ce', '2026-03-02 05:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(14, 1, '102室电表', 'DEV001', NULL, 1, NULL, 3.80, 0.00, 0.45, 1.61, 0.88, 0.86, '2878cad9e9883f1f6109e7bb9743ecd7cc7ec6d9190877aaac77006c2cbb22de', '2026-03-02 06:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(15, 1, '102室电表', 'DEV001', NULL, 1, NULL, 3.97, 0.00, 0.45, 1.61, 1.05, 0.86, '8425ad7d7466f3755a5fa88471253d9bcab1e48e60f6a87291d31ebc804e69f9', '2026-03-02 07:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(16, 1, '102室电表', 'DEV001', NULL, 1, NULL, 4.14, 0.00, 0.62, 1.61, 1.05, 0.86, '93f58ded22ded301e459fe45628f3a801ece837a686e0cf758a1161fd8e8f259', '2026-03-02 08:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(17, 1, '102室电表', 'DEV001', NULL, 1, NULL, 6.89, 0.00, 3.37, 1.61, 1.05, 0.86, '7078119a114471360b1c37797e9d865977e2128749ff1ff3fb80cb0045354b1c', '2026-03-02 09:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(18, 1, '102室电表', 'DEV001', NULL, 1, NULL, 10.06, 0.00, 6.54, 1.61, 1.05, 0.86, '3d9cb53460e76930dc06150ba2340fdb6112a05b099b67886f24fb3543f7e12a', '2026-03-02 10:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(19, 1, '102室电表', 'DEV001', NULL, 1, NULL, 13.42, 3.36, 6.54, 1.61, 1.05, 0.86, '8c83a7ad270865c44fbdcc63f9688e4afda11143486c7702e5776a689a0ed56a', '2026-03-02 11:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(20, 1, '102室电表', 'DEV001', NULL, 1, NULL, 16.68, 6.62, 6.54, 1.61, 1.05, 0.86, '5c46b894376df376affea74ad4879d36afc8eae2a460a8806bf51091057985f2', '2026-03-02 12:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(21, 1, '102室电表', 'DEV001', NULL, 1, NULL, 20.14, 6.62, 10.00, 1.61, 1.05, 0.86, 'c0d82b73a65e8181cf5a4ec5d780d7175c196ccfd480dbaaf5228497590c9dd7', '2026-03-02 13:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(22, 1, '102室电表', 'DEV001', NULL, 1, NULL, 23.02, 6.62, 12.88, 1.61, 1.05, 0.86, '6b32b7bb5e92d0b105dfce7f71559d3c81703e1193bebe584cd35eb97557b319', '2026-03-02 14:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(23, 1, '102室电表', 'DEV001', NULL, 1, NULL, 26.09, 6.62, 15.95, 1.61, 1.05, 0.86, '4bcba3d512dcb1caf6eec2106db97148add3b14cb8849afcae9fe32e4003fb6b', '2026-03-02 15:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(24, 1, '102室电表', 'DEV001', NULL, 1, NULL, 29.10, 6.62, 18.96, 1.61, 1.05, 0.86, '08f327c2a60a734ce3b4872b6664075761a74c41862e48048c96f2abe3c3cf67', '2026-03-02 16:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(25, 1, '102室电表', 'DEV001', NULL, 1, NULL, 32.30, 6.62, 22.16, 1.61, 1.05, 0.86, '886f555887ced4edff551ff279f19c9a265b41f9845cd7c38ccfdeb55b153ef1', '2026-03-02 17:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(26, 1, '102室电表', 'DEV001', NULL, 1, NULL, 32.47, 6.62, 22.16, 1.78, 1.05, 0.86, '0c0cf86e152593e174c276f3de3ccb9f8664b7117d565071da4c16f0a4611d48', '2026-03-02 18:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(27, 1, '102室电表', 'DEV001', NULL, 1, NULL, 32.61, 6.62, 22.16, 1.92, 1.05, 0.86, 'c19b4fe7b2b753926de4a8cfecc07944032adf03b86d457e3675bf0dd2f22bc7', '2026-03-02 19:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(28, 1, '102室电表', 'DEV001', NULL, 1, NULL, 32.74, 6.62, 22.16, 2.05, 1.05, 0.86, '0e4be01f3f057341743463535e652acbe0b57643cff6f496f2d6e00e99ed4e5d', '2026-03-02 20:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(29, 1, '102室电表', 'DEV001', NULL, 1, NULL, 32.88, 6.62, 22.16, 2.19, 1.05, 0.86, '17752906401e6e3fd04c91f328550add13f46683b2d3581ca5feb5479fe864fb', '2026-03-02 21:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(30, 1, '102室电表', 'DEV001', NULL, 1, NULL, 33.04, 6.62, 22.16, 2.19, 1.21, 0.86, '1c0f177273628b2e8cf4c7cda6cb63dd9b827afb9078c2f7eb847d17793391e5', '2026-03-02 22:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(31, 1, '102室电表', 'DEV001', NULL, 1, NULL, 33.21, 6.62, 22.16, 2.19, 1.38, 0.86, '0000c108840253413fa2720283aa015cad1694e8db5935f622bbdb952f169590', '2026-03-02 23:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(32, 1, '102室电表', 'DEV001', NULL, 1, NULL, 33.38, 6.62, 22.16, 2.19, 1.38, 1.03, '4c41d9ac8d83f04e4e830e8a4b81c23f86e19b6e4440d465408da2d586ce67c1', '2026-03-03 00:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(33, 1, '102室电表', 'DEV001', NULL, 1, NULL, 33.52, 6.62, 22.16, 2.19, 1.38, 1.17, '801ad6836132b300290e700f342cf2740c80cf3b423b37d212cb00ed99a372de', '2026-03-03 01:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(34, 1, '102室电表', 'DEV001', NULL, 1, NULL, 33.67, 6.62, 22.16, 2.19, 1.38, 1.32, 'ca2d79ede955186a0349c5dad816c94172f9d13570882e063c05d70d9e9e8530', '2026-03-03 02:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(35, 1, '102室电表', 'DEV001', NULL, 1, NULL, 33.83, 6.62, 22.16, 2.19, 1.38, 1.48, '20b314d00c4a9f9ed3121ebc6c0e854c6822d24d29664fc9093f753b075e0594', '2026-03-03 03:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(36, 1, '102室电表', 'DEV001', NULL, 1, NULL, 33.99, 6.62, 22.16, 2.19, 1.38, 1.64, '5d5f4411b2fefe156016815a93aa104d052a38958e9d54a068c5ee312dd000bc', '2026-03-03 04:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(37, 1, '102室电表', 'DEV001', NULL, 1, NULL, 34.16, 6.62, 22.16, 2.19, 1.38, 1.81, '66d7c49489b44a8687210cd8aead05ff1b7fa0361ebc5b4165e34d48300fe75e', '2026-03-03 05:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(38, 1, '102室电表', 'DEV001', NULL, 1, NULL, 34.30, 6.62, 22.16, 2.19, 1.52, 1.81, '7ee8db3c21559e3272a8973a7d443c46660c7646645716d2a9d2e1331403e7c4', '2026-03-03 06:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(39, 1, '102室电表', 'DEV001', NULL, 1, NULL, 34.45, 6.62, 22.16, 2.19, 1.67, 1.81, 'f834f600ba7811cb79a1466eff5b68919576bb72ae7d29e90285d616cdde2678', '2026-03-03 07:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(40, 1, '102室电表', 'DEV001', NULL, 1, NULL, 34.60, 6.62, 22.31, 2.19, 1.67, 1.81, '49b97acb1b57d9446a3d36abb9b29ac271bb3225410b8899a2b9cd538f5e2c80', '2026-03-03 08:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(41, 1, '102室电表', 'DEV001', NULL, 1, NULL, 37.99, 6.62, 25.70, 2.19, 1.67, 1.81, 'd9c974ac0342aef6e5baf88fcdfbbdef2285697faae225ce4aa744699f6fe023', '2026-03-03 09:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(42, 1, '102室电表', 'DEV001', NULL, 1, NULL, 40.81, 6.62, 28.52, 2.19, 1.67, 1.81, '7a34a7d74ed187b68cd76e3492a3cfb9227ff334b7fda671772d41da8f05777e', '2026-03-03 10:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(43, 1, '102室电表', 'DEV001', NULL, 1, NULL, 43.82, 9.63, 28.52, 2.19, 1.67, 1.81, 'eb4ccabfb68e60d07a3a5e5d516594cc785bade24ff242eaed669515c280d72c', '2026-03-03 11:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(44, 1, '102室电表', 'DEV001', NULL, 1, NULL, 46.73, 12.54, 28.52, 2.19, 1.67, 1.81, '3c2f5c73d2cb1a4722478f34ed74f75efecbe75f38b82c6687e31d99f4007f7c', '2026-03-03 12:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(45, 1, '102室电表', 'DEV001', NULL, 1, NULL, 49.83, 12.54, 31.62, 2.19, 1.67, 1.81, '45f368dd8929cc39ec90d3e62238a55bae63f12fbb2aeea4095377edd81f74bc', '2026-03-03 13:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(46, 1, '102室电表', 'DEV001', NULL, 1, NULL, 53.35, 12.54, 35.14, 2.19, 1.67, 1.81, '5af772540d6ea27f863919155fd71ed8c8151c61de02d78e194668a87060a107', '2026-03-03 14:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(47, 1, '102室电表', 'DEV001', NULL, 1, NULL, 56.07, 12.54, 37.86, 2.19, 1.67, 1.81, '03af27c6b153e3c03401cd95c19bb74a8bb0584f67af15e064fc8236c8d82b80', '2026-03-03 15:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(48, 1, '102室电表', 'DEV001', NULL, 1, NULL, 59.72, 12.54, 41.51, 2.19, 1.67, 1.81, 'ab2bfa7a26ea2b90918fc464db23cdeeba107c16f4ce07182337e3cae9c50349', '2026-03-03 16:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(49, 1, '102室电表', 'DEV001', NULL, 1, NULL, 62.57, 12.54, 44.36, 2.19, 1.67, 1.81, '3488726626868130663b8768ef51473623e4240a081f98e82d4b14955551844d', '2026-03-03 17:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(50, 1, '102室电表', 'DEV001', NULL, 1, NULL, 62.73, 12.54, 44.36, 2.35, 1.67, 1.81, 'b095a71d8c4b8905ba7d800bcdb54c983707eb7b67d93face14b1ccfc1784ad2', '2026-03-03 18:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(51, 1, '102室电表', 'DEV001', NULL, 1, NULL, 62.90, 12.54, 44.36, 2.52, 1.67, 1.81, '49352f38af24465d2f6e29eba56e2a21d448a36330202b9f8a87426eac81afcc', '2026-03-03 19:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(52, 1, '102室电表', 'DEV001', NULL, 1, NULL, 63.06, 12.54, 44.36, 2.68, 1.67, 1.81, 'fc8ba72ea21b8f2e456f64f6513a81e020a39bf2d195863733e74723a73e51df', '2026-03-03 20:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(53, 1, '102室电表', 'DEV001', NULL, 1, NULL, 63.23, 12.54, 44.36, 2.85, 1.67, 1.81, 'd7751e4b19e49eb33b2dd8c0580be0fa70c78ff935e2b6aad317602b310eb90f', '2026-03-03 21:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(54, 1, '102室电表', 'DEV001', NULL, 1, NULL, 63.37, 12.54, 44.36, 2.85, 1.81, 1.81, '18a0c73db6304e836c1e9c02b7121dbd48dbdfe622aa03962767fecd353c94e7', '2026-03-03 22:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(55, 1, '102室电表', 'DEV001', NULL, 1, NULL, 63.52, 12.54, 44.36, 2.85, 1.96, 1.81, '22a5e54d43ae68b456418edcb5c4b50c702ddaaccbba1e9d56d2f377fe75e849', '2026-03-03 23:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(56, 1, '102室电表', 'DEV001', NULL, 1, NULL, 63.67, 12.54, 44.36, 2.85, 1.96, 1.96, '4eb515d628643be019e5f4052fc466557e2a86b15b342dbf7d885b32f999f2a7', '2026-03-04 00:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(57, 1, '102室电表', 'DEV001', NULL, 1, NULL, 63.83, 12.54, 44.36, 2.85, 1.96, 2.12, '4636c6322ec09cf3a005e5f997fef32ae4311417da7fa2116e669dafcfd52a4f', '2026-03-04 01:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(58, 1, '102室电表', 'DEV001', NULL, 1, NULL, 63.97, 12.54, 44.36, 2.85, 1.96, 2.26, 'f2fb9b976c6c0a60a7ae51906464d897a31053ac5302edecde6a1dc785722ed1', '2026-03-04 02:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(59, 1, '102室电表', 'DEV001', NULL, 1, NULL, 64.12, 12.54, 44.36, 2.85, 1.96, 2.41, '76b84e3d08bb6db9fcdbc489c341fafa74865e44b170bee090782ad914bcfead', '2026-03-04 03:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(60, 1, '102室电表', 'DEV001', NULL, 1, NULL, 64.26, 12.54, 44.36, 2.85, 1.96, 2.55, '49c09f2bf3316a5304e967c5fdbe38457c81b835d944d972824890f8c5dc20c0', '2026-03-04 04:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(61, 1, '102室电表', 'DEV001', NULL, 1, NULL, 64.41, 12.54, 44.36, 2.85, 1.96, 2.70, '57b5130e9101320cb08ffff4eb824b8848d6cf8ec573e556e1ed779b6ccb0c81', '2026-03-04 05:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(62, 1, '102室电表', 'DEV001', NULL, 1, NULL, 64.58, 12.54, 44.36, 2.85, 2.13, 2.70, '3726c32ff2704dda3b51164aa503ac7e02504368351dc427826bd5ed610d4f45', '2026-03-04 06:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(63, 1, '102室电表', 'DEV001', NULL, 1, NULL, 64.72, 12.54, 44.36, 2.85, 2.27, 2.70, 'b7efb25add931dc72461a0b53208cc911bf9005312d3dca4b0db3d90fe2ed656', '2026-03-04 07:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(64, 1, '102室电表', 'DEV001', NULL, 1, NULL, 64.85, 12.54, 44.49, 2.85, 2.27, 2.70, '1c8bfc919127beaa7c913a1c3ec224b32dd15d082c00216969250577736fa0b3', '2026-03-04 08:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(65, 1, '102室电表', 'DEV001', NULL, 1, NULL, 67.86, 12.54, 47.50, 2.85, 2.27, 2.70, '64ad35a81cf4f921c6e0e845f9f68642449aa8530d1c39586c4f512845d17c7b', '2026-03-04 09:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(66, 1, '102室电表', 'DEV001', NULL, 1, NULL, 71.28, 12.54, 50.92, 2.85, 2.27, 2.70, '53a6335817ee504c3ed62ebca608565fea26fa8c34c69a13af77ed7f4d7adaf4', '2026-03-04 10:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(67, 1, '102室电表', 'DEV001', NULL, 1, NULL, 74.90, 16.16, 50.92, 2.85, 2.27, 2.70, '526f244967ccb97de0e66e25fc7b4dd2444438ad397e2c86fec87f16ad32279e', '2026-03-04 11:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(68, 1, '102室电表', 'DEV001', NULL, 1, NULL, 78.42, 19.68, 50.92, 2.85, 2.27, 2.70, 'fdf0ee865159c5658105678296829e37ef6e45a980974fa380f46ebcddb4f429', '2026-03-04 12:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(69, 1, '102室电表', 'DEV001', NULL, 1, NULL, 81.14, 19.68, 53.64, 2.85, 2.27, 2.70, 'ba867d1be511763db6f41d1427b8ca6c9e68d60844d388078725f7b9ff704125', '2026-03-04 13:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(70, 1, '102室电表', 'DEV001', NULL, 1, NULL, 84.28, 19.68, 56.78, 2.85, 2.27, 2.70, '2122c104eb20331b44448e5a80d0b1f0d7c625f77b1aaa2ebbb3da66af814b7e', '2026-03-04 14:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(71, 1, '102室电表', 'DEV001', NULL, 1, NULL, 87.61, 19.68, 60.11, 2.85, 2.27, 2.70, '73493ad1b7dcfb9dcdb023615df507f40e58762f3c51a7eca5c173ef57ea77ca', '2026-03-04 15:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(72, 1, '102室电表', 'DEV001', NULL, 1, NULL, 90.87, 19.68, 63.37, 2.85, 2.27, 2.70, '27a3741bd65478616e297e81dc30655fb16b53b1241b1390b3185e93dabbbf01', '2026-03-04 16:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(73, 1, '102室电表', 'DEV001', NULL, 1, NULL, 94.33, 19.68, 66.83, 2.85, 2.27, 2.70, 'b90f49c86710a7a5990d4b6f26deae72fe86c0a3407582ce5022ef43bf9b4152', '2026-03-04 17:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(74, 1, '102室电表', 'DEV001', NULL, 1, NULL, 94.47, 19.68, 66.83, 2.99, 2.27, 2.70, 'ae585453a55ab2a006dcbb90a29d056caf5413eb76162e404c7d81e433a68899', '2026-03-04 18:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(75, 1, '102室电表', 'DEV001', NULL, 1, NULL, 94.62, 19.68, 66.83, 3.14, 2.27, 2.70, '508714e14b1d5546fc4c3aa8a2f8b4a6160552c13461b1419b06cd8cb97c3b01', '2026-03-04 19:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(76, 1, '102室电表', 'DEV001', NULL, 1, NULL, 94.76, 19.68, 66.83, 3.28, 2.27, 2.70, 'ab9e42298e0134d340217bb61dca25582a3db66c978bbdb61841e45530af7dd7', '2026-03-04 20:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(77, 1, '102室电表', 'DEV001', NULL, 1, NULL, 94.91, 19.68, 66.83, 3.43, 2.27, 2.70, 'b31eaba25f5239da45cc56681c990a1d62a83b1669c8e1c2be540ea30c30468b', '2026-03-04 21:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(78, 1, '102室电表', 'DEV001', NULL, 1, NULL, 95.08, 19.68, 66.83, 3.43, 2.44, 2.70, '4a3a0422dc870d994318e7431c5d630ac742a5ccff37d3ffb0f93ec94a62792c', '2026-03-04 22:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(79, 1, '102室电表', 'DEV001', NULL, 1, NULL, 95.21, 19.68, 66.83, 3.43, 2.57, 2.70, '1fcbd9962ccb9506610b188ce0a782345b6c5a6f1e41a4746bc0e17a9e0b11a3', '2026-03-04 23:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(80, 1, '102室电表', 'DEV001', NULL, 1, NULL, 95.35, 19.68, 66.83, 3.43, 2.57, 2.84, 'b494cab4f721f6916c5fe52ce1878c4bf8fa326647b935b83ccdd35a049b44c4', '2026-03-05 00:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(81, 1, '102室电表', 'DEV001', NULL, 1, NULL, 95.50, 19.68, 66.83, 3.43, 2.57, 2.99, '4a11dc1bc83346382bcf8686cdc4d05a40f96c08d1103f303116aa72a1100607', '2026-03-05 01:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(82, 1, '102室电表', 'DEV001', NULL, 1, NULL, 95.67, 19.68, 66.83, 3.43, 2.57, 3.16, '466b8d17d12042317206d88de581a4b77440a5bcc5e7dfac5c2eb29f01767a71', '2026-03-05 02:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(83, 1, '102室电表', 'DEV001', NULL, 1, NULL, 95.80, 19.68, 66.83, 3.43, 2.57, 3.29, '2e7f3ae391df59e53819d32df3bf93c8378fda67c72a81ba8e2fb1f866fa91cb', '2026-03-05 03:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(84, 1, '102室电表', 'DEV001', NULL, 1, NULL, 95.97, 19.68, 66.83, 3.43, 2.57, 3.46, '5d5fd7b1cffff87ed1a61a7b8bde6b95a8f59e9885b8e3672ae793b9cdf4eba8', '2026-03-05 04:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(85, 1, '102室电表', 'DEV001', NULL, 1, NULL, 96.10, 19.68, 66.83, 3.43, 2.57, 3.59, '62b5467df6db8addf06195904d14b34999e6d6013ed2283fafa6f749cedba43f', '2026-03-05 05:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(86, 1, '102室电表', 'DEV001', NULL, 1, NULL, 96.25, 19.68, 66.83, 3.43, 2.72, 3.59, 'e85947286d60f7b68e71d49e02f9121240604b7b3d92a46d4e6fd00c9487039e', '2026-03-05 06:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(87, 1, '102室电表', 'DEV001', NULL, 1, NULL, 96.41, 19.68, 66.83, 3.43, 2.88, 3.59, '531da4a929d312c15a6f0b58eac6ce5d6b89815901e344a6779154e4463f63e4', '2026-03-05 07:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(88, 1, '102室电表', 'DEV001', NULL, 1, NULL, 96.57, 19.68, 66.99, 3.43, 2.88, 3.59, '990a3676aebc05ee86c8108c5a5f9276ff083a016b9e511559b36e8d22b9ee61', '2026-03-05 08:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(89, 1, '102室电表', 'DEV001', NULL, 1, NULL, 100.19, 19.68, 70.61, 3.43, 2.88, 3.59, '16a6094d1948b9d10415a52abd305d8d2a08bbb36e053112d9dc39d4feff3154', '2026-03-05 09:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(90, 1, '102室电表', 'DEV001', NULL, 1, NULL, 103.23, 19.68, 73.65, 3.43, 2.88, 3.59, '6b45fbc60cfa137fe1226299b4cffca82e7f6bff49307ae74959ce153f15d88a', '2026-03-05 10:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(91, 1, '102室电表', 'DEV001', NULL, 1, NULL, 106.46, 22.91, 73.65, 3.43, 2.88, 3.59, 'd124b71118a39ac9d33e5a9db18fd36917c77f9e0996dcef325ce0eb797778dc', '2026-03-05 11:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(92, 1, '102室电表', 'DEV001', NULL, 1, NULL, 109.60, 26.05, 73.65, 3.43, 2.88, 3.59, 'b8f743a0e90f890cd6c2f8a6a07349ebd013ff8d4b3d010aa78e2f0c44dcbb99', '2026-03-05 12:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(93, 1, '102室电表', 'DEV001', NULL, 1, NULL, 112.93, 26.05, 76.98, 3.43, 2.88, 3.59, 'd4f7932e82257c190533cee56a69168c653ec5ea8ebea34335bb575dcd486b98', '2026-03-05 13:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(94, 1, '102室电表', 'DEV001', NULL, 1, NULL, 115.68, 26.05, 79.73, 3.43, 2.88, 3.59, 'bb1fc45d09e7f9380b47a0448449ab5e177b61905e45a3bbe8a1907e431adc8a', '2026-03-05 14:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(95, 1, '102室电表', 'DEV001', NULL, 1, NULL, 118.62, 26.05, 82.67, 3.43, 2.88, 3.59, '091194b9ae75f76bcaf30a815ec0bd5929c7b5e3449945653bfda61dcd2886af', '2026-03-05 15:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(96, 1, '102室电表', 'DEV001', NULL, 1, NULL, 121.50, 26.05, 85.55, 3.43, 2.88, 3.59, '70865552c353cc98f8d1ff5ddc5b03a463b9e36c96057cb01edd48ecaa3c9fd3', '2026-03-05 16:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(97, 1, '102室电表', 'DEV001', NULL, 1, NULL, 124.57, 26.05, 88.62, 3.43, 2.88, 3.59, 'fb365c0bd31bd42676fb3cef6b8d070b31c436fd3b13f239b71a24200f599a42', '2026-03-05 17:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(98, 1, '102室电表', 'DEV001', NULL, 1, NULL, 124.74, 26.05, 88.62, 3.60, 2.88, 3.59, '8f213f083fda11366701d47b0a02942bbbd44a3bc8916e7a106b5e876a0dcb69', '2026-03-05 18:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(99, 1, '102室电表', 'DEV001', NULL, 1, NULL, 124.87, 26.05, 88.62, 3.73, 2.88, 3.59, 'bf2efcb179515793e7fe77b79b30423feb5ac94f3907fc7079a7fe4ce012e815', '2026-03-05 19:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(100, 1, '102室电表', 'DEV001', NULL, 1, NULL, 125.04, 26.05, 88.62, 3.90, 2.88, 3.59, 'e4ec4d656157f8171c453dadb9ec3de397f55c8044c31e491c86ff2af9d5ef2f', '2026-03-05 20:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(101, 1, '102室电表', 'DEV001', NULL, 1, NULL, 125.17, 26.05, 88.62, 4.03, 2.88, 3.59, '646054453b8f6b5e753f17b1d595651b9d15a85162524f02e0549b8b4843c873', '2026-03-05 21:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(102, 1, '102室电表', 'DEV001', NULL, 1, NULL, 125.32, 26.05, 88.62, 4.03, 3.03, 3.59, '30208bde9d071bc8cc0836e84379e04e32d3346515624ed478d46b8fb29d9f25', '2026-03-05 22:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(103, 1, '102室电表', 'DEV001', NULL, 1, NULL, 125.48, 26.05, 88.62, 4.03, 3.19, 3.59, 'c63c98ffbcee94c309f9236d6b12b0f6c55056250ea464aae2a483f8df9ace14', '2026-03-05 23:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(104, 1, '102室电表', 'DEV001', NULL, 1, NULL, 125.65, 26.05, 88.62, 4.03, 3.19, 3.76, '298bce5d23dd3182dea1aa91c2f829e7c250491fc061904482907e0665b581fc', '2026-03-06 00:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(105, 1, '102室电表', 'DEV001', NULL, 1, NULL, 125.78, 26.05, 88.62, 4.03, 3.19, 3.89, '4873a3c77629a8e5677281cb29cd55b605e6c3f673c4be6d621cf6dc71a58d90', '2026-03-06 01:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(106, 1, '102室电表', 'DEV001', NULL, 1, NULL, 125.93, 26.05, 88.62, 4.03, 3.19, 4.04, 'f32974f8944da9d863565dbff2c9f0e513a0bcfdd271ce09290f0a8646a17364', '2026-03-06 02:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(107, 1, '102室电表', 'DEV001', NULL, 1, NULL, 126.09, 26.05, 88.62, 4.03, 3.19, 4.20, 'b5bc6c88dadf4b85e44f55b3b5a0adfbad0573391571e1585e2636f6ad260ba1', '2026-03-06 03:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(108, 1, '102室电表', 'DEV001', NULL, 1, NULL, 126.24, 26.05, 88.62, 4.03, 3.19, 4.35, '4fdc27bcfce300f80eafeb8844d98d4728ddda481edae95580b101701464539c', '2026-03-06 04:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(109, 1, '102室电表', 'DEV001', NULL, 1, NULL, 126.40, 26.05, 88.62, 4.03, 3.19, 4.51, '9c3ccb0252d66649c598639386d5c5df6a15931e9387697c6d298f169a3ab93b', '2026-03-06 05:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(110, 1, '102室电表', 'DEV001', NULL, 1, NULL, 126.54, 26.05, 88.62, 4.03, 3.33, 4.51, 'a6da6bd9af11658e495e04a0745018bb91cf5ae25f3341309f9d68f8703b2757', '2026-03-06 06:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(111, 1, '102室电表', 'DEV001', NULL, 1, NULL, 126.69, 26.05, 88.62, 4.03, 3.48, 4.51, 'bb23e468cadcc248f7f97047f9444f635a3b2fc32ee1eab2ed4e442d5e67f7ec', '2026-03-06 07:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(112, 1, '102室电表', 'DEV001', NULL, 1, NULL, 126.83, 26.05, 88.76, 4.03, 3.48, 4.51, 'b795ffc42f6adfb5edb755a361397f7d53274aa8555af0dfd32bd01bcd2e3e70', '2026-03-06 08:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(113, 1, '102室电表', 'DEV001', NULL, 1, NULL, 130.09, 26.05, 92.02, 4.03, 3.48, 4.51, '1367db0bf39ae879e3ba2d42f80f59bd1d6120b5c8534daeb19b55f8d4602613', '2026-03-06 09:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(114, 1, '102室电表', 'DEV001', NULL, 1, NULL, 133.77, 26.05, 95.70, 4.03, 3.48, 4.51, '55c9a055351b9e60792bfb1fa9854d48bc753ff74dbf637bb7aa73143de5048a', '2026-03-06 10:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(115, 1, '102室电表', 'DEV001', NULL, 1, NULL, 136.65, 28.93, 95.70, 4.03, 3.48, 4.51, '16d32dca5c96ba2acc11015b49f4b1b123c0bcffe69589a8c85f7fb590b00606', '2026-03-06 11:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(116, 1, '102室电表', 'DEV001', NULL, 1, NULL, 139.43, 31.71, 95.70, 4.03, 3.48, 4.51, '9270639e0213cf2d9cf9b5fe956c812f557672a82f485f6e65670584574f9ee9', '2026-03-06 12:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(117, 1, '102室电表', 'DEV001', NULL, 1, NULL, 142.41, 31.71, 98.68, 4.03, 3.48, 4.51, '570d732c98f1e2d9a4fa2754cc9274f0a51f3e4029f112a34164d9b8c87ef7ff', '2026-03-06 13:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(118, 1, '102室电表', 'DEV001', NULL, 1, NULL, 145.80, 31.71, 102.07, 4.03, 3.48, 4.51, '91d977c6d5375434aad5da9c4a436ffc8c36ebe7b965a128b361f08e6b4905ed', '2026-03-06 14:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(119, 1, '102室电表', 'DEV001', NULL, 1, NULL, 149.38, 31.71, 105.65, 4.03, 3.48, 4.51, '1eb6f363623b1e85d26c6c262d3c319b70f059995a4e94a02b0409e557acd2ed', '2026-03-06 15:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(120, 1, '102室电表', 'DEV001', NULL, 1, NULL, 152.90, 31.71, 109.17, 4.03, 3.48, 4.51, 'a1afd5443fe5323b2124304ead07645b7e43fc5fc8f6b9e02610d268beeafa2e', '2026-03-06 16:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(121, 1, '102室电表', 'DEV001', NULL, 1, NULL, 155.62, 31.71, 111.89, 4.03, 3.48, 4.51, '3bbdf7cd509207f862971b3293e42fd84d3de5413a7db9989b283010c934bc9d', '2026-03-06 17:00:08', '2026-03-29 15:28:29', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(122, 1, '102室电表', 'DEV001', NULL, 1, NULL, 155.77, 31.71, 111.89, 4.18, 3.48, 4.51, '652cd3c7aa53c2aefdbb6dd924962d6efa2607bdcfdd82caa0297b7dcf5edf8d', '2026-03-06 18:00:08', '2026-03-29 15:28:29', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(123, 1, '102室电表', 'DEV001', NULL, 1, NULL, 155.93, 31.71, 111.89, 4.34, 3.48, 4.51, 'fdf0af78fa8ef34f23afe8c707b6792e81800e0ba91bcc53b29aba96c790b10f', '2026-03-06 19:00:08', '2026-03-29 15:28:29', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(124, 1, '102室电表', 'DEV001', NULL, 1, NULL, 156.08, 31.71, 111.89, 4.49, 3.48, 4.51, 'e01547aa70fca29bfb7d672b15ac5ed3a8157c2a95c052de78871315c5218169', '2026-03-06 20:00:08', '2026-03-29 15:28:30', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(125, 1, '102室电表', 'DEV001', NULL, 1, NULL, 156.38, 31.71, 111.89, 4.65, 3.62, 4.51, '6e86ac1f359ae66706819b1781138e317e90837b91a78485f44fa8fbb9f65b02', '2026-03-06 22:00:08', '2026-03-29 15:28:30', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(126, 1, '102室电表', 'DEV001', NULL, 1, NULL, 157.35, 31.71, 111.89, 4.65, 3.77, 5.33, 'd8b61221eba82f1033bb86ddc80135945bc6989073964a3490ca795b0acb72bb', '2026-03-07 01:00:08', '2026-03-29 15:28:30', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(127, 1, '102室电表', 'DEV001', NULL, 1, NULL, 156.24, 31.71, 111.89, 4.65, 3.48, 4.51, '3ce0249fa94e27f07d04949e5ff2c82eb65841ac9df5a7f3aad2d6b078d7ccf5', '2026-03-06 21:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(128, 1, '102室电表', 'DEV001', NULL, 1, NULL, 156.93, 31.71, 111.89, 4.65, 3.77, 4.91, 'd00fe2b68394a2b57f5e3bbd4e327d08ae526f0462292ceb1db8e2aac82d815a', '2026-03-07 00:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(129, 1, '102室电表', 'DEV001', NULL, 1, NULL, 158.07, 31.71, 111.89, 4.65, 3.77, 6.05, '2749d1277532cd3060d5df116628b092c72459fa26f240ee30e094be91aa7664', '2026-03-07 03:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(130, 1, '102室电表', 'DEV001', NULL, 1, NULL, 156.53, 31.71, 111.89, 4.65, 3.77, 4.51, '33448dea6c63876eebc06381307c4b2c0c17ca85b0051b55cc4abfffa9635ac0', '2026-03-06 23:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(131, 1, '102室电表', 'DEV001', NULL, 1, NULL, 158.43, 31.71, 111.89, 4.65, 3.77, 6.41, 'c76bed82b393e0b0bd10065d8aac5d5e20715d1fdef04ad47d48f4fdf2fc858e', '2026-03-07 04:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(132, 1, '102室电表', 'DEV001', NULL, 1, NULL, 159.25, 31.71, 111.89, 4.65, 4.21, 6.79, 'c64f6382597441844acad2c347e2cebc00b34c922c82d52fba990966019b86bc', '2026-03-07 06:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(133, 1, '102室电表', 'DEV001', NULL, 1, NULL, 159.59, 31.71, 111.89, 4.65, 4.55, 6.79, '27ea87d3ba1672b219703eeab5120c0dfea6e70274541689be5875a22a90eaaf', '2026-03-07 07:00:08', '2026-03-29 15:28:32', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(134, 1, '102室电表', 'DEV001', NULL, 1, NULL, 160.41, 31.71, 112.71, 4.65, 4.55, 6.79, '4475ea0caf65f7c8b71f04c56a51fce8ba8c22f60c558733653fec5144c103b0', '2026-03-07 09:00:08', '2026-03-29 15:28:32', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(135, 1, '102室电表', 'DEV001', NULL, 1, NULL, 160.82, 31.71, 113.12, 4.65, 4.55, 6.79, 'beeac47c6956546c80f4a2b00df119550252343019856ee9a6caa1c175421632', '2026-03-07 10:00:08', '2026-03-29 15:28:32', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(136, 1, '102室电表', 'DEV001', NULL, 1, NULL, 161.68, 32.57, 113.12, 4.65, 4.55, 6.79, '4dd807069ca87a916689b9313d9093c4d27d27351a033cbc47d7771b687e2d66', '2026-03-07 12:00:08', '2026-03-29 15:28:33', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(137, 1, '102室电表', 'DEV001', NULL, 1, NULL, 162.13, 32.57, 113.57, 4.65, 4.55, 6.79, '1ea7049eaf3cb1959e9e9a1ab6440e180807dba3fb3dc1224794b384da1d5682', '2026-03-07 13:00:08', '2026-03-29 15:28:33', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(138, 1, '102室电表', 'DEV001', NULL, 1, NULL, 162.91, 32.57, 114.35, 4.65, 4.55, 6.79, 'bb4cd89fac4faedd96cb889f3bf171a9a4cfabd2770a229433f9fe9124293ffd', '2026-03-07 15:00:08', '2026-03-29 15:28:33', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(139, 1, '102室电表', 'DEV001', NULL, 1, NULL, 163.30, 32.57, 114.74, 4.65, 4.55, 6.79, '5e34977eae410bfa5e49679d691046a03612c9235bfdd83415c78aa5c96e8749', '2026-03-07 16:00:08', '2026-03-29 15:28:33', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(140, 1, '102室电表', 'DEV001', NULL, 1, NULL, 164.07, 32.57, 115.16, 5.00, 4.55, 6.79, '2bb18f805f92b69fd1a73bbf06e9e444fe7c4ba983825c1010f7e434c5a1052e', '2026-03-07 18:00:08', '2026-03-29 15:28:34', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(141, 1, '102室电表', 'DEV001', NULL, 1, NULL, 164.45, 32.57, 115.16, 5.38, 4.55, 6.79, 'b6bd42e91c58e80324b060d18a4d80d2ac9788ccb24cc58982154f12d6ae9289', '2026-03-07 19:00:08', '2026-03-29 15:28:34', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(142, 1, '102室电表', 'DEV001', NULL, 1, NULL, 165.20, 32.57, 115.16, 6.13, 4.55, 6.79, '6a3892d3438b12417df2c2c94336e6a66796e963eba2642847cf437de682aeb0', '2026-03-07 21:00:08', '2026-03-29 15:28:34', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(143, 1, '102室电表', 'DEV001', NULL, 1, NULL, 165.64, 32.57, 115.16, 6.13, 4.99, 6.79, 'af56b6aa3d1b6c8c5d51400d6bd1bfd7d0154cd80530bd2c3182352514ffb506', '2026-03-07 22:00:08', '2026-03-29 15:28:35', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(144, 1, '102室电表', 'DEV001', NULL, 1, NULL, 166.33, 32.57, 115.16, 6.13, 5.33, 7.14, 'a1dd5bfad08b083869fe28943bf86a4675b9b13a556967c13c76c2724614949b', '2026-03-08 00:00:08', '2026-03-29 15:28:35', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(145, 1, '102室电表', 'DEV001', NULL, 1, NULL, 166.70, 32.57, 115.16, 6.13, 5.33, 7.51, '823a33953e8cdb0b4e0732169d7de386ceb128a1d66865db01dc7e67e8f0c6f6', '2026-03-08 01:00:08', '2026-03-29 15:28:35', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(146, 1, '102室电表', 'DEV001', NULL, 1, NULL, 167.57, 32.57, 115.16, 6.13, 5.33, 8.38, 'cf707d3986e5a49f29fbd2b78118f6d8af125a271aa7159747a7d986d34d70c6', '2026-03-08 03:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(147, 1, '102室电表', 'DEV001', NULL, 1, NULL, 168.01, 32.57, 115.16, 6.13, 5.33, 8.82, '9a893c85644a6eb80a5a1af8534732f6ce661d5d3425de717dfdf5d74b1e0642', '2026-03-08 04:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(148, 1, '102室电表', 'DEV001', NULL, 1, NULL, 157.70, 31.71, 111.89, 4.65, 3.77, 5.68, 'cdce78d88e655fe7d09883c58dd5546eb4131d8a2c2d80bd1fefa658ef1e2e86', '2026-03-07 02:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(149, 1, '102室电表', 'DEV001', NULL, 1, NULL, 158.81, 31.71, 111.89, 4.65, 3.77, 6.79, '4aeada19feab6b5e012cdcbd03f24809b898f1b5cba0661d695eea1590334f91', '2026-03-07 05:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(150, 1, '102室电表', 'DEV001', NULL, 1, NULL, 160.05, 31.71, 112.35, 4.65, 4.55, 6.79, '4ec30fd58ce4cca376373952e757e564cf59fd78580e51f6cfc75b95b3562a77', '2026-03-07 08:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(151, 1, '102室电表', 'DEV001', NULL, 1, NULL, 161.26, 32.15, 113.12, 4.65, 4.55, 6.79, '03aaedfbb6b84193b3c5ed0c5a11bc11d54c4828e019d4a1e321335ad00f8a5a', '2026-03-07 11:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(152, 1, '102室电表', 'DEV001', NULL, 1, NULL, 162.51, 32.57, 113.95, 4.65, 4.55, 6.79, 'd13aaae43fb92ec6c7cc8c285a0f2ee22bb24c99ace627f751c1f0269e3d6546', '2026-03-07 14:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(153, 1, '102室电表', 'DEV001', NULL, 1, NULL, 163.72, 32.57, 115.16, 4.65, 4.55, 6.79, '6eeb74a0de2ae380f9bd724e80f5976edae63c0782168130b5720afb9918f168', '2026-03-07 17:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(154, 1, '102室电表', 'DEV001', NULL, 1, NULL, 164.81, 32.57, 115.16, 5.74, 4.55, 6.79, 'df51e4a2471b3295dbc2c169f7c6d2148aa033d447aeeea8c8f1b88c5005f8d8', '2026-03-07 20:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(155, 1, '102室电表', 'DEV001', NULL, 1, NULL, 165.98, 32.57, 115.16, 6.13, 5.33, 6.79, '5c805b27c8f10897e764da08d645d80dc46a7c7394d3111ce82c131cf734a30a', '2026-03-07 23:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(156, 1, '102室电表', 'DEV001', NULL, 1, NULL, 167.12, 32.57, 115.16, 6.13, 5.33, 7.93, 'e11ad9fbf1a819f67070471d7a5c98ea5804e1102d49424715d490c86da15e02', '2026-03-08 02:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(157, 1, '102室电表', 'DEV001', NULL, 1, NULL, 168.47, 32.57, 115.16, 6.13, 5.33, 9.28, '4ab9778f751b06fe5116e7ea1ecda7a2457cb22a160de0917639de9dea54527e', '2026-03-08 05:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(158, 1, '102室电表', 'DEV001', NULL, 1, NULL, 168.87, 32.57, 115.16, 6.13, 5.73, 9.28, 'b98ec4249b8eb34e45d65c3447abf2e666b27a4c52ad703818e6a6352b24656a', '2026-03-08 06:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(159, 1, '102室电表', 'DEV001', NULL, 1, NULL, 169.29, 32.57, 115.16, 6.13, 6.15, 9.28, '18f8b2aa6babf7b50341d07d0d38c1908c02728a696e34265f4ebf237c1adea0', '2026-03-08 07:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(160, 1, '102室电表', 'DEV001', NULL, 1, NULL, 169.70, 32.57, 115.57, 6.13, 6.15, 9.28, 'dae249bb8f4151bb397c6c9d1a7ad667c34a11973b4795aef5f9d6cb16c896ea', '2026-03-08 08:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(161, 1, '102室电表', 'DEV001', NULL, 1, NULL, 170.14, 32.57, 116.01, 6.13, 6.15, 9.28, '86bca1fc74a7ca1a054ed016ccff23b5023cee5c81b09a674e20c6a0055dd5b7', '2026-03-08 09:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(162, 1, '102室电表', 'DEV001', NULL, 1, NULL, 170.50, 32.57, 116.37, 6.13, 6.15, 9.28, '297899298a5bce5631b2921843c7ad9f17b572fc94e25c4391f3c0c1d2493e8a', '2026-03-08 10:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(163, 1, '102室电表', 'DEV001', NULL, 1, NULL, 170.89, 32.96, 116.37, 6.13, 6.15, 9.28, '3dd8e42f77b929ca86b1bcbba1d5524eb69ba7b4af3048925abdc010fcc97f41', '2026-03-08 11:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(164, 1, '102室电表', 'DEV001', NULL, 1, NULL, 171.27, 33.34, 116.37, 6.13, 6.15, 9.28, '9591d7fb952c06f672100a574b7787e8504ca29519ceabb7e48b7c6f31e38353', '2026-03-08 12:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(165, 1, '102室电表', 'DEV001', NULL, 1, NULL, 171.67, 33.34, 116.77, 6.13, 6.15, 9.28, '4c7e173b5913d1142fc35e63f769d10c044d9f5fc5760b13bdacb2ecf292fd64', '2026-03-08 13:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(166, 1, '102室电表', 'DEV001', NULL, 1, NULL, 172.12, 33.34, 117.22, 6.13, 6.15, 9.28, 'fc1f841c1eaf28a7a75dd3edebfbb77985101446d928aaaafbfe126a7c37d5af', '2026-03-08 14:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(167, 1, '102室电表', 'DEV001', NULL, 1, NULL, 172.47, 33.34, 117.57, 6.13, 6.15, 9.28, 'b31976786e850bafdf87a9d4d1a0f4c9525a6156e2f4ac88bad329af7934f189', '2026-03-08 15:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(168, 1, '102室电表', 'DEV001', NULL, 1, NULL, 172.81, 33.34, 117.91, 6.13, 6.15, 9.28, '69ef97179941a2c38314cfdb3ce53ff69ae58698c4cdad9e4a95b6b924224af1', '2026-03-08 16:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(169, 1, '102室电表', 'DEV001', NULL, 1, NULL, 173.18, 33.34, 118.28, 6.13, 6.15, 9.28, '12919b5140217d9137ddc6f84cc642d10b3598871a063dc4a9679a88c93a7bf1', '2026-03-08 17:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(170, 1, '102室电表', 'DEV001', NULL, 1, NULL, 173.61, 33.34, 118.28, 6.56, 6.15, 9.28, '3a2ad8a066008632ad72c67becf5e26831ae0294145671554e8c169a859444a6', '2026-03-08 18:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(171, 1, '102室电表', 'DEV001', NULL, 1, NULL, 174.06, 33.34, 118.28, 7.01, 6.15, 9.28, 'b5fd1842be0a58ccd0403d4836c9d8b8eb88dc2ef182dac808bfa3fb87594560', '2026-03-08 19:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(172, 1, '102室电表', 'DEV001', NULL, 1, NULL, 174.50, 33.34, 118.28, 7.45, 6.15, 9.28, 'e7820e60a655ddfc6f153e950d7e01875d11a94778ee57e3b229c13e05f72391', '2026-03-08 20:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(173, 1, '102室电表', 'DEV001', NULL, 1, NULL, 174.84, 33.34, 118.28, 7.79, 6.15, 9.28, 'd1186d8479c42a833950e069a44fe0909b3c528cbf9fefa7f1c87112bc8ca68d', '2026-03-08 21:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(174, 1, '102室电表', 'DEV001', NULL, 1, NULL, 175.23, 33.34, 118.28, 7.79, 6.54, 9.28, 'bb30e09645c56c8de27e7e5ac89dbcdfaa332f0b849627a1de8effc3e5cf0b20', '2026-03-08 22:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(175, 1, '102室电表', 'DEV001', NULL, 1, NULL, 175.65, 33.34, 118.28, 7.79, 6.96, 9.28, '37cfbff134b72f36a227065e704132d55b814f289790ca85384f40f692df7a93', '2026-03-08 23:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(176, 1, '102室电表', 'DEV001', NULL, 1, NULL, 175.81, 33.34, 118.28, 7.79, 6.96, 9.44, '7d0ba0f7d294d1e748dbf4bede42908d26eb8d4e06a9d29e467b990da20e9e73', '2026-03-09 00:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(177, 1, '102室电表', 'DEV001', NULL, 1, NULL, 175.98, 33.34, 118.28, 7.79, 6.96, 9.61, '4020b22e0190d24f5a3e448d1988e7c9306c73daa1216a22ac40998d32cc30d3', '2026-03-09 01:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(178, 1, '102室电表', 'DEV001', NULL, 1, NULL, 176.12, 33.34, 118.28, 7.79, 6.96, 9.75, '01690d92c15bccf9d376551af64cd1dfa0baf5f14799704fc7b46e6945fbea0a', '2026-03-09 02:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(179, 1, '102室电表', 'DEV001', NULL, 1, NULL, 176.27, 33.34, 118.28, 7.79, 6.96, 9.90, 'daf9bae5a1e8a45e36cdf143ebacae1fec0cc288f2c792bbde3de98ecb5dc67b', '2026-03-09 03:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(180, 1, '102室电表', 'DEV001', NULL, 1, NULL, 176.42, 33.34, 118.28, 7.79, 6.96, 10.05, 'ee7db2416fd6f03bf3a4a8f8d8e95c4d5e0c498445510225c5f92246d21c988b', '2026-03-09 04:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(181, 1, '102室电表', 'DEV001', NULL, 1, NULL, 176.57, 33.34, 118.28, 7.79, 6.96, 10.20, '45afe4bfe35baf0a0179bb998af04dfa4684be8326001150db2a95e9726ecdb7', '2026-03-09 05:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(182, 1, '102室电表', 'DEV001', NULL, 1, NULL, 176.70, 33.34, 118.28, 7.79, 7.09, 10.20, '673bcffa258c6babeff6b455a1e65a744392fa1742c33b432e239a734f9b2433', '2026-03-09 06:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(183, 1, '102室电表', 'DEV001', NULL, 1, NULL, 176.84, 33.34, 118.28, 7.79, 7.23, 10.20, '953ee8ca24c9c11a93a6a0f5dc43dc855d99d6004db3a9286bb59849ac627d49', '2026-03-09 07:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(184, 1, '102室电表', 'DEV001', NULL, 1, NULL, 176.98, 33.34, 118.42, 7.79, 7.23, 10.20, '832641d618975ff853c4e1dabf60454f1269d3468994b5dd107cf79ea3e2c343', '2026-03-09 08:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(185, 1, '102室电表', 'DEV001', NULL, 1, NULL, 180.08, 33.34, 121.52, 7.79, 7.23, 10.20, '663bc66f2725444a4574c6fcaba0178340ae879971cb3a3a3b72ff66ee5956b5', '2026-03-09 09:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(186, 1, '102室电表', 'DEV001', NULL, 1, NULL, 183.60, 33.34, 125.04, 7.79, 7.23, 10.20, '47f9f860eccc7a1607d3c878c70222e8f71a3e21a06c7e1b37d2703c26822c98', '2026-03-09 10:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(187, 1, '102室电表', 'DEV001', NULL, 1, NULL, 186.32, 36.06, 125.04, 7.79, 7.23, 10.20, '7fd812e9dc25ae7204d907c86eb0ac7dc6590c07381d0ea18cd9faa0604cd856', '2026-03-09 11:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(188, 1, '102室电表', 'DEV001', NULL, 1, NULL, 189.94, 39.68, 125.04, 7.79, 7.23, 10.20, '2e2fedbec6cddcc597ada4455a9fa7a5d7be886c9a254cd71b985c165d5209b8', '2026-03-09 12:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(189, 1, '102室电表', 'DEV001', NULL, 1, NULL, 192.76, 39.68, 127.86, 7.79, 7.23, 10.20, '8d9f77defa0cce43f641f494e176eafb350ddc97fee5dfe411d0d1dcf2e3fb2a', '2026-03-09 13:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(190, 1, '102室电表', 'DEV001', NULL, 1, NULL, 195.99, 39.68, 131.09, 7.79, 7.23, 10.20, 'e76a45765190bd433ad35ec75c6750e68d36ce1007c2669db545165d6884fd43', '2026-03-09 14:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(191, 1, '102室电表', 'DEV001', NULL, 1, NULL, 199.41, 39.68, 134.51, 7.79, 7.23, 10.20, 'a80812a05c2e7daf2a263f0304b80ee890e146ffaabc0b7c03f989547663eb84', '2026-03-09 15:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(192, 1, '102室电表', 'DEV001', NULL, 1, NULL, 202.77, 39.68, 137.87, 7.79, 7.23, 10.20, '26b845c423d9f182eef2b0d90906716258231da86e3263f7940e7543d44c3d86', '2026-03-09 16:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(193, 1, '102室电表', 'DEV001', NULL, 1, NULL, 206.32, 39.68, 141.42, 7.79, 7.23, 10.20, '4889badb214c751639cda3287e847327525662d423fc71c8385f2e97a301f971', '2026-03-09 17:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(194, 1, '102室电表', 'DEV001', NULL, 1, NULL, 206.46, 39.68, 141.42, 7.93, 7.23, 10.20, '735d867d735ba31306d859bbdf418ef7d4259236794e4901f5140db35638b1b0', '2026-03-09 18:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(195, 1, '102室电表', 'DEV001', NULL, 1, NULL, 206.61, 39.68, 141.42, 8.08, 7.23, 10.20, 'de6aeea7dc2387d5647f6ce4295867dd34e00b0da6c83e8ab468065ab5d656a4', '2026-03-09 19:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(196, 1, '102室电表', 'DEV001', NULL, 1, NULL, 206.76, 39.68, 141.42, 8.23, 7.23, 10.20, '636cb8f8ea1a92dea23535f1758d3fdf7e5e1d88ae8a85024b987b59463ba13a', '2026-03-09 20:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(197, 1, '102室电表', 'DEV001', NULL, 1, NULL, 206.92, 39.68, 141.42, 8.39, 7.23, 10.20, 'f6af3c25fef8c8612072749f87578994c41675a959e399c2103e953029705e15', '2026-03-09 21:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(198, 1, '102室电表', 'DEV001', NULL, 1, NULL, 207.05, 39.68, 141.42, 8.39, 7.36, 10.20, '95a25a328d2a95d14d98227728ea633d436925d63336e28bac86aa09be756bfc', '2026-03-09 22:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(199, 1, '102室电表', 'DEV001', NULL, 1, NULL, 207.19, 39.68, 141.42, 8.39, 7.50, 10.20, 'f38daec2b42d2354ef228ba98f1d79556c03e716cfb6701cf2908a35185af760', '2026-03-09 23:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(200, 1, '102室电表', 'DEV001', NULL, 1, NULL, 207.33, 39.68, 141.42, 8.39, 7.50, 10.34, 'eb79f6d3d4c007c2969bc014551fddf35482276e9f6fd14219f08b5c79f335bc', '2026-03-10 00:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(201, 1, '102室电表', 'DEV001', NULL, 1, NULL, 207.48, 39.68, 141.42, 8.39, 7.50, 10.49, '28d639055547ed339a91d809a8eb3818e4f33f2431734e4d60b3df5d2a08bfe7', '2026-03-10 01:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(202, 1, '102室电表', 'DEV001', NULL, 1, NULL, 207.65, 39.68, 141.42, 8.39, 7.50, 10.66, '25e22dd2b26cde969d99b9d9cb9f444cb5368ee788985989978cb4c026fe7ed9', '2026-03-10 02:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(203, 1, '102室电表', 'DEV001', NULL, 1, NULL, 207.78, 39.68, 141.42, 8.39, 7.50, 10.79, '083239dbecaa16e89981c86996f5b2463437ea4769a074ec85e168482a6abb98', '2026-03-10 03:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(204, 1, '102室电表', 'DEV001', NULL, 1, NULL, 207.91, 39.68, 141.42, 8.39, 7.50, 10.92, '1dc91a9c85f862f23df4e23dd4a09b548586491de3d89a7499f933a5adb1edba', '2026-03-10 04:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(205, 1, '102室电表', 'DEV001', NULL, 1, NULL, 208.05, 39.68, 141.42, 8.39, 7.50, 11.06, 'f5e5d1d705cc5ba003bc45e58a7bf61d8eec26dd007f7bbd89cb6cf382fe32a8', '2026-03-10 05:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(206, 1, '102室电表', 'DEV001', NULL, 1, NULL, 208.21, 39.68, 141.42, 8.39, 7.66, 11.06, '51e212cd97dabcb34637532ee86258d45b39181acea5c95617c79c20c0e7e6c0', '2026-03-10 06:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(207, 1, '102室电表', 'DEV001', NULL, 1, NULL, 208.38, 39.68, 141.42, 8.39, 7.83, 11.06, '605aadb6a0b146bbccd110f0e9d8c8874ecfa1c5f638685d6dfc72ea851e7cb1', '2026-03-10 07:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(208, 1, '102室电表', 'DEV001', NULL, 1, NULL, 208.55, 39.68, 141.59, 8.39, 7.83, 11.06, '98c9fcae81a775849dfed240e18fd7930bc441fc0d05df943534979f66138451', '2026-03-10 08:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(209, 1, '102室电表', 'DEV001', NULL, 1, NULL, 211.30, 39.68, 144.34, 8.39, 7.83, 11.06, '80d894ae9b2ff119f05b73736bfdd2255aa9fa5924483492fe9c7862b744c1ca', '2026-03-10 09:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(210, 1, '102室电表', 'DEV001', NULL, 1, NULL, 214.47, 39.68, 147.51, 8.39, 7.83, 11.06, 'b05caa0c7265dc9a7e3d7e512d058baa4af8d55d856eab9351cf9ca65d0e1e6b', '2026-03-10 10:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(211, 1, '102室电表', 'DEV001', NULL, 1, NULL, 217.83, 43.04, 147.51, 8.39, 7.83, 11.06, '22f510a5977f9a37e58fc4ace7a7d8d0338a53b9e3aeb18e93ded2836b6b6208', '2026-03-10 11:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(212, 1, '102室电表', 'DEV001', NULL, 1, NULL, 221.09, 46.30, 147.51, 8.39, 7.83, 11.06, '63e138c3f1fde87b114143e0a083c0c40cf53e2e6012df0e78c475957e73a6d8', '2026-03-10 12:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(213, 1, '102室电表', 'DEV001', NULL, 1, NULL, 224.55, 46.30, 150.97, 8.39, 7.83, 11.06, '59f053e0dc36bf7cd77e3ac1851a0442eb776e4133efa270cd5db1b960df8074', '2026-03-10 13:00:08', '2026-03-29 15:28:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(214, 1, '102室电表', 'DEV001', NULL, 1, NULL, 227.43, 46.30, 153.85, 8.39, 7.83, 11.06, '4fc5c8a449f5925c26f42f7e235cb5ade92daf8300ac2a86cd2e8470a0d74b95', '2026-03-10 14:00:08', '2026-03-29 15:28:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(215, 1, '102室电表', 'DEV001', NULL, 1, NULL, 230.50, 46.30, 156.92, 8.39, 7.83, 11.06, '3a56aac52c288eed573e74ba58c4a95904f29aa8846246ca8dc63d498ddb5249', '2026-03-10 15:00:08', '2026-03-29 15:28:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(216, 1, '102室电表', 'DEV001', NULL, 1, NULL, 233.51, 46.30, 159.93, 8.39, 7.83, 11.06, 'f00f4c521cd8ce4d86182f806955042d62241f249acf6d30ea93b6ef72ffd005', '2026-03-10 16:00:08', '2026-03-29 15:28:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(217, 1, '102室电表', 'DEV001', NULL, 1, NULL, 236.71, 46.30, 163.13, 8.39, 7.83, 11.06, 'f5c4a9d24d16e51a29c86fe2de7fb5f83981c2f1c4a04c43a6478df99d6b7f57', '2026-03-10 17:00:08', '2026-03-29 15:28:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(218, 1, '102室电表', 'DEV001', NULL, 1, NULL, 236.88, 46.30, 163.13, 8.56, 7.83, 11.06, '12628a96436ffdd60748e1ff77901970976771bd846e8f18b2a724c08b27b113', '2026-03-10 18:00:08', '2026-03-29 15:28:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(219, 1, '102室电表', 'DEV001', NULL, 1, NULL, 237.02, 46.30, 163.13, 8.70, 7.83, 11.06, 'a904012e5317bbaca242facaa6bc4080b0fe4cbe451608857081676016f5fd6d', '2026-03-10 19:00:08', '2026-03-29 15:28:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(220, 1, '102室电表', 'DEV001', NULL, 1, NULL, 237.15, 46.30, 163.13, 8.83, 7.83, 11.06, '11921af7f5a886379675c9676b36ddd5199a5ede5bd0aa63e8c28fcebd532c15', '2026-03-10 20:00:08', '2026-03-29 15:28:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(221, 1, '102室电表', 'DEV001', NULL, 1, NULL, 237.29, 46.30, 163.13, 8.97, 7.83, 11.06, '46d6525ff21bc499f22fd6000de2ca3be3dd3fbc37d393044f4444c1682171a7', '2026-03-10 21:00:08', '2026-03-29 15:28:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(222, 1, '102室电表', 'DEV001', NULL, 1, NULL, 237.45, 46.30, 163.13, 8.97, 7.99, 11.06, '1a1514b3dbc17ba188e0bf449f927bffada983e2194c433ff14df1b5a29c8b70', '2026-03-10 22:00:08', '2026-03-29 15:28:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(223, 1, '102室电表', 'DEV001', NULL, 1, NULL, 237.62, 46.30, 163.13, 8.97, 8.16, 11.06, '64c3b02a7d5d12a8a53e57a35216a40d2796a7f0af9e7a44c51cd01e86d465f5', '2026-03-10 23:00:08', '2026-03-29 15:28:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(224, 1, '102室电表', 'DEV001', NULL, 1, NULL, 237.79, 46.30, 163.13, 8.97, 8.16, 11.23, 'cf6eb0e2ece768b7347ce6249093a7568ea621d837cfb6ebcd4d8d2cd50b56c8', '2026-03-11 00:00:08', '2026-03-29 15:28:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(225, 1, '102室电表', 'DEV001', NULL, 1, NULL, 237.92, 46.30, 163.13, 8.97, 8.16, 11.36, '0422e4c9df739475b3c2e4d17d4fc4fe4778da8028566f2486fe6b8c1d042eb7', '2026-03-11 01:00:08', '2026-03-29 15:28:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(226, 1, '102室电表', 'DEV001', NULL, 1, NULL, 238.07, 46.30, 163.13, 8.97, 8.16, 11.51, '50d28f810dab571ee8ba30e1586c518131dff91ae797e035ce8b85205e79b6ea', '2026-03-11 02:00:08', '2026-03-29 15:28:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(227, 1, '102室电表', 'DEV001', NULL, 1, NULL, 238.23, 46.30, 163.13, 8.97, 8.16, 11.67, '55dcd6865e3e97072000669c5948ca3e0f10c1b98fe0091329d71b09594dd0c8', '2026-03-11 03:00:08', '2026-03-29 15:28:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(228, 1, '102室电表', 'DEV001', NULL, 1, NULL, 238.39, 46.30, 163.13, 8.97, 8.16, 11.83, 'fd312a2e46fb3482bb05db5ee457a9e8d620e2dc7a29478cd7b821ed32da39e8', '2026-03-11 04:00:08', '2026-03-29 15:28:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(229, 1, '102室电表', 'DEV001', NULL, 1, NULL, 238.56, 46.30, 163.13, 8.97, 8.16, 12.00, '0e27ae74178e69513e895c5a06be3990cbfebea1e29fe94c438a96b2e07f7632', '2026-03-11 05:00:08', '2026-03-29 15:28:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(230, 1, '102室电表', 'DEV001', NULL, 1, NULL, 238.70, 46.30, 163.13, 8.97, 8.30, 12.00, 'feec65e12b7e7784f4e3977a8bfba7361dcf3920823eb1bfd2442b5549c263af', '2026-03-11 06:00:08', '2026-03-29 15:28:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(231, 1, '102室电表', 'DEV001', NULL, 1, NULL, 238.85, 46.30, 163.13, 8.97, 8.45, 12.00, '46626332d2514ac975684b97b9c899ed036273f6362a1a9fa9d86d861f91b8bf', '2026-03-11 07:00:08', '2026-03-29 15:28:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(232, 1, '102室电表', 'DEV001', NULL, 1, NULL, 239.00, 46.30, 163.28, 8.97, 8.45, 12.00, '9afe7bc10f52c34c2ace5b1880c6e942e88ac2020f9da9ca7636944982c3c4da', '2026-03-11 08:00:08', '2026-03-29 15:28:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(233, 1, '102室电表', 'DEV001', NULL, 1, NULL, 242.36, 46.30, 166.64, 8.97, 8.45, 12.00, 'a5e1817ffd31a558a00c951223f63ee59c09def124152074a8161dfe75ad5ceb', '2026-03-11 09:00:08', '2026-03-29 15:28:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(234, 1, '102室电表', 'DEV001', NULL, 1, NULL, 245.14, 46.30, 169.42, 8.97, 8.45, 12.00, '6978efed4072d0550706f1899c85af04789d3708ef752291eeb5142559bc9140', '2026-03-11 10:00:08', '2026-03-29 15:28:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(235, 1, '102室电表', 'DEV001', NULL, 1, NULL, 248.12, 49.28, 169.42, 8.97, 8.45, 12.00, 'b299f4e632768d04462eaf063a2d8c6eedbd10ca19ecd1411d363598aec59c87', '2026-03-11 11:00:08', '2026-03-29 15:28:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(236, 1, '102室电表', 'DEV001', NULL, 1, NULL, 251.00, 52.16, 169.42, 8.97, 8.45, 12.00, '29ddce590a4a59d8d65275017c3f4ecd1fadf0c14a0ab4a810fa67f9562fcb92', '2026-03-11 12:00:08', '2026-03-29 15:28:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(237, 1, '102室电表', 'DEV001', NULL, 1, NULL, 254.07, 52.16, 172.49, 8.97, 8.45, 12.00, 'a071779ecc90a01b56c196b59e55493d428c37d4acf72ab56af78ed46e0caa21', '2026-03-11 13:00:08', '2026-03-29 15:28:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(238, 1, '102室电表', 'DEV001', NULL, 1, NULL, 257.56, 52.16, 175.98, 8.97, 8.45, 12.00, 'e8420a6661a06a923dcb0fe99ca4142d9a1ddd58b647246026d8aa4734f46b26', '2026-03-11 14:00:08', '2026-03-29 15:28:53', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(239, 1, '102室电表', 'DEV001', NULL, 1, NULL, 261.24, 52.16, 179.66, 8.97, 8.45, 12.00, 'c0b919ad9721977c767afde400d734479b7b396745cc2714c5b4134f3a2c8216', '2026-03-11 15:00:08', '2026-03-29 15:28:53', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(240, 1, '102室电表', 'DEV001', NULL, 1, NULL, 264.86, 52.16, 183.28, 8.97, 8.45, 12.00, 'd5ff0c87ead1dd9152352972f60e63290a06402370a61964ac7fbe28a4d1e240', '2026-03-11 16:00:08', '2026-03-29 15:28:53', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(241, 1, '102室电表', 'DEV001', NULL, 1, NULL, 267.68, 52.16, 186.10, 8.97, 8.45, 12.00, 'd2b660a0be2b3127aa90714f88501e53d7eed66eb3172703050bc435866fb8a7', '2026-03-11 17:00:08', '2026-03-29 15:28:53', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(242, 1, '102室电表', 'DEV001', NULL, 1, NULL, 267.83, 52.16, 186.10, 9.12, 8.45, 12.00, '181533689aa206ee519b1b276b6312e85a35eb3a9cc932fe20d83060e3d7de56', '2026-03-11 18:00:08', '2026-03-29 15:28:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(243, 1, '102室电表', 'DEV001', NULL, 1, NULL, 267.99, 52.16, 186.10, 9.28, 8.45, 12.00, '1cf3b01699ddcd9344dca9e73538dd7ae699ff6411e8948f924b3d2a1d690ddb', '2026-03-11 19:00:08', '2026-03-29 15:28:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(244, 1, '102室电表', 'DEV001', NULL, 1, NULL, 268.15, 52.16, 186.10, 9.44, 8.45, 12.00, '59f17127a896ee74fd7bd52f3b0299c7967b5d11a4b0c9b488566c6bbcefbea0', '2026-03-11 20:00:08', '2026-03-29 15:28:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(245, 1, '102室电表', 'DEV001', NULL, 1, NULL, 268.32, 52.16, 186.10, 9.61, 8.45, 12.00, '3559ce30d985657f96e8817fc9e5c225a24a65e464260b72e3e23846bcd77298', '2026-03-11 21:00:08', '2026-03-29 15:28:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(246, 1, '102室电表', 'DEV001', NULL, 1, NULL, 268.46, 52.16, 186.10, 9.61, 8.59, 12.00, '1938ad7cf02ae3220d2480c25af7379f949170b248ed9673df05498045cc3711', '2026-03-11 22:00:08', '2026-03-29 15:28:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(247, 1, '102室电表', 'DEV001', NULL, 1, NULL, 268.61, 52.16, 186.10, 9.61, 8.74, 12.00, 'e8dd811b2de2ff0160c93ad1ec6be6c78987203d20ddd4d126ebdc3c54c068bc', '2026-03-11 23:00:08', '2026-03-29 15:28:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(248, 1, '102室电表', 'DEV001', NULL, 1, NULL, 268.76, 52.16, 186.10, 9.61, 8.74, 12.15, '9db44e1719972bb3f12ed0b17a94b977cc847baef792954d1d29561486369e8a', '2026-03-12 00:00:08', '2026-03-29 15:28:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(249, 1, '102室电表', 'DEV001', NULL, 1, NULL, 268.92, 52.16, 186.10, 9.61, 8.74, 12.31, '4c796bb2036e8680eafafebee2c73eabfe9b21d96e72ee627ca0430931dfd28f', '2026-03-12 01:00:08', '2026-03-29 15:28:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(250, 1, '102室电表', 'DEV001', NULL, 1, NULL, 269.06, 52.16, 186.10, 9.61, 8.74, 12.45, 'b2c19c045d489b275e87025530e57d10c7f5c356f0fce32ef7265390c0d38b35', '2026-03-12 02:00:08', '2026-03-29 15:28:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(251, 1, '102室电表', 'DEV001', NULL, 1, NULL, 269.20, 52.16, 186.10, 9.61, 8.74, 12.59, '035ec906f5f77c4c4892118ae8ca7b3ab8e9ecf731e02ce218f5852cc19e9b9a', '2026-03-12 03:00:08', '2026-03-29 15:28:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(252, 1, '102室电表', 'DEV001', NULL, 1, NULL, 269.34, 52.16, 186.10, 9.61, 8.74, 12.73, 'dc8dc2691cc8f91c4e3c478f22630f8b8a4d6e9aa14dc8bfb5fcce7c2ab5231c', '2026-03-12 04:00:08', '2026-03-29 15:28:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(253, 1, '102室电表', 'DEV001', NULL, 1, NULL, 269.49, 52.16, 186.10, 9.61, 8.74, 12.88, '4403ef7006df58342125ffe9dc8d56281f41e6446f2f0194c0fbcf359ae62273', '2026-03-12 05:00:08', '2026-03-29 15:28:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(254, 1, '102室电表', 'DEV001', NULL, 1, NULL, 269.66, 52.16, 186.10, 9.61, 8.91, 12.88, '9bbf0972a8253c62e4f41cf2e2330a6607826af975da4757315106b44fb7d227', '2026-03-12 06:00:08', '2026-03-29 15:28:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(255, 1, '102室电表', 'DEV001', NULL, 1, NULL, 269.79, 52.16, 186.10, 9.61, 9.04, 12.88, '0c9cef4e56ee45acbd54d3f02737e729abac58881be16f42a45ab36908688b84', '2026-03-12 07:00:08', '2026-03-29 15:28:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(256, 1, '102室电表', 'DEV001', NULL, 1, NULL, 269.92, 52.16, 186.23, 9.61, 9.04, 12.88, '0d4940bf45c3a45b7545ca9b9da3f001b7e3425bde352d2b23cd4fecda38ad78', '2026-03-12 08:00:08', '2026-03-29 15:28:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(257, 1, '102室电表', 'DEV001', NULL, 1, NULL, 272.90, 52.16, 189.21, 9.61, 9.04, 12.88, 'c66b677c95e3009e763d2e77460f38a338415784f901fce78cb396f0995d3aac', '2026-03-12 09:00:08', '2026-03-29 15:28:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(258, 1, '102室电表', 'DEV001', NULL, 1, NULL, 276.29, 52.16, 192.60, 9.61, 9.04, 12.88, 'ea2f38e689027ed99d0757edc9846f81cfc0824b8ac614183a99a2188a935e1f', '2026-03-12 10:00:08', '2026-03-29 15:28:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(259, 1, '102室电表', 'DEV001', NULL, 1, NULL, 279.87, 55.74, 192.60, 9.61, 9.04, 12.88, 'ac3f8d655acebcec347127b5e19194f0520d469439ce00a017a76a41f8e0c626', '2026-03-12 11:00:08', '2026-03-29 15:28:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(260, 1, '102室电表', 'DEV001', NULL, 1, NULL, 283.36, 59.23, 192.60, 9.61, 9.04, 12.88, '5422bc267e38badbf2ef69b50b9834e3a5d7069c1658f8b1b9930399dae94563', '2026-03-12 12:00:08', '2026-03-29 15:28:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(261, 1, '102室电表', 'DEV001', NULL, 1, NULL, 287.04, 59.23, 196.28, 9.61, 9.04, 12.88, 'aeccb8bc481eb7631130701a04cefe4bba0511555751a64038575b362d0c1d46', '2026-03-12 13:00:08', '2026-03-29 15:28:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(262, 1, '102室电表', 'DEV001', NULL, 1, NULL, 290.14, 59.23, 199.38, 9.61, 9.04, 12.88, 'b7f9c603592c679912b7fbc3009a9c24d4025174430c7ea919ae61d03cc2a423', '2026-03-12 14:00:08', '2026-03-29 15:28:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(263, 1, '102室电表', 'DEV001', NULL, 1, NULL, 293.44, 59.23, 202.68, 9.61, 9.04, 12.88, '06969f489f52b1f395fd0c55f25339058974093e796e35da9b99e9e7f4914810', '2026-03-12 15:00:08', '2026-03-29 15:28:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(264, 1, '102室电表', 'DEV001', NULL, 1, NULL, 296.67, 59.23, 205.91, 9.61, 9.04, 12.88, '42bd9002ac489df58152c6e80f1c2dec711c1d18393b0275a65dddfea728dd63', '2026-03-12 16:00:08', '2026-03-29 15:28:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(265, 1, '102室电表', 'DEV001', NULL, 1, NULL, 300.09, 59.23, 209.33, 9.61, 9.04, 12.88, '96a9632d35ed4ea940e4c97a4c4622e98a539de24d24edbc7a6480371e3b99ab', '2026-03-12 17:00:08', '2026-03-29 15:28:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(266, 1, '102室电表', 'DEV001', NULL, 1, NULL, 300.23, 59.23, 209.33, 9.75, 9.04, 12.88, 'ec27e24c535a1b543269a00d2e9578d8c28c416888c3f3735bdc868a0b282981', '2026-03-12 18:00:08', '2026-03-29 15:28:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(267, 1, '102室电表', 'DEV001', NULL, 1, NULL, 300.38, 59.23, 209.33, 9.90, 9.04, 12.88, 'ce773b8884c58001c6e520d238c7d9a7b751de4d46212982312814d62285fc2b', '2026-03-12 19:00:08', '2026-03-29 15:28:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(268, 1, '102室电表', 'DEV001', NULL, 1, NULL, 300.52, 59.23, 209.33, 10.04, 9.04, 12.88, '9349ec8fe1b879f1509816e08a6b3e652c012e7e358961de19fddc58b4a044ff', '2026-03-12 20:00:08', '2026-03-29 15:28:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(269, 1, '102室电表', 'DEV001', NULL, 1, NULL, 300.67, 59.23, 209.33, 10.19, 9.04, 12.88, '88912012d1f58e04f545b0a7d91c516004f726ac6225ea8162231e724e44fea7', '2026-03-12 21:00:08', '2026-03-29 15:28:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(270, 1, '102室电表', 'DEV001', NULL, 1, NULL, 300.84, 59.23, 209.33, 10.19, 9.21, 12.88, 'e11c739c81f31bdf54dae8444053ba1f2f51c2324503a8d74a1394aa21e94841', '2026-03-12 22:00:08', '2026-03-29 15:28:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(271, 1, '102室电表', 'DEV001', NULL, 1, NULL, 300.97, 59.23, 209.33, 10.19, 9.34, 12.88, '80eb0cb85956b9f4d51c498c3df3972c2c730fc5708b8e9517399a8d57497377', '2026-03-12 23:00:08', '2026-03-29 15:29:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(272, 1, '102室电表', 'DEV001', NULL, 1, NULL, 301.11, 59.23, 209.33, 10.19, 9.34, 13.02, '82aec26a474353552276dba0020b8ad2b3ac2b2ed98ea72b1cdb7e8e7e69de9a', '2026-03-13 00:00:08', '2026-03-29 15:29:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(273, 1, '102室电表', 'DEV001', NULL, 1, NULL, 301.26, 59.23, 209.33, 10.19, 9.34, 13.17, '03b106c6df742d710c8f34564c090c650e312e1b6dbf5c0cb48ec6bc79545fcd', '2026-03-13 01:00:08', '2026-03-29 15:29:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(274, 1, '102室电表', 'DEV001', NULL, 1, NULL, 301.43, 59.23, 209.33, 10.19, 9.34, 13.34, '68fcef7cc348cb2f08c5ad3ec51822150f693733cce46909440949789ef309dd', '2026-03-13 02:00:08', '2026-03-29 15:29:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(275, 1, '102室电表', 'DEV001', NULL, 1, NULL, 301.56, 59.23, 209.33, 10.19, 9.34, 13.47, '4fc9dce61de08c4f844ae4182c89ac4874eaefc632c1faa6da4df1be4bf9df1e', '2026-03-13 03:00:08', '2026-03-29 15:29:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(276, 1, '102室电表', 'DEV001', NULL, 1, NULL, 301.73, 59.23, 209.33, 10.19, 9.34, 13.64, '6e71f8f69a2a2cb6ce22dc0406a30f69d6cd28ded1827e193469255f6afed342', '2026-03-13 04:00:08', '2026-03-29 15:29:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(277, 1, '102室电表', 'DEV001', NULL, 1, NULL, 301.86, 59.23, 209.33, 10.19, 9.34, 13.77, 'd9014c939768e721ff078f75ea74cc3c39419cdfce534d3f97bd1fed2eac836b', '2026-03-13 05:00:08', '2026-03-29 15:29:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(278, 1, '102室电表', 'DEV001', NULL, 1, NULL, 302.01, 59.23, 209.33, 10.19, 9.49, 13.77, 'c1c7deb1bc2d2b2a1608e0e38d50bc2f01ca6f7d013166c3c52e055876b6ee0d', '2026-03-13 06:00:08', '2026-03-29 15:29:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(279, 1, '102室电表', 'DEV001', NULL, 1, NULL, 302.17, 59.23, 209.33, 10.19, 9.65, 13.77, '99100bca73d6d48dc0c250c02cff35ce40ee3c37b1c3115598c2a44fbdc2e09c', '2026-03-13 07:00:08', '2026-03-29 15:29:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(280, 1, '102室电表', 'DEV001', NULL, 1, NULL, 302.33, 59.23, 209.49, 10.19, 9.65, 13.77, 'b70923b547302db13d1b67f446f040c68b1c25424f65b0d3065adfd61a039928', '2026-03-13 08:00:08', '2026-03-29 15:29:02', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(281, 1, '102室电表', 'DEV001', NULL, 1, NULL, 305.95, 59.23, 213.11, 10.19, 9.65, 13.77, '1413b52200011d75665b31594c5157bfe66617007059b8e34b0200a75684551c', '2026-03-13 09:00:08', '2026-03-29 15:29:02', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(282, 1, '102室电表', 'DEV001', NULL, 1, NULL, 308.99, 59.23, 216.15, 10.19, 9.65, 13.77, 'ef269c6c1c6e5854cf1a56094de30c1f5f947d261a6cfd521ffec7860c476088', '2026-03-13 10:00:08', '2026-03-29 15:29:02', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(283, 1, '102室电表', 'DEV001', NULL, 1, NULL, 315.36, 65.60, 216.15, 10.19, 9.65, 13.77, 'fd525f15920056c1f7ea410655fbc7d037a3faab022f335d9838a336ea79fe78', '2026-03-13 12:00:08', '2026-03-29 15:29:02', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(284, 1, '102室电表', 'DEV001', NULL, 1, NULL, 318.69, 65.60, 219.48, 10.19, 9.65, 13.77, 'cf46db0099a352e3ad01b55fd3b395dab2fe5e4266c39b17b98b1aae16e4084c', '2026-03-13 13:00:08', '2026-03-29 15:29:03', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(285, 1, '102室电表', 'DEV001', NULL, 1, NULL, 324.38, 65.60, 225.17, 10.19, 9.65, 13.77, '1cfaf6ef94b7005dea69c80aa997e0c26c4668b8870c52fe617e5d0e9f2ea78a', '2026-03-13 15:00:08', '2026-03-29 15:29:03', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(286, 1, '102室电表', 'DEV001', NULL, 1, NULL, 327.26, 65.60, 228.05, 10.19, 9.65, 13.77, '55c20b3f3a9eb224e1784b6af22d1d3197abea56f49ee3892158599f4105a18e', '2026-03-13 16:00:08', '2026-03-29 15:29:03', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(287, 1, '102室电表', 'DEV001', NULL, 1, NULL, 330.50, 65.60, 231.12, 10.36, 9.65, 13.77, '144c7cfc27cd404aaef065575ea6245be3b9e3f06828866303ade8357797e2fc', '2026-03-13 18:00:08', '2026-03-29 15:29:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(288, 1, '102室电表', 'DEV001', NULL, 1, NULL, 330.63, 65.60, 231.12, 10.49, 9.65, 13.77, '629c3191d9aae1cb8f3ddb77ab2ff601708bb883391b840dd2ab3a432a1a36f1', '2026-03-13 19:00:08', '2026-03-29 15:29:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(289, 1, '102室电表', 'DEV001', NULL, 1, NULL, 330.93, 65.60, 231.12, 10.79, 9.65, 13.77, '6d01d13711e4edc5aa01e620b063ec6c3e954d368e29021103d5a49edecfcf60', '2026-03-13 21:00:08', '2026-03-29 15:29:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(290, 1, '102室电表', 'DEV001', NULL, 1, NULL, 331.08, 65.60, 231.12, 10.79, 9.80, 13.77, '80c58d9df549b66d8ca784b41d3dd718f920119c1649b93e59b5ce786b83e4db', '2026-03-13 22:00:08', '2026-03-29 15:29:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(291, 1, '102室电表', 'DEV001', NULL, 1, NULL, 331.68, 65.60, 231.12, 10.79, 9.96, 14.21, '3873de2749d7c325e0a2e1bed1dc487efeaaac5f52e93084dd7db0fc4d141cd6', '2026-03-14 00:00:08', '2026-03-29 15:29:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(292, 1, '102室电表', 'DEV001', NULL, 1, NULL, 332.02, 65.60, 231.12, 10.79, 9.96, 14.55, '73b27ed078c0652f039e4bf5906f405516a5737b901899253add842ee249c944', '2026-03-14 01:00:08', '2026-03-29 15:29:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(293, 1, '102室电表', 'DEV001', NULL, 1, NULL, 332.83, 65.60, 231.12, 10.79, 9.96, 15.36, '2be7472e30eb93e2af353b18f96bdcd626cfa3fefffe56b61c610ebb39ed8c9f', '2026-03-14 03:00:08', '2026-03-29 15:29:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(294, 1, '102室电表', 'DEV001', NULL, 1, NULL, 333.23, 65.60, 231.12, 10.79, 9.96, 15.76, 'c316abe96448ec03353420c2adae86d6e6918e13c6277a5ef30d9566548e257b', '2026-03-14 04:00:08', '2026-03-29 15:29:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(295, 1, '102室电表', 'DEV001', NULL, 1, NULL, 334.02, 65.60, 231.12, 10.79, 10.32, 16.19, '68f15f0bfc3018557215ef62cb84ce92224e8f83c41b0584ea250f1e90c19892', '2026-03-14 06:00:08', '2026-03-29 15:29:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(296, 1, '102室电表', 'DEV001', NULL, 1, NULL, 334.41, 65.60, 231.12, 10.79, 10.71, 16.19, 'eaccca9fff6b29d1171b67d2c1c2b8706be8a79a90a7879fe8659da42a80d0a1', '2026-03-14 07:00:08', '2026-03-29 15:29:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(297, 1, '102室电表', 'DEV001', NULL, 1, NULL, 335.19, 65.60, 231.90, 10.79, 10.71, 16.19, '892b76db092e720031779c107292f407975f75fe4e79774a5467c4c78f480c8f', '2026-03-14 09:00:08', '2026-03-29 15:29:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(298, 1, '102室电表', 'DEV001', NULL, 1, NULL, 335.65, 65.60, 232.36, 10.79, 10.71, 16.19, 'b7751f294fdefc2cce0646a39bca525582d1af86866069818309cb5036564f78', '2026-03-14 10:00:08', '2026-03-29 15:29:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(299, 1, '102室电表', 'DEV001', NULL, 1, NULL, 312.22, 62.46, 216.15, 10.19, 9.65, 13.77, 'bc2f644345e358e98a53c50dc43d75292868df46260a37a0da44baff33c7f837', '2026-03-13 11:00:08', '2026-03-29 15:29:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(300, 1, '102室电表', 'DEV001', NULL, 1, NULL, 321.44, 65.60, 222.23, 10.19, 9.65, 13.77, '8f95ac024600d66579b567ccc56d8c8518b621d33b54a9c59fa817e116e31b93', '2026-03-13 14:00:08', '2026-03-29 15:29:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(301, 1, '102室电表', 'DEV001', NULL, 1, NULL, 330.33, 65.60, 231.12, 10.19, 9.65, 13.77, 'f55fdf2f7e48a67a19c23bbf78b7e92079f8ccf0f662055289789aa7148c28b1', '2026-03-13 17:00:08', '2026-03-29 15:29:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(302, 1, '102室电表', 'DEV001', NULL, 1, NULL, 330.80, 65.60, 231.12, 10.66, 9.65, 13.77, '71f6343a3d4158651bea8117c075e27e8d03a774804a39efde721a4c4cd0c7bd', '2026-03-13 20:00:08', '2026-03-29 15:29:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(303, 1, '102室电表', 'DEV001', NULL, 1, NULL, 331.24, 65.60, 231.12, 10.79, 9.96, 13.77, '1ef137c10110b43bb6145083e9e74c39cd46f2a55095871de415b9f945087f4c', '2026-03-13 23:00:08', '2026-03-29 15:29:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(304, 1, '102室电表', 'DEV001', NULL, 1, NULL, 336.35, 66.30, 232.36, 10.79, 10.71, 16.19, 'b302ec496b7651d6ee411299173c997d4a283c69a80270f916a023d0744d5711', '2026-03-14 12:00:08', '2026-03-29 15:29:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(305, 1, '102室电表', 'DEV001', NULL, 1, NULL, 336.72, 66.30, 232.73, 10.79, 10.71, 16.19, 'bfc90ab6270337e2b83fd327e5caa2c41256f02fbd32116121b8a58496eedccb', '2026-03-14 13:00:08', '2026-03-29 15:29:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(306, 1, '102室电表', 'DEV001', NULL, 1, NULL, 337.58, 66.30, 233.59, 10.79, 10.71, 16.19, '216c8d922a46092a6607ee276980342d84d667d5786d4d6ce32904c81c8a6272', '2026-03-14 15:00:08', '2026-03-29 15:29:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(307, 1, '102室电表', 'DEV001', NULL, 1, NULL, 338.02, 66.30, 234.03, 10.79, 10.71, 16.19, '11fe670a6b6a335f5c0e2f1963c70834468f1c576d884fb87da7b1b011f76044', '2026-03-14 16:00:08', '2026-03-29 15:29:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(308, 1, '102室电表', 'DEV001', NULL, 1, NULL, 332.41, 65.60, 231.12, 10.79, 9.96, 14.94, 'c9573e9f68c66b47630abac7ffef4e5f5ae9930ad72a6240527305e835d98081', '2026-03-14 02:00:08', '2026-03-29 15:29:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(309, 1, '102室电表', 'DEV001', NULL, 1, NULL, 333.66, 65.60, 231.12, 10.79, 9.96, 16.19, 'a729327bb75b5f0815b3b0475431d1df7cfed5d751ebc94d172c9dcf07dbb235', '2026-03-14 05:00:08', '2026-03-29 15:29:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(310, 1, '102室电表', 'DEV001', NULL, 1, NULL, 334.79, 65.60, 231.50, 10.79, 10.71, 16.19, 'c122f8b57afefc65f01e22fa234ade9dbfd6f0031bec538075906bf619811e56', '2026-03-14 08:00:08', '2026-03-29 15:29:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(311, 1, '102室电表', 'DEV001', NULL, 1, NULL, 336.01, 65.96, 232.36, 10.79, 10.71, 16.19, '0e6de04536df25044e7c58abfc7b2f1cee64ae62960f123739222e07c153ceb1', '2026-03-14 11:00:08', '2026-03-29 15:29:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(312, 1, '102室电表', 'DEV001', NULL, 1, NULL, 337.14, 66.30, 233.15, 10.79, 10.71, 16.19, '979b358ec9c82c3add73a316da55bbb8de063c560e78758dde9a4b5aeae145a5', '2026-03-14 14:00:08', '2026-03-29 15:29:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(313, 1, '102室电表', 'DEV001', NULL, 1, NULL, 338.48, 66.30, 234.49, 10.79, 10.71, 16.19, 'fff24e9bd67cf8e18f3b01ad674f77681eb42f8ea1c88aebbc74ee6e77be3f81', '2026-03-14 17:00:08', '2026-03-29 15:29:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(314, 1, '102室电表', 'DEV001', NULL, 1, NULL, 338.88, 66.30, 234.49, 11.19, 10.71, 16.19, 'ea5e0663a952cc5182bbd051eaa82eb051bf93a340ed644189754587a463e40e', '2026-03-14 18:00:08', '2026-03-29 15:29:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(315, 1, '102室电表', 'DEV001', NULL, 1, NULL, 339.30, 66.30, 234.49, 11.61, 10.71, 16.19, '784bf2f3677f76f8ba9d4ce5a490278e123ab8408deb03f24ce62df0d06050fc', '2026-03-14 19:00:08', '2026-03-29 15:29:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(316, 1, '102室电表', 'DEV001', NULL, 1, NULL, 339.71, 66.30, 234.49, 12.02, 10.71, 16.19, '1c8f0c8ff24737a5894a2d07146762d7b7681aaba5572b14dc411e09d3282e98', '2026-03-14 20:00:08', '2026-03-29 15:29:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(317, 1, '102室电表', 'DEV001', NULL, 1, NULL, 340.14, 66.30, 234.49, 12.45, 10.71, 16.19, '0e8373307481d8c6604e0be04c06192997d5be5f88bebc2c4ef340b551b6d3a6', '2026-03-14 21:00:08', '2026-03-29 15:29:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(318, 1, '102室电表', 'DEV001', NULL, 1, NULL, 340.50, 66.30, 234.49, 12.45, 11.07, 16.19, 'a34e0f64462ecda535630b0df75b4617336fb83cf65d8b3bf52c1aa512e22ccf', '2026-03-14 22:00:08', '2026-03-29 15:29:09', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(319, 1, '102室电表', 'DEV001', NULL, 1, NULL, 340.88, 66.30, 234.49, 12.45, 11.45, 16.19, '6cd038fd176f128e6cdea7675c3844d373a7e02966f78376ec13899b0d9c5c98', '2026-03-14 23:00:08', '2026-03-29 15:29:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(320, 1, '102室电表', 'DEV001', NULL, 1, NULL, 341.28, 66.30, 234.49, 12.45, 11.45, 16.59, '9520e2f187ccce805f33ba8fef5cc0c8d386ed757b2583f9d436ce3771d4abcd', '2026-03-15 00:00:08', '2026-03-29 15:29:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(321, 1, '102室电表', 'DEV001', NULL, 1, NULL, 341.70, 66.30, 234.49, 12.45, 11.45, 17.01, '98457294542715e97e82feadad3c74831ff4cb6b313abb245fe6e409dae6ca79', '2026-03-15 01:00:08', '2026-03-29 15:29:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(322, 1, '102室电表', 'DEV001', NULL, 1, NULL, 342.05, 66.30, 234.49, 12.45, 11.45, 17.36, '26982c7d8384fef5f51a5942de8eaf4f669a888d305f0d600c0c95068ac2b3aa', '2026-03-15 02:00:08', '2026-03-29 15:29:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(323, 1, '102室电表', 'DEV001', NULL, 1, NULL, 342.42, 66.30, 234.49, 12.45, 11.45, 17.73, 'e6407870c6a8544c3bb95392c913dc2d04daa899705be313499d8e332275e993', '2026-03-15 03:00:08', '2026-03-29 15:29:10', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(324, 1, '102室电表', 'DEV001', NULL, 1, NULL, 342.78, 66.30, 234.49, 12.45, 11.45, 18.09, '9640715d77a270a88fe9a1fe5bd90063b06c721c5845703f6a4e5d84498be87d', '2026-03-15 04:00:08', '2026-03-29 15:29:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(325, 1, '102室电表', 'DEV001', NULL, 1, NULL, 343.16, 66.30, 234.49, 12.45, 11.45, 18.47, 'a3c5e4f5055b5ebc7b87a15935ebda624a4722f4399dcd4f422fff71e18a8d7e', '2026-03-15 05:00:08', '2026-03-29 15:29:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(326, 1, '102室电表', 'DEV001', NULL, 1, NULL, 343.60, 66.30, 234.49, 12.45, 11.89, 18.47, '494298e4af57bbf71971ae52b726ebe30d95d4ccf79439b9d640a15af9d77fc8', '2026-03-15 06:00:08', '2026-03-29 15:29:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(327, 1, '102室电表', 'DEV001', NULL, 1, NULL, 343.94, 66.30, 234.49, 12.45, 12.23, 18.47, 'f81e9d97b3a8da61ed8f58bc01e54d4eb5ab6c97520bd827f2f6f6bc7cbc9f66', '2026-03-15 07:00:08', '2026-03-29 15:29:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(328, 1, '102室电表', 'DEV001', NULL, 1, NULL, 344.40, 66.30, 234.95, 12.45, 12.23, 18.47, '7a799e982d21dba24bf9b86f2a961682ba81a2fb63a87ab8357348d9f94da58c', '2026-03-15 08:00:08', '2026-03-29 15:29:11', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(329, 1, '102室电表', 'DEV001', NULL, 1, NULL, 344.76, 66.30, 235.31, 12.45, 12.23, 18.47, 'e8b1728519660be72bfed659dae836aa94ac57b18c3deb1178719c909db40dc1', '2026-03-15 09:00:08', '2026-03-29 15:29:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(330, 1, '102室电表', 'DEV001', NULL, 1, NULL, 345.17, 66.30, 235.72, 12.45, 12.23, 18.47, '59fe4785fc83dead972d066ca04872b2eb20b78d10ce46d702ca94e285b939a1', '2026-03-15 10:00:08', '2026-03-29 15:29:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(331, 1, '102室电表', 'DEV001', NULL, 1, NULL, 345.61, 66.74, 235.72, 12.45, 12.23, 18.47, '4d1e1695cd80eee8bd574fdbbea6b9475103da32d7288245862198ef4a8e433f', '2026-03-15 11:00:08', '2026-03-29 15:29:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(332, 1, '102室电表', 'DEV001', NULL, 1, NULL, 346.03, 67.16, 235.72, 12.45, 12.23, 18.47, '629113596d967e391ab33000a9420b7a90da2d8bb001812bcb5e63b02e03646f', '2026-03-15 12:00:08', '2026-03-29 15:29:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(333, 1, '102室电表', 'DEV001', NULL, 1, NULL, 346.48, 67.16, 236.17, 12.45, 12.23, 18.47, '789c8c064e02aadedde49585010fd1af3eb39f19e2db7d247a5890b014fb5a06', '2026-03-15 13:00:08', '2026-03-29 15:29:12', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(334, 1, '102室电表', 'DEV001', NULL, 1, NULL, 346.86, 67.16, 236.55, 12.45, 12.23, 18.47, '2f7f61e26b9594994a0428544823617ddf65e5305479e6c5b77dd961c00b4faa', '2026-03-15 14:00:08', '2026-03-29 15:29:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(335, 1, '102室电表', 'DEV001', NULL, 1, NULL, 347.26, 67.16, 236.95, 12.45, 12.23, 18.47, '3539d17323950306dc3a8be248be6133a11fa770812fec5f1f6444214798d2b3', '2026-03-15 15:00:08', '2026-03-29 15:29:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(336, 1, '102室电表', 'DEV001', NULL, 1, NULL, 347.65, 67.16, 237.34, 12.45, 12.23, 18.47, '5251023432552f62bd9a68bf728469d91fcba089a2902826d7e13d571a7a837d', '2026-03-15 16:00:08', '2026-03-29 15:29:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(337, 1, '102室电表', 'DEV001', NULL, 1, NULL, 348.07, 67.16, 237.76, 12.45, 12.23, 18.47, '303017793c6f6cb52fa0383dfce53f3e66fa7905692e174188b8b16f1ef56567', '2026-03-15 17:00:08', '2026-03-29 15:29:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(338, 1, '102室电表', 'DEV001', NULL, 1, NULL, 348.42, 67.16, 237.76, 12.80, 12.23, 18.47, '5e26a1375b3e9b4eb67ea5db543eb127c286d3bc06c653f50fdce3a77686244a', '2026-03-15 18:00:08', '2026-03-29 15:29:13', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(339, 1, '102室电表', 'DEV001', NULL, 1, NULL, 348.80, 67.16, 237.76, 13.18, 12.23, 18.47, '9a45a7a081797839f7ba746f751c54baecf91757547dbd76c47c509bd49fd68d', '2026-03-15 19:00:08', '2026-03-29 15:29:14', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(340, 1, '102室电表', 'DEV001', NULL, 1, NULL, 349.16, 67.16, 237.76, 13.54, 12.23, 18.47, '81515309115773de580024cf8bd24ed21d73c9e3cdb22d41ad5960a6829fb37c', '2026-03-15 20:00:08', '2026-03-29 15:29:14', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(341, 1, '102室电表', 'DEV001', NULL, 1, NULL, 349.55, 67.16, 237.76, 13.93, 12.23, 18.47, '5c87c05b08cd2a75f4d4ec5524d3707f91f185ace4c57bb3d5782e79568bd1fd', '2026-03-15 21:00:08', '2026-03-29 15:29:14', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(342, 1, '102室电表', 'DEV001', NULL, 1, NULL, 349.99, 67.16, 237.76, 13.93, 12.67, 18.47, '6122597613fdd404f0d362219a860ffb8bb904a038f9f25099299b6da875fd17', '2026-03-15 22:00:08', '2026-03-29 15:29:14', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(343, 1, '102室电表', 'DEV001', NULL, 1, NULL, 350.33, 67.16, 237.76, 13.93, 13.01, 18.47, 'd8d560b664ccbc7e2c06e40f72492bdf441cb608c5565d25003b97145cf7efd0', '2026-03-15 23:00:08', '2026-03-29 15:29:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(344, 1, '102室电表', 'DEV001', NULL, 1, NULL, 350.46, 67.16, 237.76, 13.93, 13.01, 18.60, 'd4a4b8f2a49533063ebadf82b09d72c98699946f9dfa2ec790985cea407f1a44', '2026-03-16 00:00:08', '2026-03-29 15:29:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(345, 1, '102室电表', 'DEV001', NULL, 1, NULL, 350.60, 67.16, 237.76, 13.93, 13.01, 18.74, '59c5ebf682f59f1363a93806f767671a35030788d990fb565bc594aefb0ea09a', '2026-03-16 01:00:08', '2026-03-29 15:29:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(346, 1, '102室电表', 'DEV001', NULL, 1, NULL, 350.76, 67.16, 237.76, 13.93, 13.01, 18.90, '3abb6bba1111ece6b2eb8f33a23097c11642d57c4991ab98de8a4048cc2f9abd', '2026-03-16 02:00:08', '2026-03-29 15:29:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(347, 1, '102室电表', 'DEV001', NULL, 1, NULL, 350.93, 67.16, 237.76, 13.93, 13.01, 19.07, '06dd7f6751964fa461a64d1c500be4fc864b59ee3b2d8b0ea917089680187087', '2026-03-16 03:00:08', '2026-03-29 15:29:15', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(348, 1, '102室电表', 'DEV001', NULL, 1, NULL, 351.10, 67.16, 237.76, 13.93, 13.01, 19.24, '6efa84fc0eaccaaa2a73f6007e8366a943521d5723681fa25f4db14156d8de28', '2026-03-16 04:00:08', '2026-03-29 15:29:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(349, 1, '102室电表', 'DEV001', NULL, 1, NULL, 351.23, 67.16, 237.76, 13.93, 13.01, 19.37, '1c4fad47b0ccaf4e8b594952ba594f562b688f17629a642c759b153caa69773d', '2026-03-16 05:00:08', '2026-03-29 15:29:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(350, 1, '102室电表', 'DEV001', NULL, 1, NULL, 351.38, 67.16, 237.76, 13.93, 13.16, 19.37, '2307ef0ce2efc708f91f9d3a1da88945bf3194edfaf1481c7f65f48291057b65', '2026-03-16 06:00:08', '2026-03-29 15:29:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(351, 1, '102室电表', 'DEV001', NULL, 1, NULL, 351.54, 67.16, 237.76, 13.93, 13.32, 19.37, 'fd4e1f0f93adf4658a510bf27efe5c99b4f7b75cb016be84769ab9ffba848e01', '2026-03-16 07:00:08', '2026-03-29 15:29:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(352, 1, '102室电表', 'DEV001', NULL, 1, NULL, 351.70, 67.16, 237.92, 13.93, 13.32, 19.37, '751617e4d13f789fbfda4373238d3220ad1d3ccc9ab2754516566d3aea65d824', '2026-03-16 08:00:08', '2026-03-29 15:29:16', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(353, 1, '102室电表', 'DEV001', NULL, 1, NULL, 355.22, 67.16, 241.44, 13.93, 13.32, 19.37, '93dbaeb844188c051eef2203368e75e07c5e9dbee23a09625d14a2c34d467eb5', '2026-03-16 09:00:08', '2026-03-29 15:29:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(354, 1, '102室电表', 'DEV001', NULL, 1, NULL, 358.16, 67.16, 244.38, 13.93, 13.32, 19.37, '6a140d693e97275fe75dc3f835288b66b04fe898c66df0679b151a4f73f89dab', '2026-03-16 10:00:08', '2026-03-29 15:29:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(355, 1, '102室电表', 'DEV001', NULL, 1, NULL, 361.30, 70.30, 244.38, 13.93, 13.32, 19.37, 'c663fbccae5698c3d4051a7315541eaae8ad40d40d19aa4653bea835b9002872', '2026-03-16 11:00:08', '2026-03-29 15:29:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(356, 1, '102室电表', 'DEV001', NULL, 1, NULL, 364.34, 73.34, 244.38, 13.93, 13.32, 19.37, 'dc5ab42701ee67388beefd675b4938903b3eaff413db07272b0bc0b647988df7', '2026-03-16 12:00:08', '2026-03-29 15:29:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(357, 1, '102室电表', 'DEV001', NULL, 1, NULL, 367.57, 73.34, 247.61, 13.93, 13.32, 19.37, '3ed4c47605fc7ed82ae24eaa4cab0ab07df276bfc905a40ccb488cc2604982d5', '2026-03-16 13:00:08', '2026-03-29 15:29:17', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(358, 1, '102室电表', 'DEV001', NULL, 1, NULL, 371.22, 73.34, 251.26, 13.93, 13.32, 19.37, 'a8619d34806b8dbbc6c80b1787e9ef32b251941e6517860eb7fd69ffc9e2a56f', '2026-03-16 14:00:08', '2026-03-29 15:29:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(359, 1, '102室电表', 'DEV001', NULL, 1, NULL, 374.07, 73.34, 254.11, 13.93, 13.32, 19.37, 'cb9de4e608720c1563afcf6bd6c7d0a5d8aa1c3a53eb0bccbc7960ee6c2d61e6', '2026-03-16 15:00:08', '2026-03-29 15:29:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(360, 1, '102室电表', 'DEV001', NULL, 1, NULL, 376.85, 73.34, 256.89, 13.93, 13.32, 19.37, 'bcc80124bc38f363b93202d97cad76132a7748b86fef38582aad748b3c07ed46', '2026-03-16 16:00:08', '2026-03-29 15:29:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(361, 1, '102室电表', 'DEV001', NULL, 1, NULL, 379.83, 73.34, 259.87, 13.93, 13.32, 19.37, '2674fb119bd50c47939258eacb22f7f3ed49db2a202c10285bd4a0239767c90a', '2026-03-16 17:00:08', '2026-03-29 15:29:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(362, 1, '102室电表', 'DEV001', NULL, 1, NULL, 379.99, 73.34, 259.87, 14.09, 13.32, 19.37, '3c8ac8b686498da3fd56c81e92341e138beb165989aee7784c7f1d4ba58237a5', '2026-03-16 18:00:08', '2026-03-29 15:29:18', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(363, 1, '102室电表', 'DEV001', NULL, 1, NULL, 380.16, 73.34, 259.87, 14.26, 13.32, 19.37, '2dc34a33c2239da8f96f125b9f84ec0aa304c1b592d85230ae2a2cd0cb0491ce', '2026-03-16 19:00:08', '2026-03-29 15:29:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(364, 1, '102室电表', 'DEV001', NULL, 1, NULL, 380.33, 73.34, 259.87, 14.43, 13.32, 19.37, '511ab9be8c9992f8c27dbe944157cbfcf04f64efb4e2d3edea53a72b0c39e761', '2026-03-16 20:00:08', '2026-03-29 15:29:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(365, 1, '102室电表', 'DEV001', NULL, 1, NULL, 380.46, 73.34, 259.87, 14.56, 13.32, 19.37, '5773edf9ee16bcdcd54aa7777d1ed74ceec439347f0d6cfce4ccfce10f95283e', '2026-03-16 21:00:08', '2026-03-29 15:29:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(366, 1, '102室电表', 'DEV001', NULL, 1, NULL, 380.61, 73.34, 259.87, 14.56, 13.47, 19.37, '014b6840fc164e09a40fee50915086e98a941f59308a94f10f838e3cb9ba5e90', '2026-03-16 22:00:08', '2026-03-29 15:29:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(367, 1, '102室电表', 'DEV001', NULL, 1, NULL, 380.77, 73.34, 259.87, 14.56, 13.63, 19.37, '0a16b5d1e71f998134928214c0ed5323105eb1d00d161f2809abf0f5924b313f', '2026-03-16 23:00:08', '2026-03-29 15:29:19', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(368, 1, '102室电表', 'DEV001', NULL, 1, NULL, 380.93, 73.34, 259.87, 14.56, 13.63, 19.53, 'e65c7be513ce60a74191a8ed3bed3fbd9ecc12eba44ced7fa52e10c609c0907a', '2026-03-17 00:00:08', '2026-03-29 15:29:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(369, 1, '102室电表', 'DEV001', NULL, 1, NULL, 381.10, 73.34, 259.87, 14.56, 13.63, 19.70, 'e2329c5c31ed23f79f2364a4712b41ffa80ab64a4e581569cb557eca1748567f', '2026-03-17 01:00:08', '2026-03-29 15:29:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(370, 1, '102室电表', 'DEV001', NULL, 1, NULL, 381.24, 73.34, 259.87, 14.56, 13.63, 19.84, '204f8c800220d0981332f34bda58487d17491ff823a7492a0c3cc148d046b64e', '2026-03-17 02:00:08', '2026-03-29 15:29:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(371, 1, '102室电表', 'DEV001', NULL, 1, NULL, 381.39, 73.34, 259.87, 14.56, 13.63, 19.99, '5b96d1edcc42e34745a01965f01637b1818f36896dbcae8fc2783acf58a15d53', '2026-03-17 03:00:08', '2026-03-29 15:29:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(372, 1, '102室电表', 'DEV001', NULL, 1, NULL, 381.54, 73.34, 259.87, 14.56, 13.63, 20.14, '55e7ac32e8e64f573a07aedecbd5840bef403808286be5cdf7a39e54ab2b1c5d', '2026-03-17 04:00:08', '2026-03-29 15:29:20', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(373, 1, '102室电表', 'DEV001', NULL, 1, NULL, 381.70, 73.34, 259.87, 14.56, 13.63, 20.30, 'c7aaa38b4b286fee1b6fcce1a7809336bfdb5b61ca167c655001b912b0ce184b', '2026-03-17 05:00:08', '2026-03-29 15:29:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(374, 1, '102室电表', 'DEV001', NULL, 1, NULL, 381.83, 73.34, 259.87, 14.56, 13.76, 20.30, '07534eb556ffe4f7abc04befa837866fe54d21846dc299f8d7d17ce13a269cc9', '2026-03-17 06:00:08', '2026-03-29 15:29:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(375, 1, '102室电表', 'DEV001', NULL, 1, NULL, 381.97, 73.34, 259.87, 14.56, 13.90, 20.30, '2871f7d5d37706eb7a41c6eecd44de57cb0072e33b3a22a51d84f2311681738a', '2026-03-17 07:00:08', '2026-03-29 15:29:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(376, 1, '102室电表', 'DEV001', NULL, 1, NULL, 382.11, 73.34, 260.01, 14.56, 13.90, 20.30, 'a2bbd6b9800065acbb8bc464099aeb6ebdb43e8421a04143006eeb2e414b1a6a', '2026-03-17 08:00:08', '2026-03-29 15:29:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(377, 1, '102室电表', 'DEV001', NULL, 1, NULL, 385.28, 73.34, 263.18, 14.56, 13.90, 20.30, 'cbf08022792093fe0f697fe777378be6226922c062920b6252ca0feeb0c2a796', '2026-03-17 09:00:08', '2026-03-29 15:29:21', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(378, 1, '102室电表', 'DEV001', NULL, 1, NULL, 388.86, 73.34, 266.76, 14.56, 13.90, 20.30, 'ceea71f099fc0e2a09b58da4d0158163c0e503376bf3fd41b3f07511e26ca318', '2026-03-17 10:00:08', '2026-03-29 15:29:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(379, 1, '102室电表', 'DEV001', NULL, 1, NULL, 391.64, 76.12, 266.76, 14.56, 13.90, 20.30, '7d3779c9361061ef5b69d91002ac99795347befbbcc47c6ad894ee2a80cf10e5', '2026-03-17 11:00:08', '2026-03-29 15:29:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(380, 1, '102室电表', 'DEV001', NULL, 1, NULL, 395.32, 79.80, 266.76, 14.56, 13.90, 20.30, '0160d839e262b3c682848c33b51fe5d03b2df73ebbced5bb6968c4707eac4368', '2026-03-17 12:00:08', '2026-03-29 15:29:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(381, 1, '102室电表', 'DEV001', NULL, 1, NULL, 398.20, 79.80, 269.64, 14.56, 13.90, 20.30, 'adfa291e41a722aabf28dacef2c246f26b3c6002ec6dee606fe5cc897760dd35', '2026-03-17 13:00:08', '2026-03-29 15:29:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(382, 1, '102室电表', 'DEV001', NULL, 1, NULL, 401.50, 79.80, 272.94, 14.56, 13.90, 20.30, '17ca9e09352cb4bd5ae33387ec84aba63171fab06741759bc390be213aae20c8', '2026-03-17 14:00:08', '2026-03-29 15:29:22', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(383, 1, '102室电表', 'DEV001', NULL, 1, NULL, 404.99, 79.80, 276.43, 14.56, 13.90, 20.30, 'ca26ea0159999549ff649e012dc8d2cd2211c256c189036afc2a2584bc4f151a', '2026-03-17 15:00:08', '2026-03-29 15:29:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(384, 1, '102室电表', 'DEV001', NULL, 1, NULL, 408.41, 79.80, 279.85, 14.56, 13.90, 20.30, 'a35a4aaf19650cd23abe7551059033aadc692be0c07d599700ece274ad7d4700', '2026-03-17 16:00:08', '2026-03-29 15:29:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(385, 1, '102室电表', 'DEV001', NULL, 1, NULL, 412.03, 79.80, 283.47, 14.56, 13.90, 20.30, '249aecf6e8d60646f2d70ad041b07492f2b13173ab05559343086bf2f99c389b', '2026-03-17 17:00:08', '2026-03-29 15:29:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(386, 1, '102室电表', 'DEV001', NULL, 1, NULL, 412.18, 79.80, 283.47, 14.71, 13.90, 20.30, 'c101ac6e3e17c34fc98d2e67d1b7ed276608ad06c3c153014d40975191e68b04', '2026-03-17 18:00:08', '2026-03-29 15:29:23', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(387, 1, '102室电表', 'DEV001', NULL, 1, NULL, 412.33, 79.80, 283.47, 14.86, 13.90, 20.30, '7904e2904053597c4b92a9305e71ef132bd97d962a33983a4249cdbaa10247da', '2026-03-17 19:00:08', '2026-03-29 15:29:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(388, 1, '102室电表', 'DEV001', NULL, 1, NULL, 412.48, 79.80, 283.47, 15.01, 13.90, 20.30, 'cc13b2078ae42f7ab7d315a92ea5b2834aa94993c5d231c3114681e111ea611f', '2026-03-17 20:00:08', '2026-03-29 15:29:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(389, 1, '102室电表', 'DEV001', NULL, 1, NULL, 412.64, 79.80, 283.47, 15.17, 13.90, 20.30, '7d13d01506da4782d0f2d00c9e62b1e294f2d1c9a8eb4ec1923fe7b8ec68f5ef', '2026-03-17 21:00:08', '2026-03-29 15:29:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(390, 1, '102室电表', 'DEV001', NULL, 1, NULL, 412.77, 79.80, 283.47, 15.17, 14.03, 20.30, '77f2263a7640f6640548c5f06a9d2d312d772f1f05f4241e6eeeb06862f49238', '2026-03-17 22:00:08', '2026-03-29 15:29:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(391, 1, '102室电表', 'DEV001', NULL, 1, NULL, 412.91, 79.80, 283.47, 15.17, 14.17, 20.30, '7804e38a1636af6c6da0960181fbb39909903a563206150c3579b9e8aa014df3', '2026-03-17 23:00:08', '2026-03-29 15:29:24', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(392, 1, '102室电表', 'DEV001', NULL, 1, NULL, 413.05, 79.80, 283.47, 15.17, 14.17, 20.44, 'bd2d09da61653fc79868d5b4a1aa14fe1ec8723134374da33eb964503bb88d26', '2026-03-18 00:00:08', '2026-03-29 15:29:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(393, 1, '102室电表', 'DEV001', NULL, 1, NULL, 413.20, 79.80, 283.47, 15.17, 14.17, 20.59, '52f427acf3430b5f778f13b2ee4d5c87e297540f2bc7f5764a302d7d67141c09', '2026-03-18 01:00:08', '2026-03-29 15:29:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(394, 1, '102室电表', 'DEV001', NULL, 1, NULL, 413.37, 79.80, 283.47, 15.17, 14.17, 20.76, 'd31101c3ce97069b07173394df0586364f4079dae89c4e6066cc4166b82e1f7d', '2026-03-18 02:00:08', '2026-03-29 15:29:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(395, 1, '102室电表', 'DEV001', NULL, 1, NULL, 413.51, 79.80, 283.47, 15.17, 14.17, 20.90, '63cf95d2d264e8a07188a0383f17ccdf1d3987fc83d51c0710e68ded101e0205', '2026-03-18 03:00:08', '2026-03-29 15:29:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(396, 1, '102室电表', 'DEV001', NULL, 1, NULL, 413.64, 79.80, 283.47, 15.17, 14.17, 21.03, '0baac3f17f7d40d0620ad2c72e66c7549a6dc65c5cd0a881a1847dfe849846be', '2026-03-18 04:00:08', '2026-03-29 15:29:25', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(397, 1, '102室电表', 'DEV001', NULL, 1, NULL, 413.78, 79.80, 283.47, 15.17, 14.17, 21.17, '741e0fa422ae3857a99a2534023ff45a5caec4b24018958a771568719e99076f', '2026-03-18 05:00:08', '2026-03-29 15:29:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(398, 1, '102室电表', 'DEV001', NULL, 1, NULL, 413.94, 79.80, 283.47, 15.17, 14.33, 21.17, '20c8ada2a047921baa15a61d71f1f874ce4d97b406bc47814c06652fe3acb75a', '2026-03-18 06:00:08', '2026-03-29 15:29:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(399, 1, '102室电表', 'DEV001', NULL, 1, NULL, 414.11, 79.80, 283.47, 15.17, 14.50, 21.17, 'a742f4a392899c7c0fbb75204e3776ddc0a923ed948572fe1919afac4beeb457', '2026-03-18 07:00:08', '2026-03-29 15:29:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(400, 1, '102室电表', 'DEV001', NULL, 1, NULL, 414.28, 79.80, 283.64, 15.17, 14.50, 21.17, '3dc1eea9063efa333dae58ac4b651040cb55d1bab077336c6850e3cadf52feaf', '2026-03-18 08:00:08', '2026-03-29 15:29:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(401, 1, '102室电表', 'DEV001', NULL, 1, NULL, 417.06, 79.80, 286.42, 15.17, 14.50, 21.17, 'b44bf918516d745e5e0ed9d99d6dba01af1e265e6f3d2db727d5f6fd93d18a33', '2026-03-18 09:00:08', '2026-03-29 15:29:26', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(402, 1, '102室电表', 'DEV001', NULL, 1, NULL, 420.26, 79.80, 289.62, 15.17, 14.50, 21.17, '45b40dcc810e5fd15132195597ae7f618c595600ddc404184a0c7b546392db25', '2026-03-18 10:00:08', '2026-03-29 15:29:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(403, 1, '102室电表', 'DEV001', NULL, 1, NULL, 423.65, 83.19, 289.62, 15.17, 14.50, 21.17, '839e43b743d099259c27612c4966b069bc10fb5534e3a1484dc924f8f7284b6b', '2026-03-18 11:00:08', '2026-03-29 15:29:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(404, 1, '102室电表', 'DEV001', NULL, 1, NULL, 426.95, 86.49, 289.62, 15.17, 14.50, 21.17, '9af60e30b3a4c164758a117ca3c3816fc3b9fa5931a77b4b7a97ab6c880e90af', '2026-03-18 12:00:08', '2026-03-29 15:29:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(405, 1, '102室电表', 'DEV001', NULL, 1, NULL, 430.44, 86.49, 293.11, 15.17, 14.50, 21.17, '2ce478f1a667b5214d3206ae14d4449c757e6f4ddf6035b8551cd0f3692b2f6e', '2026-03-18 13:00:08', '2026-03-29 15:29:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(406, 1, '102室电表', 'DEV001', NULL, 1, NULL, 433.35, 86.49, 296.02, 15.17, 14.50, 21.17, '1b3d3635dd7504d711c6474e0399103ddb2890124dd15545bb36dec79d608001', '2026-03-18 14:00:08', '2026-03-29 15:29:27', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(407, 1, '102室电表', 'DEV001', NULL, 1, NULL, 439.49, 86.49, 302.16, 15.17, 14.50, 21.17, '716e44c0ae03a1080944a90e56b72e94ceec570fa11849dc28f785e445f27303', '2026-03-18 16:00:08', '2026-03-29 15:29:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(408, 1, '102室电表', 'DEV001', NULL, 1, NULL, 442.72, 86.49, 305.39, 15.17, 14.50, 21.17, 'eba2218adddc54977a4c3281bdaa5449ee6a4cf37400055e3052c5086755a9d8', '2026-03-18 17:00:08', '2026-03-29 15:29:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(409, 1, '102室电表', 'DEV001', NULL, 1, NULL, 442.85, 86.49, 305.39, 15.30, 14.50, 21.17, 'a1d499dc72c91e35608b774a9374bdf65edc12c7af781eb39feb6224d11c41c9', '2026-03-18 18:00:08', '2026-03-29 15:29:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(410, 1, '102室电表', 'DEV001', NULL, 1, NULL, 442.99, 86.49, 305.39, 15.44, 14.50, 21.17, 'dd48c6e0ec90a395df5b12e194e8d769bca31dd94b2cac65013517e2b551f608', '2026-03-18 19:00:08', '2026-03-29 15:29:28', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(411, 1, '102室电表', 'DEV001', NULL, 1, NULL, 443.12, 86.49, 305.39, 15.57, 14.50, 21.17, '9df65bf08d2f941dcc40684074629e555390468d7f38fc089307d6ba6b7050dc', '2026-03-18 20:00:08', '2026-03-29 15:29:29', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(412, 1, '102室电表', 'DEV001', NULL, 1, NULL, 443.26, 86.49, 305.39, 15.71, 14.50, 21.17, 'e1bbe72bfc786f672ffebc31a61626d3be1c55c0a32750ac2decba6384a5dac8', '2026-03-18 21:00:08', '2026-03-29 15:29:29', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(413, 1, '102室电表', 'DEV001', NULL, 1, NULL, 443.42, 86.49, 305.39, 15.71, 14.66, 21.17, '6f9f33a482b8f92766adbc42718dcf69d698178461d6e55b9865df834a283896', '2026-03-18 22:00:08', '2026-03-29 15:29:29', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(414, 1, '102室电表', 'DEV001', NULL, 1, NULL, 443.59, 86.49, 305.39, 15.71, 14.83, 21.17, '31de18556b4e95bc063c89f8436978072380e90641e5a1200483a3162a9a77b3', '2026-03-18 23:00:08', '2026-03-29 15:29:29', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(415, 1, '102室电表', 'DEV001', NULL, 1, NULL, 443.72, 86.49, 305.39, 15.71, 14.83, 21.30, 'd86e215d160e9973ea4c24c1005d657d7c680eacecd3f48fa330644cdd7cb50c', '2026-03-19 00:00:08', '2026-03-29 15:29:29', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(416, 1, '102室电表', 'DEV001', NULL, 1, NULL, 443.86, 86.49, 305.39, 15.71, 14.83, 21.44, 'ddbebd231952cca7431f2947920756ff6dc4f71860ecf43537c174bc3a2f9f2e', '2026-03-19 01:00:08', '2026-03-29 15:29:30', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(417, 1, '102室电表', 'DEV001', NULL, 1, NULL, 444.02, 86.49, 305.39, 15.71, 14.83, 21.60, '70dca69a723e3b2d24e909a3ebd6d59ba1848eb50f112c94657fcbd8cec4f90f', '2026-03-19 02:00:08', '2026-03-29 15:29:30', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(418, 1, '102室电表', 'DEV001', NULL, 1, NULL, 444.19, 86.49, 305.39, 15.71, 14.83, 21.77, '710b567842152dcdcbc2b9b36b8ae62d05df8655177eec732077d397a4f87d26', '2026-03-19 03:00:08', '2026-03-29 15:29:30', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(419, 1, '102室电表', 'DEV001', NULL, 1, NULL, 444.35, 86.49, 305.39, 15.71, 14.83, 21.93, 'ba8c7dee1091101830c0d62ce184b0f8935fc2cd0f48e647655b9a253781dffa', '2026-03-19 04:00:08', '2026-03-29 15:29:30', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(420, 1, '102室电表', 'DEV001', NULL, 1, NULL, 444.52, 86.49, 305.39, 15.71, 14.83, 22.10, '648c8e1c896a98f77b325211326c266654e6c5abf15051de6738f8bc5d3aadaa', '2026-03-19 05:00:08', '2026-03-29 15:29:30', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(421, 1, '102室电表', 'DEV001', NULL, 1, NULL, 444.67, 86.49, 305.39, 15.71, 14.98, 22.10, '5099d6c2dde18110e9d533b3d166c4217743cf402728191b9a4339a6d996142e', '2026-03-19 06:00:08', '2026-03-29 15:29:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(422, 1, '102室电表', 'DEV001', NULL, 1, NULL, 444.82, 86.49, 305.39, 15.71, 15.13, 22.10, 'd357ca02375600a8c1846099f8231a06c5c0cb5ad4cccd6b1c50551bcc6593fa', '2026-03-19 07:00:08', '2026-03-29 15:29:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(423, 1, '102室电表', 'DEV001', NULL, 1, NULL, 444.97, 86.49, 305.54, 15.71, 15.13, 22.10, '0d8a1ed477cfeb8361347fe11200ea90899726e9c1f9141d7c1b3621ef069ed3', '2026-03-19 08:00:08', '2026-03-29 15:29:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(424, 1, '102室电表', 'DEV001', NULL, 1, NULL, 448.39, 86.49, 308.96, 15.71, 15.13, 22.10, 'b989247af0d5b021110fc986a4aaa008b241361e2fdf1bce6968fffc1f8ddc34', '2026-03-19 09:00:08', '2026-03-29 15:29:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(425, 1, '102室电表', 'DEV001', NULL, 1, NULL, 451.24, 86.49, 311.81, 15.71, 15.13, 22.10, '136524513172d0a7f3ea601dbb41f942e496c898530c822957ce774fdefb5bb1', '2026-03-19 10:00:08', '2026-03-29 15:29:31', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(426, 1, '102室电表', 'DEV001', NULL, 1, NULL, 454.28, 89.53, 311.81, 15.71, 15.13, 22.10, '92a6c7763f8748a843633cb9e6c28e034c805e1b29fd0c1e0f8988e924f6b8bd', '2026-03-19 11:00:08', '2026-03-29 15:29:32', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(427, 1, '102室电表', 'DEV001', NULL, 1, NULL, 457.22, 92.47, 311.81, 15.71, 15.13, 22.10, 'd64a01ffc867c49fb7a7ca7df311d93ec53433eea32f6c2278a54b2b06c650d2', '2026-03-19 12:00:08', '2026-03-29 15:29:32', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(428, 1, '102室电表', 'DEV001', NULL, 1, NULL, 460.36, 92.47, 314.95, 15.71, 15.13, 22.10, 'a23bdb63ea32760af1d787ec5f59dd431a942efda164f0d12094c67702b89256', '2026-03-19 13:00:08', '2026-03-29 15:29:32', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(429, 1, '102室电表', 'DEV001', NULL, 1, NULL, 463.91, 92.47, 318.50, 15.71, 15.13, 22.10, 'ae006cbe191bd9bb3ba909f482f54afca5f92b831858677391388460af9f1308', '2026-03-19 14:00:08', '2026-03-29 15:29:32', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(430, 1, '102室电表', 'DEV001', NULL, 1, NULL, 470.34, 92.47, 324.93, 15.71, 15.13, 22.10, '79288b5e6eda7bec3b310f9b92ca6a5c91109ffad53650049e28871a79f5f555', '2026-03-19 16:00:08', '2026-03-29 15:29:33', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(431, 1, '102室电表', 'DEV001', NULL, 1, NULL, 473.22, 92.47, 327.81, 15.71, 15.13, 22.10, 'b8b0cc1939833c44bb87c5e0c95f58ff84e0701280d301b5f909b3a270373f5b', '2026-03-19 17:00:08', '2026-03-29 15:29:33', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(432, 1, '102室电表', 'DEV001', NULL, 1, NULL, 473.38, 92.47, 327.81, 15.87, 15.13, 22.10, '2ae90f834f9afc9628d4eff0742bff5c6e7b4d8da89d6ecf17814fe7ef0d2d83', '2026-03-19 18:00:08', '2026-03-29 15:29:33', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(433, 1, '102室电表', 'DEV001', NULL, 1, NULL, 473.55, 92.47, 327.81, 16.04, 15.13, 22.10, 'ed7ceac1c4af1a0650f1b1da05290b57438b83edce858be3dad06c8650b9e694', '2026-03-19 19:00:08', '2026-03-29 15:29:33', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(434, 1, '102室电表', 'DEV001', NULL, 1, NULL, 473.71, 92.47, 327.81, 16.20, 15.13, 22.10, 'c26ad3205f228cbe97b18eceedadcdbb8a36e79171455c0e4a8c9a6de4976f3d', '2026-03-19 20:00:08', '2026-03-29 15:29:34', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(435, 1, '102室电表', 'DEV001', NULL, 1, NULL, 473.88, 92.47, 327.81, 16.37, 15.13, 22.10, '0f05e746981b18954b7e462a8f6548b8ee9dfe649b68dbdcb971c4be7b6d6cae', '2026-03-19 21:00:08', '2026-03-29 15:29:34', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(436, 1, '102室电表', 'DEV001', NULL, 1, NULL, 474.02, 92.47, 327.81, 16.37, 15.27, 22.10, '8bd029d6f13de7b50269df7ea18f53b879d1d096c6959316d6e4f300dc3ddb84', '2026-03-19 22:00:08', '2026-03-29 15:29:34', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(437, 1, '102室电表', 'DEV001', NULL, 1, NULL, 474.17, 92.47, 327.81, 16.37, 15.42, 22.10, '882277c38e8cc914f946a8ec3a6e6ed6d612c800ad01985f4d075a242afac3ad', '2026-03-19 23:00:08', '2026-03-29 15:29:34', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(438, 1, '102室电表', 'DEV001', NULL, 1, NULL, 474.33, 92.47, 327.81, 16.37, 15.42, 22.26, '5a5bc74073b7ebaac3f856e4a425b920a027fd59535500e2bc8ff92121656d81', '2026-03-20 00:00:08', '2026-03-29 15:29:34', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(439, 1, '102室电表', 'DEV001', NULL, 1, NULL, 474.50, 92.47, 327.81, 16.37, 15.42, 22.43, '2f7c5aa1d348c583fb9ec0ffc8edb86f7e9d034c2cf608f04fa94db5daa86cd3', '2026-03-20 01:00:08', '2026-03-29 15:29:35', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(440, 1, '102室电表', 'DEV001', NULL, 1, NULL, 474.64, 92.47, 327.81, 16.37, 15.42, 22.57, 'f4c0554711eac905c0ca38ad9fd8c5906fefc42fa05ed7875d24c32ff57b0d66', '2026-03-20 02:00:08', '2026-03-29 15:29:35', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(441, 1, '102室电表', 'DEV001', NULL, 1, NULL, 474.79, 92.47, 327.81, 16.37, 15.42, 22.72, '4808d35cf4fd1c33376ac617cadd98f88972f7b19c9afdd57aa0b8a97ddf25fb', '2026-03-20 03:00:08', '2026-03-29 15:29:35', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(442, 1, '102室电表', 'DEV001', NULL, 1, NULL, 474.93, 92.47, 327.81, 16.37, 15.42, 22.86, 'd7bb8123bf348aa34c22f1f614f6b2745d889f5c005f51edd113cf7b404339a0', '2026-03-20 04:00:08', '2026-03-29 15:29:35', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(443, 1, '102室电表', 'DEV001', NULL, 1, NULL, 475.08, 92.47, 327.81, 16.37, 15.42, 23.01, 'b962d2deab763708267d745d743318e23c74a90fdbd3d883b0af6d5b178043eb', '2026-03-20 05:00:08', '2026-03-29 15:29:35', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(444, 1, '102室电表', 'DEV001', NULL, 1, NULL, 475.21, 92.47, 327.81, 16.37, 15.55, 23.01, 'e5e0110c127663ffff7c62ec4bf8053c6ff22a2c773c198ac1074c6275c9b0bb', '2026-03-20 06:00:08', '2026-03-29 15:29:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(445, 1, '102室电表', 'DEV001', NULL, 1, NULL, 475.35, 92.47, 327.81, 16.37, 15.69, 23.01, 'b177914d10cd9a71a83dcac868dac2c1e05e5d0f4751297ff694ec0927f34f9c', '2026-03-20 07:00:08', '2026-03-29 15:29:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(446, 1, '102室电表', 'DEV001', NULL, 1, NULL, 475.48, 92.47, 327.94, 16.37, 15.69, 23.01, '9c3d02fbf970940da52908967239d460404411eef9ec947ec46ef3ccf7611607', '2026-03-20 08:00:08', '2026-03-29 15:29:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(447, 1, '102室电表', 'DEV001', NULL, 1, NULL, 478.52, 92.47, 330.98, 16.37, 15.69, 23.01, '2f7bb4a2628559e721b420ab175472825d14c2843c8c5485fb9f4259e7f84556', '2026-03-20 09:00:08', '2026-03-29 15:29:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(448, 1, '102室电表', 'DEV001', NULL, 1, NULL, 481.98, 92.47, 334.44, 16.37, 15.69, 23.01, 'a7356ee7d055b241e2925b49088fe8b0c8e724c377f90a1f44fc68aa39fe8a44', '2026-03-20 10:00:08', '2026-03-29 15:29:36', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(449, 1, '102室电表', 'DEV001', NULL, 1, NULL, 485.63, 96.12, 334.44, 16.37, 15.69, 23.01, '83ce63a2b5c2c0fb234cde04118e3d0afa647bb4e45f0b31fbc408661321744d', '2026-03-20 11:00:08', '2026-03-29 15:29:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(450, 1, '102室电表', 'DEV001', NULL, 1, NULL, 489.18, 99.67, 334.44, 16.37, 15.69, 23.01, '8ac0ab066e8111058c505bf0473e4ac55517dfa3cae7b0df4e78f53444637ba1', '2026-03-20 12:00:08', '2026-03-29 15:29:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(451, 1, '102室电表', 'DEV001', NULL, 1, NULL, 491.93, 99.67, 337.19, 16.37, 15.69, 23.01, 'aed42134ec140f9d3c011af2f4cfac0f4ad94bfbcb57e1194d1289a52c35d062', '2026-03-20 13:00:08', '2026-03-29 15:29:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(452, 1, '102室电表', 'DEV001', NULL, 1, NULL, 495.10, 99.67, 340.36, 16.37, 15.69, 23.01, '820470a4d2c2eed0960ad078686a1ed7f5990ef14fef389d0d5d2ddf06c3d57d', '2026-03-20 14:00:08', '2026-03-29 15:29:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(453, 1, '102室电表', 'DEV001', NULL, 1, NULL, 498.46, 99.67, 343.72, 16.37, 15.69, 23.01, '13e3a84485a8aeef8c32a90c54de27ac1c3460d5a4402189091cf60e9cf50467', '2026-03-20 15:00:08', '2026-03-29 15:29:37', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(454, 1, '102室电表', 'DEV001', NULL, 1, NULL, 501.76, 99.67, 347.02, 16.37, 15.69, 23.01, '2971428d51a51f4ec267b77bad421c8c6f15628ed2431ebf11704377d2e8d249', '2026-03-20 16:00:08', '2026-03-29 15:29:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(455, 1, '102室电表', 'DEV001', NULL, 1, NULL, 505.25, 99.67, 350.51, 16.37, 15.69, 23.01, 'bbc45ae452f9a66047c7821b3ef6ab89717e473ac7ed83a65689d65ebfd826ef', '2026-03-20 17:00:08', '2026-03-29 15:29:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(456, 1, '102室电表', 'DEV001', NULL, 1, NULL, 505.39, 99.67, 350.51, 16.51, 15.69, 23.01, '25149563c45fdb479b25ed21da1590d64b366b33982892e52ec296ad2476ad5e', '2026-03-20 18:00:08', '2026-03-29 15:29:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(457, 1, '102室电表', 'DEV001', NULL, 1, NULL, 505.54, 99.67, 350.51, 16.66, 15.69, 23.01, 'd1e983e45b2cc135be77e96aec0c6c3304bbe6ab93c3dd3c023e5aed7bc8e647', '2026-03-20 19:00:08', '2026-03-29 15:29:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(458, 1, '102室电表', 'DEV001', NULL, 1, NULL, 505.68, 99.67, 350.51, 16.80, 15.69, 23.01, 'e38190643001c3ef017f9cc5dd01d033ac35b6eeee136b4eae813ff4f847623a', '2026-03-20 20:00:08', '2026-03-29 15:29:38', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(459, 1, '102室电表', 'DEV001', NULL, 1, NULL, 505.83, 99.67, 350.51, 16.95, 15.69, 23.01, '673a0c241401537c48c6b8b9b8c6aabebee3e28e99c79c13ee5167fa8e4a6938', '2026-03-20 21:00:08', '2026-03-29 15:29:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(460, 1, '102室电表', 'DEV001', NULL, 1, NULL, 506.00, 99.67, 350.51, 16.95, 15.86, 23.01, '7d2d8a38bcc371ab5b5b8d07add24da0e27c37c63139fe3e5e4bc5f2059c34b6', '2026-03-20 22:00:08', '2026-03-29 15:29:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(461, 1, '102室电表', 'DEV001', NULL, 1, NULL, 506.14, 99.67, 350.51, 16.95, 16.00, 23.01, '07ff0340866d3b93a9032490ccd18b02aeed6b78950afb691566ef375e77698f', '2026-03-20 23:00:08', '2026-03-29 15:29:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(462, 1, '102室电表', 'DEV001', NULL, 1, NULL, 506.51, 99.67, 350.51, 16.95, 16.00, 23.38, 'b1fa1d9af5a37866e38e3c45603e63b7d90b79e02567743e3d8b383a548f8506', '2026-03-21 00:00:08', '2026-03-29 15:29:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(463, 1, '102室电表', 'DEV001', NULL, 1, NULL, 506.91, 99.67, 350.51, 16.95, 16.00, 23.78, 'b0c08f9ea76ba770fb1e33b9269287b23a9d3414d71dcbc809c3bdeaa9a4c45a', '2026-03-21 01:00:08', '2026-03-29 15:29:39', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(464, 1, '102室电表', 'DEV001', NULL, 1, NULL, 507.36, 99.67, 350.51, 16.95, 16.00, 24.23, '6db3fbe867cb778c2a1624871c77a83b2a06066b4824c28317380a6bf368d2fc', '2026-03-21 02:00:08', '2026-03-29 15:29:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(465, 1, '102室电表', 'DEV001', NULL, 1, NULL, 507.71, 99.67, 350.51, 16.95, 16.00, 24.58, 'b1588cfc313fc319b41ed33ea4b3960fbb4ec2c36d6eebe5463457efb082a0d7', '2026-03-21 03:00:08', '2026-03-29 15:29:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(466, 1, '102室电表', 'DEV001', NULL, 1, NULL, 508.17, 99.67, 350.51, 16.95, 16.00, 25.04, '4bbf3392174cad94cd1c660d83c54791b2e66a20346efc6538fe3f2b3243830f', '2026-03-21 04:00:08', '2026-03-29 15:29:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(467, 1, '102室电表', 'DEV001', NULL, 1, NULL, 508.53, 99.67, 350.51, 16.95, 16.00, 25.40, '458c97ec041d475f20106cabfdf93b4ef7be245dfbf5839e12feceae15d9807c', '2026-03-21 05:00:08', '2026-03-29 15:29:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(468, 1, '102室电表', 'DEV001', NULL, 1, NULL, 508.95, 99.67, 350.51, 16.95, 16.42, 25.40, '2dfde2e0b6244011ce899aec17011b1eee0c672ef26a106ebb4aaa8cbcbc12a6', '2026-03-21 06:00:08', '2026-03-29 15:29:40', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(469, 1, '102室电表', 'DEV001', NULL, 1, NULL, 509.39, 99.67, 350.51, 16.95, 16.86, 25.40, '56089beb19fbedd2a164505a4d9e2029a09753ac45f8c7170e76d4a6a9e14fb5', '2026-03-21 07:00:08', '2026-03-29 15:29:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(470, 1, '102室电表', 'DEV001', NULL, 1, NULL, 509.83, 99.67, 350.95, 16.95, 16.86, 25.40, '0d3efd9280933c84c5eef57ddf47a2c5fa065bcf3ceb544294c55134777cc9a6', '2026-03-21 08:00:08', '2026-03-29 15:29:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(471, 1, '102室电表', 'DEV001', NULL, 1, NULL, 510.29, 99.67, 351.41, 16.95, 16.86, 25.40, '27087f847d449400040b22cdb579333084f13741227eca3a669ce00dfbe0dadf', '2026-03-21 09:00:08', '2026-03-29 15:29:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(472, 1, '102室电表', 'DEV001', NULL, 1, NULL, 510.68, 99.67, 351.80, 16.95, 16.86, 25.40, 'a794c47d753fa6e5e62492bd464b2dbfff4a9986690ddeb3692c4ccb30399503', '2026-03-21 10:00:08', '2026-03-29 15:29:41', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(473, 1, '102室电表', 'DEV001', NULL, 1, NULL, 511.09, 100.08, 351.80, 16.95, 16.86, 25.40, 'acd332294f151cc0b0b2d5f68d35869897fe5c7d45d96b82eb7807fa9fbe4f6a', '2026-03-21 11:00:08', '2026-03-29 15:29:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(474, 1, '102室电表', 'DEV001', NULL, 1, NULL, 511.49, 100.48, 351.80, 16.95, 16.86, 25.40, '8493f7edc5ed7db491c5000bdc5738fd86ee666ef22c462e36432a0ec743fcf4', '2026-03-21 12:00:08', '2026-03-29 15:29:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(475, 1, '102室电表', 'DEV001', NULL, 1, NULL, 511.91, 100.48, 352.22, 16.95, 16.86, 25.40, 'a003f800df0792fcf3593898f25acf1ad701ff64cd1f5fd316cdc2a39487d866', '2026-03-21 13:00:08', '2026-03-29 15:29:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(476, 1, '102室电表', 'DEV001', NULL, 1, NULL, 512.26, 100.48, 352.57, 16.95, 16.86, 25.40, '8a3c5f7c7a07af620fab91115c496b8be9fc2af1f283d3f9e5916c9d494b51e7', '2026-03-21 14:00:08', '2026-03-29 15:29:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(477, 1, '102室电表', 'DEV001', NULL, 1, NULL, 512.64, 100.48, 352.95, 16.95, 16.86, 25.40, 'b9791013c39f79ed763d73767902f3605a9aaba653ae81b2822e11f77764bb89', '2026-03-21 15:00:08', '2026-03-29 15:29:42', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(478, 1, '102室电表', 'DEV001', NULL, 1, NULL, 513.01, 100.48, 353.32, 16.95, 16.86, 25.40, '47519387023d28aebadb244b350d8d3b7c5ef54e67fd8049f31d16fc0f27cfd1', '2026-03-21 16:00:08', '2026-03-29 15:29:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(479, 1, '102室电表', 'DEV001', NULL, 1, NULL, 513.40, 100.48, 353.71, 16.95, 16.86, 25.40, '0ad3f72c75bd159399bd2b1f431f2799b041f7367a6fb5bd315ddcaa7948493a', '2026-03-21 17:00:08', '2026-03-29 15:29:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(480, 1, '102室电表', 'DEV001', NULL, 1, NULL, 513.85, 100.48, 353.71, 17.40, 16.86, 25.40, '57062461d01d230de5ebca9b02ac8a5deb9e76a769df916736677545a311461e', '2026-03-21 18:00:08', '2026-03-29 15:29:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(481, 1, '102室电表', 'DEV001', NULL, 1, NULL, 514.20, 100.48, 353.71, 17.75, 16.86, 25.40, '30407f467d46f6777adf35cbbc502f6c047b32c73667d2421eedde6ef7e1f304', '2026-03-21 19:00:08', '2026-03-29 15:29:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(482, 1, '102室电表', 'DEV001', NULL, 1, NULL, 514.54, 100.48, 353.71, 18.09, 16.86, 25.40, 'bb4e5c1cbc675b82c4df5e79417815840f13934b507d9c41c2f0a7b59eb4a661', '2026-03-21 20:00:08', '2026-03-29 15:29:43', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(483, 1, '102室电表', 'DEV001', NULL, 1, NULL, 514.90, 100.48, 353.71, 18.45, 16.86, 25.40, '6a527215ec21b66fc1cbc95dc7e42e5b4d229b6632c86fab0147c7be37ab9ef7', '2026-03-21 21:00:08', '2026-03-29 15:29:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(484, 1, '102室电表', 'DEV001', NULL, 1, NULL, 515.32, 100.48, 353.71, 18.45, 17.28, 25.40, 'd138eb9b9c0e5ed00aaffd4edd0e3f973b1b782391b6f1e210c9d509c5cafc14', '2026-03-21 22:00:08', '2026-03-29 15:29:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(485, 1, '102室电表', 'DEV001', NULL, 1, NULL, 515.76, 100.48, 353.71, 18.45, 17.72, 25.40, '00c30dada6aa0a22e982763c044c9f7a69b5482539bd9540941512f69f862a4e', '2026-03-21 23:00:08', '2026-03-29 15:29:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(486, 1, '102室电表', 'DEV001', NULL, 1, NULL, 516.21, 100.48, 353.71, 18.45, 17.72, 25.85, '810107c980d017afb8f701984f8f70f29e9cdbbea7cde9299c0c83190d8e3f37', '2026-03-22 00:00:08', '2026-03-29 15:29:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(487, 1, '102室电表', 'DEV001', NULL, 1, NULL, 516.56, 100.48, 353.71, 18.45, 17.72, 26.20, '697a1aa6ae4a7dd17683118274dc915ac7435b3a77838e432f8c13d4a9e32b56', '2026-03-22 01:00:08', '2026-03-29 15:29:44', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(488, 1, '102室电表', 'DEV001', NULL, 1, NULL, 516.96, 100.48, 353.71, 18.45, 17.72, 26.60, 'fdfbcc9cbde8670123b32e229b98d634df1b9247b53ccc745e9a5b00fdda2b2b', '2026-03-22 02:00:08', '2026-03-29 15:29:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(489, 1, '102室电表', 'DEV001', NULL, 1, NULL, 517.38, 100.48, 353.71, 18.45, 17.72, 27.02, '2cb103c09848e3f60f8fc7b7ec8bb250c7fe81b5d077a266832b0460fc0d668d', '2026-03-22 03:00:08', '2026-03-29 15:29:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(490, 1, '102室电表', 'DEV001', NULL, 1, NULL, 517.79, 100.48, 353.71, 18.45, 17.72, 27.43, '07b6aa0b08e7672f5aad42ced69cb46392c90d10160a71d3d5f0cc4432c97ad3', '2026-03-22 04:00:08', '2026-03-29 15:29:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(491, 1, '102室电表', 'DEV001', NULL, 1, NULL, 518.23, 100.48, 353.71, 18.45, 17.72, 27.87, 'd1568b7a8962615d5cd68a018d10884ddb74b2f638f233943f246680507840b0', '2026-03-22 05:00:08', '2026-03-29 15:29:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(492, 1, '102室电表', 'DEV001', NULL, 1, NULL, 518.60, 100.48, 353.71, 18.45, 18.09, 27.87, 'e974a0a8fb1eae106fe6ab3b5cf4dba5caed024acaf2f15918027ac1de9bdd35', '2026-03-22 06:00:08', '2026-03-29 15:29:45', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(493, 1, '102室电表', 'DEV001', NULL, 1, NULL, 519.00, 100.48, 353.71, 18.45, 18.49, 27.87, 'fe07bb8cb2ab5cdb86bddcd4906d0be2783315ad4cc3652e590badb1745b1519', '2026-03-22 07:00:08', '2026-03-29 15:29:46', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(494, 1, '102室电表', 'DEV001', NULL, 1, NULL, 519.39, 100.48, 354.10, 18.45, 18.49, 27.87, '78642a459ef0fdd7258e3e146dcf4a40faf354759a7467622353312df6402ced', '2026-03-22 08:00:08', '2026-03-29 15:29:46', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(495, 1, '102室电表', 'DEV001', NULL, 1, NULL, 519.80, 100.48, 354.51, 18.45, 18.49, 27.87, 'b5ee7c6271cbc90bcfb5eae5ee2473729eccda56369f148b5153bda70689a2cc', '2026-03-22 09:00:08', '2026-03-29 15:29:46', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(496, 1, '102室电表', 'DEV001', NULL, 1, NULL, 520.14, 100.48, 354.85, 18.45, 18.49, 27.87, '4fed12b5a11e3a93ff5330af12adddef95fa7e1a168e1ac1e8c2b5fef920e6ab', '2026-03-22 10:00:08', '2026-03-29 15:29:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(497, 1, '102室电表', 'DEV001', NULL, 1, NULL, 520.50, 100.84, 354.85, 18.45, 18.49, 27.87, 'c5897e0990983f9d5aedd7fd56f738a4d60c992f3f7d5a9c7d6ccc85d62d61a5', '2026-03-22 11:00:08', '2026-03-29 15:29:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(498, 1, '102室电表', 'DEV001', NULL, 1, NULL, 520.85, 101.19, 354.85, 18.45, 18.49, 27.87, '81b4d0ee92998b85bd14524a9d78ab1ebc6d9f3096fed1935eeaac5b6e62448d', '2026-03-22 12:00:08', '2026-03-29 15:29:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(499, 1, '102室电表', 'DEV001', NULL, 1, NULL, 521.23, 101.19, 355.23, 18.45, 18.49, 27.87, 'f90cf202714a5a5962f5b9dbf867b23251adb974367a35406b4a8d7b5183cf4f', '2026-03-22 13:00:08', '2026-03-29 15:29:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(500, 1, '102室电表', 'DEV001', NULL, 1, NULL, 521.66, 101.19, 355.66, 18.45, 18.49, 27.87, '4d22613071a388dabcc5b2adcc0b6854f836c9e809e2aed167c35ecd0dae9b04', '2026-03-22 14:00:08', '2026-03-29 15:29:47', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(501, 1, '102室电表', 'DEV001', NULL, 1, NULL, 522.11, 101.19, 356.11, 18.45, 18.49, 27.87, '8b66ce5bc942a1e60d196b27177780e9f1c1ecd88fcff0cbe0ed82ead9fab238', '2026-03-22 15:00:08', '2026-03-29 15:29:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(502, 1, '102室电表', 'DEV001', NULL, 1, NULL, 522.55, 101.19, 356.55, 18.45, 18.49, 27.87, '84dde79fc76275f90182b977296ebeed6e95ca1601841a8828ed80ac35d0a86a', '2026-03-22 16:00:08', '2026-03-29 15:29:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(503, 1, '102室电表', 'DEV001', NULL, 1, NULL, 522.89, 101.19, 356.89, 18.45, 18.49, 27.87, 'cea56bfc348d3da3992fa71b10b4ae0b3d226736afad62085e35d0367250203b', '2026-03-22 17:00:08', '2026-03-29 15:29:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(504, 1, '102室电表', 'DEV001', NULL, 1, NULL, 523.29, 101.19, 356.89, 18.85, 18.49, 27.87, 'c8a48d1b62a7776cfe0d6a6a701a81922a9bd514cba2c0ed807ba336ddb5e56e', '2026-03-22 18:00:08', '2026-03-29 15:29:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(505, 1, '102室电表', 'DEV001', NULL, 1, NULL, 523.72, 101.19, 356.89, 19.28, 18.49, 27.87, 'a3aca893e2158d28fb749dbeaecddfd589c9060b54b8e1268bee1f4bd471d20a', '2026-03-22 19:00:08', '2026-03-29 15:29:48', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(506, 1, '102室电表', 'DEV001', NULL, 1, NULL, 524.14, 101.19, 356.89, 19.70, 18.49, 27.87, '4a3a99cbe85dbfc80c1f3523459758978cc678602d9d17912ec221c3a90af753', '2026-03-22 20:00:08', '2026-03-29 15:29:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(507, 1, '102室电表', 'DEV001', NULL, 1, NULL, 524.58, 101.19, 356.89, 20.14, 18.49, 27.87, '88cbb937226b32a8415a69f3fc6a8df009196e7fb2567243d52009c9b12c5744', '2026-03-22 21:00:08', '2026-03-29 15:29:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(508, 1, '102室电表', 'DEV001', NULL, 1, NULL, 524.95, 101.19, 356.89, 20.14, 18.86, 27.87, '20ec5b7395b693f34ee8022e2cb30739322c8d0349978e56c251c5be9f9753cb', '2026-03-22 22:00:08', '2026-03-29 15:29:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(509, 1, '102室电表', 'DEV001', NULL, 1, NULL, 525.34, 101.19, 356.89, 20.14, 19.25, 27.87, '9df86919c5b4d2a529acf9cd457d37dc3f13cf79ec54b1d63acf988680ff1b5b', '2026-03-22 23:00:08', '2026-03-29 15:29:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(510, 1, '102室电表', 'DEV001', NULL, 1, NULL, 525.49, 101.19, 356.89, 20.14, 19.25, 28.02, 'b853b6b7b43133af3c2f48f95a8249ea9ce626c47fae7f8e5775dcd689e40606', '2026-03-23 00:00:08', '2026-03-29 15:29:49', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(511, 1, '102室电表', 'DEV001', NULL, 1, NULL, 525.65, 101.19, 356.89, 20.14, 19.25, 28.18, '3d25bb75ee245a0ddb6a728c3bd8f6ccd2882c77de841a090bbc8d6d81eeaebd', '2026-03-23 01:00:08', '2026-03-29 15:29:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(512, 1, '102室电表', 'DEV001', NULL, 1, NULL, 525.78, 101.19, 356.89, 20.14, 19.25, 28.31, '35ad8230c4d4556de4b01d4c093063abc6d8d058c1d9ef1c5d5f4c8fd54ff879', '2026-03-23 02:00:08', '2026-03-29 15:29:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(513, 1, '102室电表', 'DEV001', NULL, 1, NULL, 525.92, 101.19, 356.89, 20.14, 19.25, 28.45, 'b1c69c6928a2f8df8a59b0a1e937acc086958b51ac26f486cc8ed1c33db3fc4d', '2026-03-23 03:00:08', '2026-03-29 15:29:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(514, 1, '102室电表', 'DEV001', NULL, 1, NULL, 526.06, 101.19, 356.89, 20.14, 19.25, 28.59, 'e821a98c9820b5e956f379884923709b04cfced6d8258b4a729b7bb3df113fe7', '2026-03-23 04:00:08', '2026-03-29 15:29:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(515, 1, '102室电表', 'DEV001', NULL, 1, NULL, 526.21, 101.19, 356.89, 20.14, 19.25, 28.74, 'e3eb80adb7cb00de420b11a3a47c9f36773da9486a1183453634eac0f869758c', '2026-03-23 05:00:08', '2026-03-29 15:29:50', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(516, 1, '102室电表', 'DEV001', NULL, 1, NULL, 526.38, 101.19, 356.89, 20.14, 19.42, 28.74, 'ed3bcdb8ac421b7a69a7276864e5dbe2e345a6b402582279e4d58360527c15c8', '2026-03-23 06:00:08', '2026-03-29 15:29:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(517, 1, '102室电表', 'DEV001', NULL, 1, NULL, 526.51, 101.19, 356.89, 20.14, 19.55, 28.74, '8a43055a943a04398c13a8f1669a896b53b29eb6060a2c5093ab01b4bbf4c5c8', '2026-03-23 07:00:08', '2026-03-29 15:29:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(518, 1, '102室电表', 'DEV001', NULL, 1, NULL, 526.64, 101.19, 357.02, 20.14, 19.55, 28.74, 'bb8e48f2515ece8e19edca0f0e97153373e6c29b92ad8878709771d47d29fdaa', '2026-03-23 08:00:08', '2026-03-29 15:29:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(519, 1, '102室电表', 'DEV001', NULL, 1, NULL, 529.58, 101.19, 359.96, 20.14, 19.55, 28.74, 'a642f57201766085e2a8b5e32b65d05058c28bc029d62ef0f4c540bc9ec0e5f0', '2026-03-23 09:00:08', '2026-03-29 15:29:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(520, 1, '102室电表', 'DEV001', NULL, 1, NULL, 532.94, 101.19, 363.32, 20.14, 19.55, 28.74, '230f12d8089984b3f222783ad827fa9c8e37385c1dd681e3cce14f7ab602488c', '2026-03-23 10:00:08', '2026-03-29 15:29:51', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(521, 1, '102室电表', 'DEV001', NULL, 1, NULL, 536.49, 104.74, 363.32, 20.14, 19.55, 28.74, 'fa07d714e52df98603d2d17809af45615e7ab0fb9a05b95e522e5599cb496787', '2026-03-23 11:00:08', '2026-03-29 15:29:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(522, 1, '102室电表', 'DEV001', NULL, 1, NULL, 539.95, 108.20, 363.32, 20.14, 19.55, 28.74, 'd4715d69840ee4b6755e2fba6751a98d746db42e49d2429fdd1898d549b13c44', '2026-03-23 12:00:08', '2026-03-29 15:29:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(523, 1, '102室电表', 'DEV001', NULL, 1, NULL, 543.60, 108.20, 366.97, 20.14, 19.55, 28.74, '71e773a108793edcdf0aac01fc6281e1757692ebd2afbb66d4e87254f528ca01', '2026-03-23 13:00:08', '2026-03-29 15:29:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(524, 1, '102室电表', 'DEV001', NULL, 1, NULL, 546.67, 108.20, 370.04, 20.14, 19.55, 28.74, '85692c88b63dbbab00980f06dfd34a6ad2223ee541113412c8702c9e90631d05', '2026-03-23 14:00:08', '2026-03-29 15:29:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(525, 1, '102室电表', 'DEV001', NULL, 1, NULL, 549.93, 108.20, 373.30, 20.14, 19.55, 28.74, '76b2533487cd3f768a4ce7e4f0fa5249786f8645221a79760d92f269a350f469', '2026-03-23 15:00:08', '2026-03-29 15:29:52', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(526, 1, '102室电表', 'DEV001', NULL, 1, NULL, 553.13, 108.20, 376.50, 20.14, 19.55, 28.74, '3527e2e7d33ff4a7bd6aa584c09095f2b6f255e57d4a7ac92b6ac1d94b4bfbc3', '2026-03-23 16:00:08', '2026-03-29 15:29:53', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(527, 1, '102室电表', 'DEV001', NULL, 1, NULL, 556.52, 108.20, 379.89, 20.14, 19.55, 28.74, '9c46508fb2659a1f18327abdd0122004ba8dca44c946f5df37ec4d26a2ee6496', '2026-03-23 17:00:08', '2026-03-29 15:29:53', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(528, 1, '102室电表', 'DEV001', NULL, 1, NULL, 556.66, 108.20, 379.89, 20.28, 19.55, 28.74, '1ec7b6006a0a3113a3e65fcb63743c5f9c15639985570949ae7349432da9edcb', '2026-03-23 18:00:08', '2026-03-29 15:29:53', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(529, 1, '102室电表', 'DEV001', NULL, 1, NULL, 556.80, 108.20, 379.89, 20.42, 19.55, 28.74, '1f4bc6ec22a808dc35c4a7a4a8650d440c744bd3dded3beaa5dc61d70baefa01', '2026-03-23 19:00:08', '2026-03-29 15:29:53', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(530, 1, '102室电表', 'DEV001', NULL, 1, NULL, 556.94, 108.20, 379.89, 20.56, 19.55, 28.74, '7f5b9604b7b447fd551b62d29a495ab8372f3733e56b3cba9b7429cbbf8a5e7a', '2026-03-23 20:00:08', '2026-03-29 15:29:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(531, 1, '102室电表', 'DEV001', NULL, 1, NULL, 557.09, 108.20, 379.89, 20.71, 19.55, 28.74, '453a614e4fb7ba343d37b4405b52567f6ce3865e756d0e26833a930321db3ef4', '2026-03-23 21:00:08', '2026-03-29 15:29:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(532, 1, '102室电表', 'DEV001', NULL, 1, NULL, 557.26, 108.20, 379.89, 20.71, 19.72, 28.74, '66d0d9de230661c1b55b73fde803ca18e54df2fb655b2f288ddbf2869c08878f', '2026-03-23 22:00:08', '2026-03-29 15:29:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(533, 1, '102室电表', 'DEV001', NULL, 1, NULL, 557.39, 108.20, 379.89, 20.71, 19.85, 28.74, '079b16ca4fd5dc181038e178de3e6229ec4cd764a9d744d24ced9d4446b5524e', '2026-03-23 23:00:08', '2026-03-29 15:29:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(534, 1, '102室电表', 'DEV001', NULL, 1, NULL, 557.52, 108.20, 379.89, 20.71, 19.85, 28.87, '3b2f29c4952514995fb720ed8f23c6dd3575df038d5b8949ea7184e90de62a8c', '2026-03-24 00:00:08', '2026-03-29 15:29:54', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(535, 1, '102室电表', 'DEV001', NULL, 1, NULL, 557.66, 108.20, 379.89, 20.71, 19.85, 29.01, '21cdc9095f19f409443b22e8accc90e12f2307048c7c7f2ff8ae2acddd63cc6f', '2026-03-24 01:00:08', '2026-03-29 15:29:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(536, 1, '102室电表', 'DEV001', NULL, 1, NULL, 557.82, 108.20, 379.89, 20.71, 19.85, 29.17, 'c0f345568c9c06de3fc3ed27944641afb0e5a667538f06b6bcd6586758a0609d', '2026-03-24 02:00:08', '2026-03-29 15:29:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(537, 1, '102室电表', 'DEV001', NULL, 1, NULL, 557.99, 108.20, 379.89, 20.71, 19.85, 29.34, '1552cfb8cd79bbfa720a3d8bdd96ca605c69b0c61578b5b156cdee0c1d9713a1', '2026-03-24 03:00:08', '2026-03-29 15:29:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(538, 1, '102室电表', 'DEV001', NULL, 1, NULL, 558.16, 108.20, 379.89, 20.71, 19.85, 29.51, '765c7a898cf215b20301358bc1522e8655270a85ced17401f2b69c187790e6e0', '2026-03-24 04:00:08', '2026-03-29 15:29:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(539, 1, '102室电表', 'DEV001', NULL, 1, NULL, 558.29, 108.20, 379.89, 20.71, 19.85, 29.64, '409cd45edb0202866bb5020ecfaf0c410ca6f8119ad4f92e5acbb20dc4474b57', '2026-03-24 05:00:08', '2026-03-29 15:29:55', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(540, 1, '102室电表', 'DEV001', NULL, 1, NULL, 558.44, 108.20, 379.89, 20.71, 20.00, 29.64, 'c4453225ea60d180b22b68b315c01bb6d9d0df39c969af9ae754b2e1270c69b0', '2026-03-24 06:00:08', '2026-03-29 15:29:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(541, 1, '102室电表', 'DEV001', NULL, 1, NULL, 558.60, 108.20, 379.89, 20.71, 20.16, 29.64, '02a383f5eb9796412421a6bf48c9670a52e57214685a29d10b0c6dc2c8f9db54', '2026-03-24 07:00:08', '2026-03-29 15:29:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(542, 1, '102室电表', 'DEV001', NULL, 1, NULL, 558.76, 108.20, 380.05, 20.71, 20.16, 29.64, 'c06d657e9e9daea846e0a06f349038e23fb66609c71b3fbeb11c917464eef899', '2026-03-24 08:00:08', '2026-03-29 15:29:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(543, 1, '102室电表', 'DEV001', NULL, 1, NULL, 562.28, 108.20, 383.57, 20.71, 20.16, 29.64, '95c0ae5197664019042ae372ba79c6c3889f0d99331bfbfc29ce99a2ba60fc1e', '2026-03-24 09:00:08', '2026-03-29 15:29:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(544, 1, '102室电表', 'DEV001', NULL, 1, NULL, 565.22, 108.20, 386.51, 20.71, 20.16, 29.64, '98d911633072857defe4ccaacabb8a3c6b50839c90860ff913d3a8012a0e9e96', '2026-03-24 10:00:08', '2026-03-29 15:29:56', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(545, 1, '102室电表', 'DEV001', NULL, 1, NULL, 568.36, 111.34, 386.51, 20.71, 20.16, 29.64, '51b3b22eaf9b59ae2121ecb1d8bad3027678c933492a3a159add409ecb9fafbb', '2026-03-24 11:00:08', '2026-03-29 15:29:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(546, 1, '102室电表', 'DEV001', NULL, 1, NULL, 571.40, 114.38, 386.51, 20.71, 20.16, 29.64, '9eafce3f09c39fb7941b9bd138d86195e31dcba4c7fa73ce70d27266e60eb53c', '2026-03-24 12:00:08', '2026-03-29 15:29:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(547, 1, '102室电表', 'DEV001', NULL, 1, NULL, 574.63, 114.38, 389.74, 20.71, 20.16, 29.64, '7920ec614c810e7ecbd02fae6b9a4fa6bb814558acff8847c46c3cc254d3d67b', '2026-03-24 13:00:08', '2026-03-29 15:29:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(548, 1, '102室电表', 'DEV001', NULL, 1, NULL, 578.28, 114.38, 393.39, 20.71, 20.16, 29.64, 'f449c4b53f71459b76b9fc835624d756007d125dd7656d1bba1ca014ee661ad0', '2026-03-24 14:00:08', '2026-03-29 15:29:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(549, 1, '102室电表', 'DEV001', NULL, 1, NULL, 581.13, 114.38, 396.24, 20.71, 20.16, 29.64, 'db783238cadb3c00fc0d0c42eb1ffcb6c006cb6376ca3adaad0ee3c7efa576dc', '2026-03-24 15:00:08', '2026-03-29 15:29:57', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(550, 1, '102室电表', 'DEV001', NULL, 1, NULL, 583.91, 114.38, 399.02, 20.71, 20.16, 29.64, 'f1ea0be7fff2720e8b0f4e7ed4adf1a72be46991c628941f91255531a482753c', '2026-03-24 16:00:08', '2026-03-29 15:29:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(551, 1, '102室电表', 'DEV001', NULL, 1, NULL, 586.89, 114.38, 402.00, 20.71, 20.16, 29.64, '6d97c3a77392ce4559cc883c8088bb3727623e3cd3996d8e7359507336f78494', '2026-03-24 17:00:08', '2026-03-29 15:29:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(552, 1, '102室电表', 'DEV001', NULL, 1, NULL, 587.05, 114.38, 402.00, 20.87, 20.16, 29.64, '44d6778eb78d873c3a951fdc5e2854971f82bb8f0bcde843201f80e875c835f3', '2026-03-24 18:00:08', '2026-03-29 15:29:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(553, 1, '102室电表', 'DEV001', NULL, 1, NULL, 587.22, 114.38, 402.00, 21.04, 20.16, 29.64, '0247a280e6d69ea95a3db28943542e87722da9615276daa59d156e7aa15b2cfa', '2026-03-24 19:00:08', '2026-03-29 15:29:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(554, 1, '102室电表', 'DEV001', NULL, 1, NULL, 587.39, 114.38, 402.00, 21.21, 20.16, 29.64, '35b6a7e43b3497a0a5ed826d46bcc21d771eb933ab9fcea430e08b84b1998375', '2026-03-24 20:00:08', '2026-03-29 15:29:58', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(555, 1, '102室电表', 'DEV001', NULL, 1, NULL, 587.52, 114.38, 402.00, 21.34, 20.16, 29.64, 'be676628f05eb180aba32489901978a40a00cf9988e9bd0f9e969868102fd2e2', '2026-03-24 21:00:08', '2026-03-29 15:29:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(556, 1, '102室电表', 'DEV001', NULL, 1, NULL, 587.67, 114.38, 402.00, 21.34, 20.31, 29.64, '04a54228caa7c78aad06dc433262ab91facc29d9aac4c836f526c96b73a7d01b', '2026-03-24 22:00:08', '2026-03-29 15:29:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(557, 1, '102室电表', 'DEV001', NULL, 1, NULL, 587.83, 114.38, 402.00, 21.34, 20.47, 29.64, '4d52526f2c81f8cfc9c93a037f30c5e2287748acca59a40a6ae4863022aa5ec2', '2026-03-24 23:00:08', '2026-03-29 15:29:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(558, 1, '102室电表', 'DEV001', NULL, 1, NULL, 587.99, 114.38, 402.00, 21.34, 20.47, 29.80, '02c395699653531be6b332b1b945eb56003c64644c6f9cded990d53619e678f6', '2026-03-25 00:00:08', '2026-03-29 15:29:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(559, 1, '102室电表', 'DEV001', NULL, 1, NULL, 588.16, 114.38, 402.00, 21.34, 20.47, 29.97, 'e0c7c5917f499f6e2a446b774d0c74b56a78d1afd35247e12831d23d088ddb51', '2026-03-25 01:00:08', '2026-03-29 15:29:59', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(560, 1, '102室电表', 'DEV001', NULL, 1, NULL, 588.30, 114.38, 402.00, 21.34, 20.47, 30.11, '51f2659803565d850de7f8b968d06f9858a06c53714d7179c468fb9feb381c3f', '2026-03-25 02:00:08', '2026-03-29 15:30:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(561, 1, '102室电表', 'DEV001', NULL, 1, NULL, 588.45, 114.38, 402.00, 21.34, 20.47, 30.26, '02c17af96ecd9c17594f40612c8b6673ce0281be2b0fda9c884222d61267d2b9', '2026-03-25 03:00:08', '2026-03-29 15:30:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(562, 1, '102室电表', 'DEV001', NULL, 1, NULL, 588.60, 114.38, 402.00, 21.34, 20.47, 30.41, 'e82f71e044dd758ae7018879af0fe5709db3a4eaef0fde5a079b5dea45571f58', '2026-03-25 04:00:08', '2026-03-29 15:30:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(563, 1, '102室电表', 'DEV001', NULL, 1, NULL, 588.76, 114.38, 402.00, 21.34, 20.47, 30.57, '0153fe302e01039dabdcee6865a9319da28f5744540fc968ef898280f24907f4', '2026-03-25 05:00:08', '2026-03-29 15:30:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(564, 1, '102室电表', 'DEV001', NULL, 1, NULL, 588.89, 114.38, 402.00, 21.34, 20.60, 30.57, '3073a4f34c0392bcf1bab141c834f10d33e06da88fe98dbf5a35508987947f74', '2026-03-25 06:00:08', '2026-03-29 15:30:00', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(565, 1, '102室电表', 'DEV001', NULL, 1, NULL, 589.03, 114.38, 402.00, 21.34, 20.74, 30.57, 'dcd02afeece3255ac48d41584e94b34829a1c87182bed938cc3e606e39479bc5', '2026-03-25 07:00:08', '2026-03-29 15:30:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(566, 1, '102室电表', 'DEV001', NULL, 1, NULL, 589.17, 114.38, 402.14, 21.34, 20.74, 30.57, 'ab8e59ad82ce354ffe214afce2391cf12f161b31fe285454ee128a689bd795a6', '2026-03-25 08:00:08', '2026-03-29 15:30:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(567, 1, '102室电表', 'DEV001', NULL, 1, NULL, 592.31, 114.38, 405.28, 21.34, 20.74, 30.57, 'b25b22769233ffb225ff14742e8c4b3333114ad04cf0e89acb7fe66032062603', '2026-03-25 09:00:08', '2026-03-29 15:30:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(568, 1, '102室电表', 'DEV001', NULL, 1, NULL, 595.86, 114.38, 408.83, 21.34, 20.74, 30.57, '8a017ed94b2f63ea9fbc373e19e4ad3077bfc280f3d677f878ca4ddf73ca5af2', '2026-03-25 10:00:08', '2026-03-29 15:30:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(569, 1, '102室电表', 'DEV001', NULL, 1, NULL, 598.61, 117.13, 408.83, 21.34, 20.74, 30.57, '245f28e1e92ac4ee2d699ca54c1556f3b1b0fa94d30fd52529f4a6514409b70d', '2026-03-25 11:00:08', '2026-03-29 15:30:01', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(570, 1, '102室电表', 'DEV001', NULL, 1, NULL, 602.26, 120.78, 408.83, 21.34, 20.74, 30.57, 'c1ed1ad11619e4424b6d58c83c8690da7d39f7259f5169d445541edbef37cc58', '2026-03-25 12:00:08', '2026-03-29 15:30:02', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(571, 1, '102室电表', 'DEV001', NULL, 1, NULL, 605.11, 120.78, 411.68, 21.34, 20.74, 30.57, '6559a8a1436be567ff3a9efea89e5c5122f020f7c0bbc91feaf48e961534a914', '2026-03-25 13:00:08', '2026-03-29 15:30:02', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(572, 1, '102室电表', 'DEV001', NULL, 1, NULL, 608.37, 120.78, 414.94, 21.34, 20.74, 30.57, '85bf4b363a88da2e617e863cc9af0eb454c8c8aaba77a9548e93b67c6819766c', '2026-03-25 14:00:08', '2026-03-29 15:30:02', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(573, 1, '102室电表', 'DEV001', NULL, 1, NULL, 611.83, 120.78, 418.40, 21.34, 20.74, 30.57, '2620fb1cad6a7680f36252fcba127792724d4569d74c03b5c2a4feab2c0b330c', '2026-03-25 15:00:08', '2026-03-29 15:30:02', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(574, 1, '102室电表', 'DEV001', NULL, 1, NULL, 615.22, 120.78, 421.79, 21.34, 20.74, 30.57, '4c42ddadd6e16b5bba1a0ee276ed7a0ceeafe4b9a63b0e347b5f752beaf7d0a7', '2026-03-25 16:00:08', '2026-03-29 15:30:03', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(575, 1, '102室电表', 'DEV001', NULL, 1, NULL, 618.80, 120.78, 425.37, 21.34, 20.74, 30.57, 'e42c5447d80dd7ee24ea9c5e784ba9d6ee732a47c26cb90b017f2e05b546eca8', '2026-03-25 17:00:08', '2026-03-29 15:30:03', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(576, 1, '102室电表', 'DEV001', NULL, 1, NULL, 618.94, 120.78, 425.37, 21.48, 20.74, 30.57, '625c69d882e50c4e9db0789798dd969a5b581cf250341fdfbcd9d32691f640b8', '2026-03-25 18:00:08', '2026-03-29 15:30:03', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(577, 1, '102室电表', 'DEV001', NULL, 1, NULL, 619.09, 120.78, 425.37, 21.63, 20.74, 30.57, '2fa9ed83dfc7abee0b1fd8c6e446134d68c86cec95ff93e3bf15dcd1093dbc15', '2026-03-25 19:00:08', '2026-03-29 15:30:03', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(578, 1, '102室电表', 'DEV001', NULL, 1, NULL, 619.24, 120.78, 425.37, 21.78, 20.74, 30.57, '9a615e2ad4a1cb87e17f44a17366a01e9da9b2fb8398b55d7519543e3277f48b', '2026-03-25 20:00:08', '2026-03-29 15:30:03', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(579, 1, '102室电表', 'DEV001', NULL, 1, NULL, 619.40, 120.78, 425.37, 21.94, 20.74, 30.57, '46711ffa7d0ac337783e2337eec9d71d451f170adcbb297b4fa141730e3ccb6a', '2026-03-25 21:00:08', '2026-03-29 15:30:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(580, 1, '102室电表', 'DEV001', NULL, 1, NULL, 619.53, 120.78, 425.37, 21.94, 20.87, 30.57, 'c98ed5f4c30701971e581903821f88b15faee0aa6c5786c3e0f53f60958ceab7', '2026-03-25 22:00:08', '2026-03-29 15:30:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(581, 1, '102室电表', 'DEV001', NULL, 1, NULL, 619.67, 120.78, 425.37, 21.94, 21.01, 30.57, 'ab82d396b0aef4b8b821f862a860f5eddee3c1b622e0279ca03522dcf21f1968', '2026-03-25 23:00:08', '2026-03-29 15:30:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(582, 1, '102室电表', 'DEV001', NULL, 1, NULL, 619.81, 120.78, 425.37, 21.94, 21.01, 30.71, '5a75dc1021e215b010c307a48b1674b41572f15e8b18f55fbd4ed39180f12042', '2026-03-26 00:00:08', '2026-03-29 15:30:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(583, 1, '102室电表', 'DEV001', NULL, 1, NULL, 619.96, 120.78, 425.37, 21.94, 21.01, 30.86, '8590e5e3dced018c551088570bf900b06437ddd40a9999a95a951887f3cdd8bb', '2026-03-26 01:00:08', '2026-03-29 15:30:04', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(584, 1, '102室电表', 'DEV001', NULL, 1, NULL, 620.13, 120.78, 425.37, 21.94, 21.01, 31.03, '0b6c31a2ae1162b6ae96159432c5bc4fc566e37b02f4b2c725303a3bef06d170', '2026-03-26 02:00:08', '2026-03-29 15:30:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(585, 1, '102室电表', 'DEV001', NULL, 1, NULL, 620.27, 120.78, 425.37, 21.94, 21.01, 31.17, '6b2792888d65b4be8ce4aeca1a29f0aa02a5cc77c05d451936e7414c86899297', '2026-03-26 03:00:08', '2026-03-29 15:30:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(586, 1, '102室电表', 'DEV001', NULL, 1, NULL, 620.40, 120.78, 425.37, 21.94, 21.01, 31.30, '3588bb8323c8873323e0773ec6fd4382d757471e55677b3f95d16fc25f0e528b', '2026-03-26 04:00:08', '2026-03-29 15:30:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(587, 1, '102室电表', 'DEV001', NULL, 1, NULL, 620.54, 120.78, 425.37, 21.94, 21.01, 31.44, '52d2eac767b4fee3029b63cfb43092b1b606ffcf31ed0b4f574f49a198149447', '2026-03-26 05:00:08', '2026-03-29 15:30:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(588, 1, '102室电表', 'DEV001', NULL, 1, NULL, 620.70, 120.78, 425.37, 21.94, 21.17, 31.44, '2afcd0cacca13debc8f265a91bb5d5d923edbc5336ead67f3a2ac7431515ea2a', '2026-03-26 06:00:08', '2026-03-29 15:30:05', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(589, 1, '102室电表', 'DEV001', NULL, 1, NULL, 620.87, 120.78, 425.37, 21.94, 21.34, 31.44, '6a885e43c78d002b72b79df5d3eb768b3a0576d2d3d242f1ee662d38867de4ea', '2026-03-26 07:00:08', '2026-03-29 15:30:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(590, 1, '102室电表', 'DEV001', NULL, 1, NULL, 621.04, 120.78, 425.54, 21.94, 21.34, 31.44, 'dcd0dcbc9a5a7aac0a3c756c5e5ca9089756da45cc3531fb008a4222bd771708', '2026-03-26 08:00:08', '2026-03-29 15:30:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(591, 1, '102室电表', 'DEV001', NULL, 1, NULL, 623.82, 120.78, 428.32, 21.94, 21.34, 31.44, 'ff2a627d2fcb0e45442742967142e0a29d5f55a33771b4d75d526b8272694ab4', '2026-03-26 09:00:08', '2026-03-29 15:30:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(592, 1, '102室电表', 'DEV001', NULL, 1, NULL, 627.02, 120.78, 431.52, 21.94, 21.34, 31.44, 'b89fcfec20c3f9cfa575a17a7f410bd19bc7af7ba8af95c778f5bc34d6c1a09b', '2026-03-26 10:00:08', '2026-03-29 15:30:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(593, 1, '102室电表', 'DEV001', NULL, 1, NULL, 630.41, 124.17, 431.52, 21.94, 21.34, 31.44, '4e77bcd81d5f6e9e7393b5e0fa031afe6ebd50bf305a3db74c1579c3802f8e16', '2026-03-26 11:00:08', '2026-03-29 15:30:06', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(594, 1, '102室电表', 'DEV001', NULL, 1, NULL, 633.71, 127.47, 431.52, 21.94, 21.34, 31.44, '332b80ef2c4e5a976f79145ae31de0d844947c3e743cf1a8a97008679ab27419', '2026-03-26 12:00:08', '2026-03-29 15:30:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(595, 1, '102室电表', 'DEV001', NULL, 1, NULL, 637.20, 127.47, 435.01, 21.94, 21.34, 31.44, '7ec04fd38060719fe83eeba7cde8bfef164e27a335b5b1cd9fe345757527ece1', '2026-03-26 13:00:08', '2026-03-29 15:30:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(596, 1, '102室电表', 'DEV001', NULL, 1, NULL, 640.11, 127.47, 437.92, 21.94, 21.34, 31.44, '428a0c3575d1efe08219130e36d8e977546d16d53b1c313671d152e558f654f7', '2026-03-26 14:00:08', '2026-03-29 15:30:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(597, 1, '102室电表', 'DEV001', NULL, 1, NULL, 643.21, 127.47, 441.02, 21.94, 21.34, 31.44, '038fda43fef7955a13f00eec6573e32b210973559bea6f5f6d5f7c7478891ae2', '2026-03-26 15:00:08', '2026-03-29 15:30:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(598, 1, '102室电表', 'DEV001', NULL, 1, NULL, 646.25, 127.47, 444.06, 21.94, 21.34, 31.44, '55f2fc6c843cf42b1adca8b282d27bb3cf0c74666eb3e0c9f94f0f4333e4d126', '2026-03-26 16:00:08', '2026-03-29 15:30:07', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(599, 1, '102室电表', 'DEV001', NULL, 1, NULL, 649.48, 127.47, 447.29, 21.94, 21.34, 31.44, '9ecddce98074c5674a02ea8f747e8c9b926bde6f4f719a523b3aec51b72c0555', '2026-03-26 17:00:08', '2026-03-29 15:30:08', 0);
INSERT INTO energy_electric_meter_power_record
(id, meter_id, meter_name, device_no, account_id, is_prepay, ct, power, power_higher, power_high, power_low, power_lower, power_deep_low, original_report_id, record_time, create_time, is_deleted)
VALUES(600, 1, '102室电表', 'DEV001', NULL, 1, NULL, 649.61, 127.47, 447.29, 22.07, 21.34, 31.44, 'ce1654fb453b1e98f024c43ab79cf850d03867fa57fe60983a95d44468c8f1a8', '2026-03-26 18:00:08', '2026-03-29 15:30:08', 0);

### 模拟消费
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(1, 1, 1, NULL, NULL, 6, 1, 0.45, 0.00, 0.45, 0.00, 0.00, 0.00, '2026-03-01 17:00:08', 2, 0.83, 0.00, 0.45, 0.38, 0.00, 0.00, '2026-03-01 18:00:08', 0.38, 0.00, 0.00, 0.38, 0.00, 0.00, '2026-03-01 18:00:08', '2026-03-29 15:28:04', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(2, 1, 1, NULL, NULL, 6, 2, 0.83, 0.00, 0.45, 0.38, 0.00, 0.00, '2026-03-01 18:00:08', 3, 1.24, 0.00, 0.45, 0.79, 0.00, 0.00, '2026-03-01 19:00:08', 0.41, 0.00, 0.00, 0.41, 0.00, 0.00, '2026-03-01 19:00:08', '2026-03-29 15:28:04', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(3, 1, 1, NULL, NULL, 6, 3, 1.24, 0.00, 0.45, 0.79, 0.00, 0.00, '2026-03-01 19:00:08', 4, 1.64, 0.00, 0.45, 1.19, 0.00, 0.00, '2026-03-01 20:00:08', 0.40, 0.00, 0.00, 0.40, 0.00, 0.00, '2026-03-01 20:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(4, 1, 1, NULL, NULL, 6, 4, 1.64, 0.00, 0.45, 1.19, 0.00, 0.00, '2026-03-01 20:00:08', 5, 2.06, 0.00, 0.45, 1.61, 0.00, 0.00, '2026-03-01 21:00:08', 0.42, 0.00, 0.00, 0.42, 0.00, 0.00, '2026-03-01 21:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(5, 1, 1, NULL, NULL, 6, 5, 2.06, 0.00, 0.45, 1.61, 0.00, 0.00, '2026-03-01 21:00:08', 6, 2.41, 0.00, 0.45, 1.61, 0.35, 0.00, '2026-03-01 22:00:08', 0.35, 0.00, 0.00, 0.00, 0.35, 0.00, '2026-03-01 22:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(6, 1, 1, NULL, NULL, 6, 6, 2.41, 0.00, 0.45, 1.61, 0.35, 0.00, '2026-03-01 22:00:08', 7, 2.78, 0.00, 0.45, 1.61, 0.72, 0.00, '2026-03-01 23:00:08', 0.37, 0.00, 0.00, 0.00, 0.37, 0.00, '2026-03-01 23:00:08', '2026-03-29 15:28:05', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(7, 1, 1, NULL, NULL, 6, 7, 2.78, 0.00, 0.45, 1.61, 0.72, 0.00, '2026-03-01 23:00:08', 8, 2.92, 0.00, 0.45, 1.61, 0.72, 0.14, '2026-03-02 00:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-02 00:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(8, 1, 1, NULL, NULL, 6, 8, 2.92, 0.00, 0.45, 1.61, 0.72, 0.14, '2026-03-02 00:00:08', 9, 3.07, 0.00, 0.45, 1.61, 0.72, 0.29, '2026-03-02 01:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-02 01:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(9, 1, 1, NULL, NULL, 6, 9, 3.07, 0.00, 0.45, 1.61, 0.72, 0.29, '2026-03-02 01:00:08', 10, 3.24, 0.00, 0.45, 1.61, 0.72, 0.46, '2026-03-02 02:00:08', 0.17, 0.00, 0.00, 0.00, 0.00, 0.17, '2026-03-02 02:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(10, 1, 1, NULL, NULL, 6, 10, 3.24, 0.00, 0.45, 1.61, 0.72, 0.46, '2026-03-02 02:00:08', 11, 3.37, 0.00, 0.45, 1.61, 0.72, 0.59, '2026-03-02 03:00:08', 0.13, 0.00, 0.00, 0.00, 0.00, 0.13, '2026-03-02 03:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(11, 1, 1, NULL, NULL, 6, 11, 3.37, 0.00, 0.45, 1.61, 0.72, 0.59, '2026-03-02 03:00:08', 12, 3.50, 0.00, 0.45, 1.61, 0.72, 0.72, '2026-03-02 04:00:08', 0.13, 0.00, 0.00, 0.00, 0.00, 0.13, '2026-03-02 04:00:08', '2026-03-29 15:28:06', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(12, 1, 1, NULL, NULL, 6, 12, 3.50, 0.00, 0.45, 1.61, 0.72, 0.72, '2026-03-02 04:00:08', 13, 3.64, 0.00, 0.45, 1.61, 0.72, 0.86, '2026-03-02 05:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-02 05:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(13, 1, 1, NULL, NULL, 6, 13, 3.64, 0.00, 0.45, 1.61, 0.72, 0.86, '2026-03-02 05:00:08', 14, 3.80, 0.00, 0.45, 1.61, 0.88, 0.86, '2026-03-02 06:00:08', 0.16, 0.00, 0.00, 0.00, 0.16, 0.00, '2026-03-02 06:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(14, 1, 1, NULL, NULL, 6, 14, 3.80, 0.00, 0.45, 1.61, 0.88, 0.86, '2026-03-02 06:00:08', 15, 3.97, 0.00, 0.45, 1.61, 1.05, 0.86, '2026-03-02 07:00:08', 0.17, 0.00, 0.00, 0.00, 0.17, 0.00, '2026-03-02 07:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(15, 1, 1, NULL, NULL, 6, 15, 3.97, 0.00, 0.45, 1.61, 1.05, 0.86, '2026-03-02 07:00:08', 16, 4.14, 0.00, 0.62, 1.61, 1.05, 0.86, '2026-03-02 08:00:08', 0.17, 0.00, 0.17, 0.00, 0.00, 0.00, '2026-03-02 08:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(16, 1, 1, NULL, NULL, 6, 16, 4.14, 0.00, 0.62, 1.61, 1.05, 0.86, '2026-03-02 08:00:08', 17, 6.89, 0.00, 3.37, 1.61, 1.05, 0.86, '2026-03-02 09:00:08', 2.75, 0.00, 2.75, 0.00, 0.00, 0.00, '2026-03-02 09:00:08', '2026-03-29 15:28:07', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(17, 1, 1, NULL, NULL, 6, 17, 6.89, 0.00, 3.37, 1.61, 1.05, 0.86, '2026-03-02 09:00:08', 18, 10.06, 0.00, 6.54, 1.61, 1.05, 0.86, '2026-03-02 10:00:08', 3.17, 0.00, 3.17, 0.00, 0.00, 0.00, '2026-03-02 10:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(18, 1, 1, NULL, NULL, 6, 18, 10.06, 0.00, 6.54, 1.61, 1.05, 0.86, '2026-03-02 10:00:08', 19, 13.42, 3.36, 6.54, 1.61, 1.05, 0.86, '2026-03-02 11:00:08', 3.36, 3.36, 0.00, 0.00, 0.00, 0.00, '2026-03-02 11:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(19, 1, 1, NULL, NULL, 6, 19, 13.42, 3.36, 6.54, 1.61, 1.05, 0.86, '2026-03-02 11:00:08', 20, 16.68, 6.62, 6.54, 1.61, 1.05, 0.86, '2026-03-02 12:00:08', 3.26, 3.26, 0.00, 0.00, 0.00, 0.00, '2026-03-02 12:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(20, 1, 1, NULL, NULL, 6, 20, 16.68, 6.62, 6.54, 1.61, 1.05, 0.86, '2026-03-02 12:00:08', 21, 20.14, 6.62, 10.00, 1.61, 1.05, 0.86, '2026-03-02 13:00:08', 3.46, 0.00, 3.46, 0.00, 0.00, 0.00, '2026-03-02 13:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(21, 1, 1, NULL, NULL, 6, 21, 20.14, 6.62, 10.00, 1.61, 1.05, 0.86, '2026-03-02 13:00:08', 22, 23.02, 6.62, 12.88, 1.61, 1.05, 0.86, '2026-03-02 14:00:08', 2.88, 0.00, 2.88, 0.00, 0.00, 0.00, '2026-03-02 14:00:08', '2026-03-29 15:28:08', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(22, 1, 1, NULL, NULL, 6, 22, 23.02, 6.62, 12.88, 1.61, 1.05, 0.86, '2026-03-02 14:00:08', 23, 26.09, 6.62, 15.95, 1.61, 1.05, 0.86, '2026-03-02 15:00:08', 3.07, 0.00, 3.07, 0.00, 0.00, 0.00, '2026-03-02 15:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(23, 1, 1, NULL, NULL, 6, 23, 26.09, 6.62, 15.95, 1.61, 1.05, 0.86, '2026-03-02 15:00:08', 24, 29.10, 6.62, 18.96, 1.61, 1.05, 0.86, '2026-03-02 16:00:08', 3.01, 0.00, 3.01, 0.00, 0.00, 0.00, '2026-03-02 16:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(24, 1, 1, NULL, NULL, 6, 24, 29.10, 6.62, 18.96, 1.61, 1.05, 0.86, '2026-03-02 16:00:08', 25, 32.30, 6.62, 22.16, 1.61, 1.05, 0.86, '2026-03-02 17:00:08', 3.20, 0.00, 3.20, 0.00, 0.00, 0.00, '2026-03-02 17:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(25, 1, 1, NULL, NULL, 6, 25, 32.30, 6.62, 22.16, 1.61, 1.05, 0.86, '2026-03-02 17:00:08', 26, 32.47, 6.62, 22.16, 1.78, 1.05, 0.86, '2026-03-02 18:00:08', 0.17, 0.00, 0.00, 0.17, 0.00, 0.00, '2026-03-02 18:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(26, 1, 1, NULL, NULL, 6, 26, 32.47, 6.62, 22.16, 1.78, 1.05, 0.86, '2026-03-02 18:00:08', 27, 32.61, 6.62, 22.16, 1.92, 1.05, 0.86, '2026-03-02 19:00:08', 0.14, 0.00, 0.00, 0.14, 0.00, 0.00, '2026-03-02 19:00:08', '2026-03-29 15:28:09', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(27, 1, 1, NULL, NULL, 6, 27, 32.61, 6.62, 22.16, 1.92, 1.05, 0.86, '2026-03-02 19:00:08', 28, 32.74, 6.62, 22.16, 2.05, 1.05, 0.86, '2026-03-02 20:00:08', 0.13, 0.00, 0.00, 0.13, 0.00, 0.00, '2026-03-02 20:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(28, 1, 1, NULL, NULL, 6, 28, 32.74, 6.62, 22.16, 2.05, 1.05, 0.86, '2026-03-02 20:00:08', 29, 32.88, 6.62, 22.16, 2.19, 1.05, 0.86, '2026-03-02 21:00:08', 0.14, 0.00, 0.00, 0.14, 0.00, 0.00, '2026-03-02 21:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(29, 1, 1, NULL, NULL, 6, 29, 32.88, 6.62, 22.16, 2.19, 1.05, 0.86, '2026-03-02 21:00:08', 30, 33.04, 6.62, 22.16, 2.19, 1.21, 0.86, '2026-03-02 22:00:08', 0.16, 0.00, 0.00, 0.00, 0.16, 0.00, '2026-03-02 22:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(30, 1, 1, NULL, NULL, 6, 30, 33.04, 6.62, 22.16, 2.19, 1.21, 0.86, '2026-03-02 22:00:08', 31, 33.21, 6.62, 22.16, 2.19, 1.38, 0.86, '2026-03-02 23:00:08', 0.17, 0.00, 0.00, 0.00, 0.17, 0.00, '2026-03-02 23:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(31, 1, 1, NULL, NULL, 6, 31, 33.21, 6.62, 22.16, 2.19, 1.38, 0.86, '2026-03-02 23:00:08', 32, 33.38, 6.62, 22.16, 2.19, 1.38, 1.03, '2026-03-03 00:00:08', 0.17, 0.00, 0.00, 0.00, 0.00, 0.17, '2026-03-03 00:00:08', '2026-03-29 15:28:10', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(32, 1, 1, NULL, NULL, 6, 32, 33.38, 6.62, 22.16, 2.19, 1.38, 1.03, '2026-03-03 00:00:08', 33, 33.52, 6.62, 22.16, 2.19, 1.38, 1.17, '2026-03-03 01:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-03 01:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(33, 1, 1, NULL, NULL, 6, 33, 33.52, 6.62, 22.16, 2.19, 1.38, 1.17, '2026-03-03 01:00:08', 34, 33.67, 6.62, 22.16, 2.19, 1.38, 1.32, '2026-03-03 02:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-03 02:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(34, 1, 1, NULL, NULL, 6, 34, 33.67, 6.62, 22.16, 2.19, 1.38, 1.32, '2026-03-03 02:00:08', 35, 33.83, 6.62, 22.16, 2.19, 1.38, 1.48, '2026-03-03 03:00:08', 0.16, 0.00, 0.00, 0.00, 0.00, 0.16, '2026-03-03 03:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(35, 1, 1, NULL, NULL, 6, 35, 33.83, 6.62, 22.16, 2.19, 1.38, 1.48, '2026-03-03 03:00:08', 36, 33.99, 6.62, 22.16, 2.19, 1.38, 1.64, '2026-03-03 04:00:08', 0.16, 0.00, 0.00, 0.00, 0.00, 0.16, '2026-03-03 04:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(36, 1, 1, NULL, NULL, 6, 36, 33.99, 6.62, 22.16, 2.19, 1.38, 1.64, '2026-03-03 04:00:08', 37, 34.16, 6.62, 22.16, 2.19, 1.38, 1.81, '2026-03-03 05:00:08', 0.17, 0.00, 0.00, 0.00, 0.00, 0.17, '2026-03-03 05:00:08', '2026-03-29 15:28:11', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(37, 1, 1, NULL, NULL, 6, 37, 34.16, 6.62, 22.16, 2.19, 1.38, 1.81, '2026-03-03 05:00:08', 38, 34.30, 6.62, 22.16, 2.19, 1.52, 1.81, '2026-03-03 06:00:08', 0.14, 0.00, 0.00, 0.00, 0.14, 0.00, '2026-03-03 06:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(38, 1, 1, NULL, NULL, 6, 38, 34.30, 6.62, 22.16, 2.19, 1.52, 1.81, '2026-03-03 06:00:08', 39, 34.45, 6.62, 22.16, 2.19, 1.67, 1.81, '2026-03-03 07:00:08', 0.15, 0.00, 0.00, 0.00, 0.15, 0.00, '2026-03-03 07:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(39, 1, 1, NULL, NULL, 6, 39, 34.45, 6.62, 22.16, 2.19, 1.67, 1.81, '2026-03-03 07:00:08', 40, 34.60, 6.62, 22.31, 2.19, 1.67, 1.81, '2026-03-03 08:00:08', 0.15, 0.00, 0.15, 0.00, 0.00, 0.00, '2026-03-03 08:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(40, 1, 1, NULL, NULL, 6, 40, 34.60, 6.62, 22.31, 2.19, 1.67, 1.81, '2026-03-03 08:00:08', 41, 37.99, 6.62, 25.70, 2.19, 1.67, 1.81, '2026-03-03 09:00:08', 3.39, 0.00, 3.39, 0.00, 0.00, 0.00, '2026-03-03 09:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(41, 1, 1, NULL, NULL, 6, 41, 37.99, 6.62, 25.70, 2.19, 1.67, 1.81, '2026-03-03 09:00:08', 42, 40.81, 6.62, 28.52, 2.19, 1.67, 1.81, '2026-03-03 10:00:08', 2.82, 0.00, 2.82, 0.00, 0.00, 0.00, '2026-03-03 10:00:08', '2026-03-29 15:28:12', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(42, 1, 1, NULL, NULL, 6, 42, 40.81, 6.62, 28.52, 2.19, 1.67, 1.81, '2026-03-03 10:00:08', 43, 43.82, 9.63, 28.52, 2.19, 1.67, 1.81, '2026-03-03 11:00:08', 3.01, 3.01, 0.00, 0.00, 0.00, 0.00, '2026-03-03 11:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(43, 1, 1, NULL, NULL, 6, 43, 43.82, 9.63, 28.52, 2.19, 1.67, 1.81, '2026-03-03 11:00:08', 44, 46.73, 12.54, 28.52, 2.19, 1.67, 1.81, '2026-03-03 12:00:08', 2.91, 2.91, 0.00, 0.00, 0.00, 0.00, '2026-03-03 12:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(44, 1, 1, NULL, NULL, 6, 44, 46.73, 12.54, 28.52, 2.19, 1.67, 1.81, '2026-03-03 12:00:08', 45, 49.83, 12.54, 31.62, 2.19, 1.67, 1.81, '2026-03-03 13:00:08', 3.10, 0.00, 3.10, 0.00, 0.00, 0.00, '2026-03-03 13:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(45, 1, 1, NULL, NULL, 6, 45, 49.83, 12.54, 31.62, 2.19, 1.67, 1.81, '2026-03-03 13:00:08', 46, 53.35, 12.54, 35.14, 2.19, 1.67, 1.81, '2026-03-03 14:00:08', 3.52, 0.00, 3.52, 0.00, 0.00, 0.00, '2026-03-03 14:00:08', '2026-03-29 15:28:13', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(46, 1, 1, NULL, NULL, 6, 46, 53.35, 12.54, 35.14, 2.19, 1.67, 1.81, '2026-03-03 14:00:08', 47, 56.07, 12.54, 37.86, 2.19, 1.67, 1.81, '2026-03-03 15:00:08', 2.72, 0.00, 2.72, 0.00, 0.00, 0.00, '2026-03-03 15:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(47, 1, 1, NULL, NULL, 6, 47, 56.07, 12.54, 37.86, 2.19, 1.67, 1.81, '2026-03-03 15:00:08', 48, 59.72, 12.54, 41.51, 2.19, 1.67, 1.81, '2026-03-03 16:00:08', 3.65, 0.00, 3.65, 0.00, 0.00, 0.00, '2026-03-03 16:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(48, 1, 1, NULL, NULL, 6, 48, 59.72, 12.54, 41.51, 2.19, 1.67, 1.81, '2026-03-03 16:00:08', 49, 62.57, 12.54, 44.36, 2.19, 1.67, 1.81, '2026-03-03 17:00:08', 2.85, 0.00, 2.85, 0.00, 0.00, 0.00, '2026-03-03 17:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(49, 1, 1, NULL, NULL, 6, 49, 62.57, 12.54, 44.36, 2.19, 1.67, 1.81, '2026-03-03 17:00:08', 50, 62.73, 12.54, 44.36, 2.35, 1.67, 1.81, '2026-03-03 18:00:08', 0.16, 0.00, 0.00, 0.16, 0.00, 0.00, '2026-03-03 18:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(50, 1, 1, NULL, NULL, 6, 50, 62.73, 12.54, 44.36, 2.35, 1.67, 1.81, '2026-03-03 18:00:08', 51, 62.90, 12.54, 44.36, 2.52, 1.67, 1.81, '2026-03-03 19:00:08', 0.17, 0.00, 0.00, 0.17, 0.00, 0.00, '2026-03-03 19:00:08', '2026-03-29 15:28:14', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(51, 1, 1, NULL, NULL, 6, 51, 62.90, 12.54, 44.36, 2.52, 1.67, 1.81, '2026-03-03 19:00:08', 52, 63.06, 12.54, 44.36, 2.68, 1.67, 1.81, '2026-03-03 20:00:08', 0.16, 0.00, 0.00, 0.16, 0.00, 0.00, '2026-03-03 20:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(52, 1, 1, NULL, NULL, 6, 52, 63.06, 12.54, 44.36, 2.68, 1.67, 1.81, '2026-03-03 20:00:08', 53, 63.23, 12.54, 44.36, 2.85, 1.67, 1.81, '2026-03-03 21:00:08', 0.17, 0.00, 0.00, 0.17, 0.00, 0.00, '2026-03-03 21:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(53, 1, 1, NULL, NULL, 6, 53, 63.23, 12.54, 44.36, 2.85, 1.67, 1.81, '2026-03-03 21:00:08', 54, 63.37, 12.54, 44.36, 2.85, 1.81, 1.81, '2026-03-03 22:00:08', 0.14, 0.00, 0.00, 0.00, 0.14, 0.00, '2026-03-03 22:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(54, 1, 1, NULL, NULL, 6, 54, 63.37, 12.54, 44.36, 2.85, 1.81, 1.81, '2026-03-03 22:00:08', 55, 63.52, 12.54, 44.36, 2.85, 1.96, 1.81, '2026-03-03 23:00:08', 0.15, 0.00, 0.00, 0.00, 0.15, 0.00, '2026-03-03 23:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(55, 1, 1, NULL, NULL, 6, 55, 63.52, 12.54, 44.36, 2.85, 1.96, 1.81, '2026-03-03 23:00:08', 56, 63.67, 12.54, 44.36, 2.85, 1.96, 1.96, '2026-03-04 00:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-04 00:00:08', '2026-03-29 15:28:15', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(56, 1, 1, NULL, NULL, 6, 56, 63.67, 12.54, 44.36, 2.85, 1.96, 1.96, '2026-03-04 00:00:08', 57, 63.83, 12.54, 44.36, 2.85, 1.96, 2.12, '2026-03-04 01:00:08', 0.16, 0.00, 0.00, 0.00, 0.00, 0.16, '2026-03-04 01:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(57, 1, 1, NULL, NULL, 6, 57, 63.83, 12.54, 44.36, 2.85, 1.96, 2.12, '2026-03-04 01:00:08', 58, 63.97, 12.54, 44.36, 2.85, 1.96, 2.26, '2026-03-04 02:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-04 02:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(58, 1, 1, NULL, NULL, 6, 58, 63.97, 12.54, 44.36, 2.85, 1.96, 2.26, '2026-03-04 02:00:08', 59, 64.12, 12.54, 44.36, 2.85, 1.96, 2.41, '2026-03-04 03:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-04 03:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(59, 1, 1, NULL, NULL, 6, 59, 64.12, 12.54, 44.36, 2.85, 1.96, 2.41, '2026-03-04 03:00:08', 60, 64.26, 12.54, 44.36, 2.85, 1.96, 2.55, '2026-03-04 04:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-04 04:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(60, 1, 1, NULL, NULL, 6, 60, 64.26, 12.54, 44.36, 2.85, 1.96, 2.55, '2026-03-04 04:00:08', 61, 64.41, 12.54, 44.36, 2.85, 1.96, 2.70, '2026-03-04 05:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-04 05:00:08', '2026-03-29 15:28:16', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(61, 1, 1, NULL, NULL, 6, 61, 64.41, 12.54, 44.36, 2.85, 1.96, 2.70, '2026-03-04 05:00:08', 62, 64.58, 12.54, 44.36, 2.85, 2.13, 2.70, '2026-03-04 06:00:08', 0.17, 0.00, 0.00, 0.00, 0.17, 0.00, '2026-03-04 06:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(62, 1, 1, NULL, NULL, 6, 62, 64.58, 12.54, 44.36, 2.85, 2.13, 2.70, '2026-03-04 06:00:08', 63, 64.72, 12.54, 44.36, 2.85, 2.27, 2.70, '2026-03-04 07:00:08', 0.14, 0.00, 0.00, 0.00, 0.14, 0.00, '2026-03-04 07:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(63, 1, 1, NULL, NULL, 6, 63, 64.72, 12.54, 44.36, 2.85, 2.27, 2.70, '2026-03-04 07:00:08', 64, 64.85, 12.54, 44.49, 2.85, 2.27, 2.70, '2026-03-04 08:00:08', 0.13, 0.00, 0.13, 0.00, 0.00, 0.00, '2026-03-04 08:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(64, 1, 1, NULL, NULL, 6, 64, 64.85, 12.54, 44.49, 2.85, 2.27, 2.70, '2026-03-04 08:00:08', 65, 67.86, 12.54, 47.50, 2.85, 2.27, 2.70, '2026-03-04 09:00:08', 3.01, 0.00, 3.01, 0.00, 0.00, 0.00, '2026-03-04 09:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(65, 1, 1, NULL, NULL, 6, 65, 67.86, 12.54, 47.50, 2.85, 2.27, 2.70, '2026-03-04 09:00:08', 66, 71.28, 12.54, 50.92, 2.85, 2.27, 2.70, '2026-03-04 10:00:08', 3.42, 0.00, 3.42, 0.00, 0.00, 0.00, '2026-03-04 10:00:08', '2026-03-29 15:28:17', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(66, 1, 1, NULL, NULL, 6, 66, 71.28, 12.54, 50.92, 2.85, 2.27, 2.70, '2026-03-04 10:00:08', 67, 74.90, 16.16, 50.92, 2.85, 2.27, 2.70, '2026-03-04 11:00:08', 3.62, 3.62, 0.00, 0.00, 0.00, 0.00, '2026-03-04 11:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(67, 1, 1, NULL, NULL, 6, 67, 74.90, 16.16, 50.92, 2.85, 2.27, 2.70, '2026-03-04 11:00:08', 68, 78.42, 19.68, 50.92, 2.85, 2.27, 2.70, '2026-03-04 12:00:08', 3.52, 3.52, 0.00, 0.00, 0.00, 0.00, '2026-03-04 12:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(68, 1, 1, NULL, NULL, 6, 68, 78.42, 19.68, 50.92, 2.85, 2.27, 2.70, '2026-03-04 12:00:08', 69, 81.14, 19.68, 53.64, 2.85, 2.27, 2.70, '2026-03-04 13:00:08', 2.72, 0.00, 2.72, 0.00, 0.00, 0.00, '2026-03-04 13:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(69, 1, 1, NULL, NULL, 6, 69, 81.14, 19.68, 53.64, 2.85, 2.27, 2.70, '2026-03-04 13:00:08', 70, 84.28, 19.68, 56.78, 2.85, 2.27, 2.70, '2026-03-04 14:00:08', 3.14, 0.00, 3.14, 0.00, 0.00, 0.00, '2026-03-04 14:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(70, 1, 1, NULL, NULL, 6, 70, 84.28, 19.68, 56.78, 2.85, 2.27, 2.70, '2026-03-04 14:00:08', 71, 87.61, 19.68, 60.11, 2.85, 2.27, 2.70, '2026-03-04 15:00:08', 3.33, 0.00, 3.33, 0.00, 0.00, 0.00, '2026-03-04 15:00:08', '2026-03-29 15:28:18', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(71, 1, 1, NULL, NULL, 6, 71, 87.61, 19.68, 60.11, 2.85, 2.27, 2.70, '2026-03-04 15:00:08', 72, 90.87, 19.68, 63.37, 2.85, 2.27, 2.70, '2026-03-04 16:00:08', 3.26, 0.00, 3.26, 0.00, 0.00, 0.00, '2026-03-04 16:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(72, 1, 1, NULL, NULL, 6, 72, 90.87, 19.68, 63.37, 2.85, 2.27, 2.70, '2026-03-04 16:00:08', 73, 94.33, 19.68, 66.83, 2.85, 2.27, 2.70, '2026-03-04 17:00:08', 3.46, 0.00, 3.46, 0.00, 0.00, 0.00, '2026-03-04 17:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(73, 1, 1, NULL, NULL, 6, 73, 94.33, 19.68, 66.83, 2.85, 2.27, 2.70, '2026-03-04 17:00:08', 74, 94.47, 19.68, 66.83, 2.99, 2.27, 2.70, '2026-03-04 18:00:08', 0.14, 0.00, 0.00, 0.14, 0.00, 0.00, '2026-03-04 18:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(74, 1, 1, NULL, NULL, 6, 74, 94.47, 19.68, 66.83, 2.99, 2.27, 2.70, '2026-03-04 18:00:08', 75, 94.62, 19.68, 66.83, 3.14, 2.27, 2.70, '2026-03-04 19:00:08', 0.15, 0.00, 0.00, 0.15, 0.00, 0.00, '2026-03-04 19:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(75, 1, 1, NULL, NULL, 6, 75, 94.62, 19.68, 66.83, 3.14, 2.27, 2.70, '2026-03-04 19:00:08', 76, 94.76, 19.68, 66.83, 3.28, 2.27, 2.70, '2026-03-04 20:00:08', 0.14, 0.00, 0.00, 0.14, 0.00, 0.00, '2026-03-04 20:00:08', '2026-03-29 15:28:19', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(76, 1, 1, NULL, NULL, 6, 76, 94.76, 19.68, 66.83, 3.28, 2.27, 2.70, '2026-03-04 20:00:08', 77, 94.91, 19.68, 66.83, 3.43, 2.27, 2.70, '2026-03-04 21:00:08', 0.15, 0.00, 0.00, 0.15, 0.00, 0.00, '2026-03-04 21:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(77, 1, 1, NULL, NULL, 6, 77, 94.91, 19.68, 66.83, 3.43, 2.27, 2.70, '2026-03-04 21:00:08', 78, 95.08, 19.68, 66.83, 3.43, 2.44, 2.70, '2026-03-04 22:00:08', 0.17, 0.00, 0.00, 0.00, 0.17, 0.00, '2026-03-04 22:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(78, 1, 1, NULL, NULL, 6, 78, 95.08, 19.68, 66.83, 3.43, 2.44, 2.70, '2026-03-04 22:00:08', 79, 95.21, 19.68, 66.83, 3.43, 2.57, 2.70, '2026-03-04 23:00:08', 0.13, 0.00, 0.00, 0.00, 0.13, 0.00, '2026-03-04 23:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(79, 1, 1, NULL, NULL, 6, 79, 95.21, 19.68, 66.83, 3.43, 2.57, 2.70, '2026-03-04 23:00:08', 80, 95.35, 19.68, 66.83, 3.43, 2.57, 2.84, '2026-03-05 00:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-05 00:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(80, 1, 1, NULL, NULL, 6, 80, 95.35, 19.68, 66.83, 3.43, 2.57, 2.84, '2026-03-05 00:00:08', 81, 95.50, 19.68, 66.83, 3.43, 2.57, 2.99, '2026-03-05 01:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-05 01:00:08', '2026-03-29 15:28:20', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(81, 1, 1, NULL, NULL, 6, 81, 95.50, 19.68, 66.83, 3.43, 2.57, 2.99, '2026-03-05 01:00:08', 82, 95.67, 19.68, 66.83, 3.43, 2.57, 3.16, '2026-03-05 02:00:08', 0.17, 0.00, 0.00, 0.00, 0.00, 0.17, '2026-03-05 02:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(82, 1, 1, NULL, NULL, 6, 82, 95.67, 19.68, 66.83, 3.43, 2.57, 3.16, '2026-03-05 02:00:08', 83, 95.80, 19.68, 66.83, 3.43, 2.57, 3.29, '2026-03-05 03:00:08', 0.13, 0.00, 0.00, 0.00, 0.00, 0.13, '2026-03-05 03:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(83, 1, 1, NULL, NULL, 6, 83, 95.80, 19.68, 66.83, 3.43, 2.57, 3.29, '2026-03-05 03:00:08', 84, 95.97, 19.68, 66.83, 3.43, 2.57, 3.46, '2026-03-05 04:00:08', 0.17, 0.00, 0.00, 0.00, 0.00, 0.17, '2026-03-05 04:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(84, 1, 1, NULL, NULL, 6, 84, 95.97, 19.68, 66.83, 3.43, 2.57, 3.46, '2026-03-05 04:00:08', 85, 96.10, 19.68, 66.83, 3.43, 2.57, 3.59, '2026-03-05 05:00:08', 0.13, 0.00, 0.00, 0.00, 0.00, 0.13, '2026-03-05 05:00:08', '2026-03-29 15:28:21', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(85, 1, 1, NULL, NULL, 6, 85, 96.10, 19.68, 66.83, 3.43, 2.57, 3.59, '2026-03-05 05:00:08', 86, 96.25, 19.68, 66.83, 3.43, 2.72, 3.59, '2026-03-05 06:00:08', 0.15, 0.00, 0.00, 0.00, 0.15, 0.00, '2026-03-05 06:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(86, 1, 1, NULL, NULL, 6, 86, 96.25, 19.68, 66.83, 3.43, 2.72, 3.59, '2026-03-05 06:00:08', 87, 96.41, 19.68, 66.83, 3.43, 2.88, 3.59, '2026-03-05 07:00:08', 0.16, 0.00, 0.00, 0.00, 0.16, 0.00, '2026-03-05 07:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(87, 1, 1, NULL, NULL, 6, 87, 96.41, 19.68, 66.83, 3.43, 2.88, 3.59, '2026-03-05 07:00:08', 88, 96.57, 19.68, 66.99, 3.43, 2.88, 3.59, '2026-03-05 08:00:08', 0.16, 0.00, 0.16, 0.00, 0.00, 0.00, '2026-03-05 08:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(88, 1, 1, NULL, NULL, 6, 88, 96.57, 19.68, 66.99, 3.43, 2.88, 3.59, '2026-03-05 08:00:08', 89, 100.19, 19.68, 70.61, 3.43, 2.88, 3.59, '2026-03-05 09:00:08', 3.62, 0.00, 3.62, 0.00, 0.00, 0.00, '2026-03-05 09:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(89, 1, 1, NULL, NULL, 6, 89, 100.19, 19.68, 70.61, 3.43, 2.88, 3.59, '2026-03-05 09:00:08', 90, 103.23, 19.68, 73.65, 3.43, 2.88, 3.59, '2026-03-05 10:00:08', 3.04, 0.00, 3.04, 0.00, 0.00, 0.00, '2026-03-05 10:00:08', '2026-03-29 15:28:22', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(90, 1, 1, NULL, NULL, 6, 90, 103.23, 19.68, 73.65, 3.43, 2.88, 3.59, '2026-03-05 10:00:08', 91, 106.46, 22.91, 73.65, 3.43, 2.88, 3.59, '2026-03-05 11:00:08', 3.23, 3.23, 0.00, 0.00, 0.00, 0.00, '2026-03-05 11:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(91, 1, 1, NULL, NULL, 6, 91, 106.46, 22.91, 73.65, 3.43, 2.88, 3.59, '2026-03-05 11:00:08', 92, 109.60, 26.05, 73.65, 3.43, 2.88, 3.59, '2026-03-05 12:00:08', 3.14, 3.14, 0.00, 0.00, 0.00, 0.00, '2026-03-05 12:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(92, 1, 1, NULL, NULL, 6, 92, 109.60, 26.05, 73.65, 3.43, 2.88, 3.59, '2026-03-05 12:00:08', 93, 112.93, 26.05, 76.98, 3.43, 2.88, 3.59, '2026-03-05 13:00:08', 3.33, 0.00, 3.33, 0.00, 0.00, 0.00, '2026-03-05 13:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(93, 1, 1, NULL, NULL, 6, 93, 112.93, 26.05, 76.98, 3.43, 2.88, 3.59, '2026-03-05 13:00:08', 94, 115.68, 26.05, 79.73, 3.43, 2.88, 3.59, '2026-03-05 14:00:08', 2.75, 0.00, 2.75, 0.00, 0.00, 0.00, '2026-03-05 14:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(94, 1, 1, NULL, NULL, 6, 94, 115.68, 26.05, 79.73, 3.43, 2.88, 3.59, '2026-03-05 14:00:08', 95, 118.62, 26.05, 82.67, 3.43, 2.88, 3.59, '2026-03-05 15:00:08', 2.94, 0.00, 2.94, 0.00, 0.00, 0.00, '2026-03-05 15:00:08', '2026-03-29 15:28:23', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(95, 1, 1, NULL, NULL, 6, 95, 118.62, 26.05, 82.67, 3.43, 2.88, 3.59, '2026-03-05 15:00:08', 96, 121.50, 26.05, 85.55, 3.43, 2.88, 3.59, '2026-03-05 16:00:08', 2.88, 0.00, 2.88, 0.00, 0.00, 0.00, '2026-03-05 16:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(96, 1, 1, NULL, NULL, 6, 96, 121.50, 26.05, 85.55, 3.43, 2.88, 3.59, '2026-03-05 16:00:08', 97, 124.57, 26.05, 88.62, 3.43, 2.88, 3.59, '2026-03-05 17:00:08', 3.07, 0.00, 3.07, 0.00, 0.00, 0.00, '2026-03-05 17:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(97, 1, 1, NULL, NULL, 6, 97, 124.57, 26.05, 88.62, 3.43, 2.88, 3.59, '2026-03-05 17:00:08', 98, 124.74, 26.05, 88.62, 3.60, 2.88, 3.59, '2026-03-05 18:00:08', 0.17, 0.00, 0.00, 0.17, 0.00, 0.00, '2026-03-05 18:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(98, 1, 1, NULL, NULL, 6, 98, 124.74, 26.05, 88.62, 3.60, 2.88, 3.59, '2026-03-05 18:00:08', 99, 124.87, 26.05, 88.62, 3.73, 2.88, 3.59, '2026-03-05 19:00:08', 0.13, 0.00, 0.00, 0.13, 0.00, 0.00, '2026-03-05 19:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(99, 1, 1, NULL, NULL, 6, 99, 124.87, 26.05, 88.62, 3.73, 2.88, 3.59, '2026-03-05 19:00:08', 100, 125.04, 26.05, 88.62, 3.90, 2.88, 3.59, '2026-03-05 20:00:08', 0.17, 0.00, 0.00, 0.17, 0.00, 0.00, '2026-03-05 20:00:08', '2026-03-29 15:28:24', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(100, 1, 1, NULL, NULL, 6, 100, 125.04, 26.05, 88.62, 3.90, 2.88, 3.59, '2026-03-05 20:00:08', 101, 125.17, 26.05, 88.62, 4.03, 2.88, 3.59, '2026-03-05 21:00:08', 0.13, 0.00, 0.00, 0.13, 0.00, 0.00, '2026-03-05 21:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(101, 1, 1, NULL, NULL, 6, 101, 125.17, 26.05, 88.62, 4.03, 2.88, 3.59, '2026-03-05 21:00:08', 102, 125.32, 26.05, 88.62, 4.03, 3.03, 3.59, '2026-03-05 22:00:08', 0.15, 0.00, 0.00, 0.00, 0.15, 0.00, '2026-03-05 22:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(102, 1, 1, NULL, NULL, 6, 102, 125.32, 26.05, 88.62, 4.03, 3.03, 3.59, '2026-03-05 22:00:08', 103, 125.48, 26.05, 88.62, 4.03, 3.19, 3.59, '2026-03-05 23:00:08', 0.16, 0.00, 0.00, 0.00, 0.16, 0.00, '2026-03-05 23:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(103, 1, 1, NULL, NULL, 6, 103, 125.48, 26.05, 88.62, 4.03, 3.19, 3.59, '2026-03-05 23:00:08', 104, 125.65, 26.05, 88.62, 4.03, 3.19, 3.76, '2026-03-06 00:00:08', 0.17, 0.00, 0.00, 0.00, 0.00, 0.17, '2026-03-06 00:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(104, 1, 1, NULL, NULL, 6, 104, 125.65, 26.05, 88.62, 4.03, 3.19, 3.76, '2026-03-06 00:00:08', 105, 125.78, 26.05, 88.62, 4.03, 3.19, 3.89, '2026-03-06 01:00:08', 0.13, 0.00, 0.00, 0.00, 0.00, 0.13, '2026-03-06 01:00:08', '2026-03-29 15:28:25', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(105, 1, 1, NULL, NULL, 6, 105, 125.78, 26.05, 88.62, 4.03, 3.19, 3.89, '2026-03-06 01:00:08', 106, 125.93, 26.05, 88.62, 4.03, 3.19, 4.04, '2026-03-06 02:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-06 02:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(106, 1, 1, NULL, NULL, 6, 106, 125.93, 26.05, 88.62, 4.03, 3.19, 4.04, '2026-03-06 02:00:08', 107, 126.09, 26.05, 88.62, 4.03, 3.19, 4.20, '2026-03-06 03:00:08', 0.16, 0.00, 0.00, 0.00, 0.00, 0.16, '2026-03-06 03:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(107, 1, 1, NULL, NULL, 6, 107, 126.09, 26.05, 88.62, 4.03, 3.19, 4.20, '2026-03-06 03:00:08', 108, 126.24, 26.05, 88.62, 4.03, 3.19, 4.35, '2026-03-06 04:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-06 04:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(108, 1, 1, NULL, NULL, 6, 108, 126.24, 26.05, 88.62, 4.03, 3.19, 4.35, '2026-03-06 04:00:08', 109, 126.40, 26.05, 88.62, 4.03, 3.19, 4.51, '2026-03-06 05:00:08', 0.16, 0.00, 0.00, 0.00, 0.00, 0.16, '2026-03-06 05:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(109, 1, 1, NULL, NULL, 6, 109, 126.40, 26.05, 88.62, 4.03, 3.19, 4.51, '2026-03-06 05:00:08', 110, 126.54, 26.05, 88.62, 4.03, 3.33, 4.51, '2026-03-06 06:00:08', 0.14, 0.00, 0.00, 0.00, 0.14, 0.00, '2026-03-06 06:00:08', '2026-03-29 15:28:26', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(110, 1, 1, NULL, NULL, 6, 110, 126.54, 26.05, 88.62, 4.03, 3.33, 4.51, '2026-03-06 06:00:08', 111, 126.69, 26.05, 88.62, 4.03, 3.48, 4.51, '2026-03-06 07:00:08', 0.15, 0.00, 0.00, 0.00, 0.15, 0.00, '2026-03-06 07:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(111, 1, 1, NULL, NULL, 6, 111, 126.69, 26.05, 88.62, 4.03, 3.48, 4.51, '2026-03-06 07:00:08', 112, 126.83, 26.05, 88.76, 4.03, 3.48, 4.51, '2026-03-06 08:00:08', 0.14, 0.00, 0.14, 0.00, 0.00, 0.00, '2026-03-06 08:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(112, 1, 1, NULL, NULL, 6, 112, 126.83, 26.05, 88.76, 4.03, 3.48, 4.51, '2026-03-06 08:00:08', 113, 130.09, 26.05, 92.02, 4.03, 3.48, 4.51, '2026-03-06 09:00:08', 3.26, 0.00, 3.26, 0.00, 0.00, 0.00, '2026-03-06 09:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(113, 1, 1, NULL, NULL, 6, 113, 130.09, 26.05, 92.02, 4.03, 3.48, 4.51, '2026-03-06 09:00:08', 114, 133.77, 26.05, 95.70, 4.03, 3.48, 4.51, '2026-03-06 10:00:08', 3.68, 0.00, 3.68, 0.00, 0.00, 0.00, '2026-03-06 10:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(114, 1, 1, NULL, NULL, 6, 114, 133.77, 26.05, 95.70, 4.03, 3.48, 4.51, '2026-03-06 10:00:08', 115, 136.65, 28.93, 95.70, 4.03, 3.48, 4.51, '2026-03-06 11:00:08', 2.88, 2.88, 0.00, 0.00, 0.00, 0.00, '2026-03-06 11:00:08', '2026-03-29 15:28:27', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(115, 1, 1, NULL, NULL, 6, 115, 136.65, 28.93, 95.70, 4.03, 3.48, 4.51, '2026-03-06 11:00:08', 116, 139.43, 31.71, 95.70, 4.03, 3.48, 4.51, '2026-03-06 12:00:08', 2.78, 2.78, 0.00, 0.00, 0.00, 0.00, '2026-03-06 12:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(116, 1, 1, NULL, NULL, 6, 116, 139.43, 31.71, 95.70, 4.03, 3.48, 4.51, '2026-03-06 12:00:08', 117, 142.41, 31.71, 98.68, 4.03, 3.48, 4.51, '2026-03-06 13:00:08', 2.98, 0.00, 2.98, 0.00, 0.00, 0.00, '2026-03-06 13:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(117, 1, 1, NULL, NULL, 6, 117, 142.41, 31.71, 98.68, 4.03, 3.48, 4.51, '2026-03-06 13:00:08', 118, 145.80, 31.71, 102.07, 4.03, 3.48, 4.51, '2026-03-06 14:00:08', 3.39, 0.00, 3.39, 0.00, 0.00, 0.00, '2026-03-06 14:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(118, 1, 1, NULL, NULL, 6, 118, 145.80, 31.71, 102.07, 4.03, 3.48, 4.51, '2026-03-06 14:00:08', 119, 149.38, 31.71, 105.65, 4.03, 3.48, 4.51, '2026-03-06 15:00:08', 3.58, 0.00, 3.58, 0.00, 0.00, 0.00, '2026-03-06 15:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(119, 1, 1, NULL, NULL, 6, 119, 149.38, 31.71, 105.65, 4.03, 3.48, 4.51, '2026-03-06 15:00:08', 120, 152.90, 31.71, 109.17, 4.03, 3.48, 4.51, '2026-03-06 16:00:08', 3.52, 0.00, 3.52, 0.00, 0.00, 0.00, '2026-03-06 16:00:08', '2026-03-29 15:28:28', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(120, 1, 1, NULL, NULL, 6, 120, 152.90, 31.71, 109.17, 4.03, 3.48, 4.51, '2026-03-06 16:00:08', 121, 155.62, 31.71, 111.89, 4.03, 3.48, 4.51, '2026-03-06 17:00:08', 2.72, 0.00, 2.72, 0.00, 0.00, 0.00, '2026-03-06 17:00:08', '2026-03-29 15:28:29', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(121, 1, 1, NULL, NULL, 6, 121, 155.62, 31.71, 111.89, 4.03, 3.48, 4.51, '2026-03-06 17:00:08', 122, 155.77, 31.71, 111.89, 4.18, 3.48, 4.51, '2026-03-06 18:00:08', 0.15, 0.00, 0.00, 0.15, 0.00, 0.00, '2026-03-06 18:00:08', '2026-03-29 15:28:29', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(122, 1, 1, NULL, NULL, 6, 122, 155.77, 31.71, 111.89, 4.18, 3.48, 4.51, '2026-03-06 18:00:08', 123, 155.93, 31.71, 111.89, 4.34, 3.48, 4.51, '2026-03-06 19:00:08', 0.16, 0.00, 0.00, 0.16, 0.00, 0.00, '2026-03-06 19:00:08', '2026-03-29 15:28:29', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(123, 1, 1, NULL, NULL, 6, 123, 155.93, 31.71, 111.89, 4.34, 3.48, 4.51, '2026-03-06 19:00:08', 124, 156.08, 31.71, 111.89, 4.49, 3.48, 4.51, '2026-03-06 20:00:08', 0.15, 0.00, 0.00, 0.15, 0.00, 0.00, '2026-03-06 20:00:08', '2026-03-29 15:28:30', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(124, 1, 1, NULL, NULL, 6, 124, 156.08, 31.71, 111.89, 4.49, 3.48, 4.51, '2026-03-06 20:00:08', 125, 156.38, 31.71, 111.89, 4.65, 3.62, 4.51, '2026-03-06 22:00:08', 0.30, 0.00, 0.00, 0.16, 0.14, 0.00, '2026-03-06 22:00:08', '2026-03-29 15:28:30', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(125, 1, 1, NULL, NULL, 6, 125, 156.38, 31.71, 111.89, 4.65, 3.62, 4.51, '2026-03-06 22:00:08', 126, 157.35, 31.71, 111.89, 4.65, 3.77, 5.33, '2026-03-07 01:00:08', 0.97, 0.00, 0.00, 0.00, 0.15, 0.82, '2026-03-07 01:00:08', '2026-03-29 15:28:30', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(126, 1, 1, NULL, NULL, 6, 126, 157.35, 31.71, 111.89, 4.65, 3.77, 5.33, '2026-03-07 01:00:08', 129, 158.07, 31.71, 111.89, 4.65, 3.77, 6.05, '2026-03-07 03:00:08', 0.72, 0.00, 0.00, 0.00, 0.00, 0.72, '2026-03-07 03:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(127, 1, 1, NULL, NULL, 6, 129, 158.07, 31.71, 111.89, 4.65, 3.77, 6.05, '2026-03-07 03:00:08', 131, 158.43, 31.71, 111.89, 4.65, 3.77, 6.41, '2026-03-07 04:00:08', 0.36, 0.00, 0.00, 0.00, 0.00, 0.36, '2026-03-07 04:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(128, 1, 1, NULL, NULL, 6, 131, 158.43, 31.71, 111.89, 4.65, 3.77, 6.41, '2026-03-07 04:00:08', 132, 159.25, 31.71, 111.89, 4.65, 4.21, 6.79, '2026-03-07 06:00:08', 0.82, 0.00, 0.00, 0.00, 0.44, 0.38, '2026-03-07 06:00:08', '2026-03-29 15:28:31', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(129, 1, 1, NULL, NULL, 6, 132, 159.25, 31.71, 111.89, 4.65, 4.21, 6.79, '2026-03-07 06:00:08', 133, 159.59, 31.71, 111.89, 4.65, 4.55, 6.79, '2026-03-07 07:00:08', 0.34, 0.00, 0.00, 0.00, 0.34, 0.00, '2026-03-07 07:00:08', '2026-03-29 15:28:32', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(130, 1, 1, NULL, NULL, 6, 133, 159.59, 31.71, 111.89, 4.65, 4.55, 6.79, '2026-03-07 07:00:08', 134, 160.41, 31.71, 112.71, 4.65, 4.55, 6.79, '2026-03-07 09:00:08', 0.82, 0.00, 0.82, 0.00, 0.00, 0.00, '2026-03-07 09:00:08', '2026-03-29 15:28:32', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(131, 1, 1, NULL, NULL, 6, 134, 160.41, 31.71, 112.71, 4.65, 4.55, 6.79, '2026-03-07 09:00:08', 135, 160.82, 31.71, 113.12, 4.65, 4.55, 6.79, '2026-03-07 10:00:08', 0.41, 0.00, 0.41, 0.00, 0.00, 0.00, '2026-03-07 10:00:08', '2026-03-29 15:28:32', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(132, 1, 1, NULL, NULL, 6, 135, 160.82, 31.71, 113.12, 4.65, 4.55, 6.79, '2026-03-07 10:00:08', 136, 161.68, 32.57, 113.12, 4.65, 4.55, 6.79, '2026-03-07 12:00:08', 0.86, 0.86, 0.00, 0.00, 0.00, 0.00, '2026-03-07 12:00:08', '2026-03-29 15:28:33', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(133, 1, 1, NULL, NULL, 6, 136, 161.68, 32.57, 113.12, 4.65, 4.55, 6.79, '2026-03-07 12:00:08', 137, 162.13, 32.57, 113.57, 4.65, 4.55, 6.79, '2026-03-07 13:00:08', 0.45, 0.00, 0.45, 0.00, 0.00, 0.00, '2026-03-07 13:00:08', '2026-03-29 15:28:33', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(134, 1, 1, NULL, NULL, 6, 137, 162.13, 32.57, 113.57, 4.65, 4.55, 6.79, '2026-03-07 13:00:08', 138, 162.91, 32.57, 114.35, 4.65, 4.55, 6.79, '2026-03-07 15:00:08', 0.78, 0.00, 0.78, 0.00, 0.00, 0.00, '2026-03-07 15:00:08', '2026-03-29 15:28:33', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(135, 1, 1, NULL, NULL, 6, 138, 162.91, 32.57, 114.35, 4.65, 4.55, 6.79, '2026-03-07 15:00:08', 139, 163.30, 32.57, 114.74, 4.65, 4.55, 6.79, '2026-03-07 16:00:08', 0.39, 0.00, 0.39, 0.00, 0.00, 0.00, '2026-03-07 16:00:08', '2026-03-29 15:28:33', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(136, 1, 1, NULL, NULL, 6, 139, 163.30, 32.57, 114.74, 4.65, 4.55, 6.79, '2026-03-07 16:00:08', 140, 164.07, 32.57, 115.16, 5.00, 4.55, 6.79, '2026-03-07 18:00:08', 0.77, 0.00, 0.42, 0.35, 0.00, 0.00, '2026-03-07 18:00:08', '2026-03-29 15:28:34', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(137, 1, 1, NULL, NULL, 6, 140, 164.07, 32.57, 115.16, 5.00, 4.55, 6.79, '2026-03-07 18:00:08', 141, 164.45, 32.57, 115.16, 5.38, 4.55, 6.79, '2026-03-07 19:00:08', 0.38, 0.00, 0.00, 0.38, 0.00, 0.00, '2026-03-07 19:00:08', '2026-03-29 15:28:34', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(138, 1, 1, NULL, NULL, 6, 141, 164.45, 32.57, 115.16, 5.38, 4.55, 6.79, '2026-03-07 19:00:08', 142, 165.20, 32.57, 115.16, 6.13, 4.55, 6.79, '2026-03-07 21:00:08', 0.75, 0.00, 0.00, 0.75, 0.00, 0.00, '2026-03-07 21:00:08', '2026-03-29 15:28:34', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(139, 1, 1, NULL, NULL, 6, 142, 165.20, 32.57, 115.16, 6.13, 4.55, 6.79, '2026-03-07 21:00:08', 143, 165.64, 32.57, 115.16, 6.13, 4.99, 6.79, '2026-03-07 22:00:08', 0.44, 0.00, 0.00, 0.00, 0.44, 0.00, '2026-03-07 22:00:08', '2026-03-29 15:28:35', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(140, 1, 1, NULL, NULL, 6, 143, 165.64, 32.57, 115.16, 6.13, 4.99, 6.79, '2026-03-07 22:00:08', 144, 166.33, 32.57, 115.16, 6.13, 5.33, 7.14, '2026-03-08 00:00:08', 0.69, 0.00, 0.00, 0.00, 0.34, 0.35, '2026-03-08 00:00:08', '2026-03-29 15:28:35', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(141, 1, 1, NULL, NULL, 6, 144, 166.33, 32.57, 115.16, 6.13, 5.33, 7.14, '2026-03-08 00:00:08', 145, 166.70, 32.57, 115.16, 6.13, 5.33, 7.51, '2026-03-08 01:00:08', 0.37, 0.00, 0.00, 0.00, 0.00, 0.37, '2026-03-08 01:00:08', '2026-03-29 15:28:35', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(142, 1, 1, NULL, NULL, 6, 145, 166.70, 32.57, 115.16, 6.13, 5.33, 7.51, '2026-03-08 01:00:08', 146, 167.57, 32.57, 115.16, 6.13, 5.33, 8.38, '2026-03-08 03:00:08', 0.87, 0.00, 0.00, 0.00, 0.00, 0.87, '2026-03-08 03:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(143, 1, 1, NULL, NULL, 6, 146, 167.57, 32.57, 115.16, 6.13, 5.33, 8.38, '2026-03-08 03:00:08', 147, 168.01, 32.57, 115.16, 6.13, 5.33, 8.82, '2026-03-08 04:00:08', 0.44, 0.00, 0.00, 0.00, 0.00, 0.44, '2026-03-08 04:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(144, 1, 1, NULL, NULL, 6, 147, 168.01, 32.57, 115.16, 6.13, 5.33, 8.82, '2026-03-08 04:00:08', 157, 168.47, 32.57, 115.16, 6.13, 5.33, 9.28, '2026-03-08 05:00:08', 0.46, 0.00, 0.00, 0.00, 0.00, 0.46, '2026-03-08 05:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(145, 1, 1, NULL, NULL, 6, 157, 168.47, 32.57, 115.16, 6.13, 5.33, 9.28, '2026-03-08 05:00:08', 158, 168.87, 32.57, 115.16, 6.13, 5.73, 9.28, '2026-03-08 06:00:08', 0.40, 0.00, 0.00, 0.00, 0.40, 0.00, '2026-03-08 06:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(146, 1, 1, NULL, NULL, 6, 158, 168.87, 32.57, 115.16, 6.13, 5.73, 9.28, '2026-03-08 06:00:08', 159, 169.29, 32.57, 115.16, 6.13, 6.15, 9.28, '2026-03-08 07:00:08', 0.42, 0.00, 0.00, 0.00, 0.42, 0.00, '2026-03-08 07:00:08', '2026-03-29 15:28:36', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(147, 1, 1, NULL, NULL, 6, 159, 169.29, 32.57, 115.16, 6.13, 6.15, 9.28, '2026-03-08 07:00:08', 160, 169.70, 32.57, 115.57, 6.13, 6.15, 9.28, '2026-03-08 08:00:08', 0.41, 0.00, 0.41, 0.00, 0.00, 0.00, '2026-03-08 08:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(148, 1, 1, NULL, NULL, 6, 160, 169.70, 32.57, 115.57, 6.13, 6.15, 9.28, '2026-03-08 08:00:08', 161, 170.14, 32.57, 116.01, 6.13, 6.15, 9.28, '2026-03-08 09:00:08', 0.44, 0.00, 0.44, 0.00, 0.00, 0.00, '2026-03-08 09:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(149, 1, 1, NULL, NULL, 6, 161, 170.14, 32.57, 116.01, 6.13, 6.15, 9.28, '2026-03-08 09:00:08', 162, 170.50, 32.57, 116.37, 6.13, 6.15, 9.28, '2026-03-08 10:00:08', 0.36, 0.00, 0.36, 0.00, 0.00, 0.00, '2026-03-08 10:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(150, 1, 1, NULL, NULL, 6, 162, 170.50, 32.57, 116.37, 6.13, 6.15, 9.28, '2026-03-08 10:00:08', 163, 170.89, 32.96, 116.37, 6.13, 6.15, 9.28, '2026-03-08 11:00:08', 0.39, 0.39, 0.00, 0.00, 0.00, 0.00, '2026-03-08 11:00:08', '2026-03-29 15:28:37', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(151, 1, 1, NULL, NULL, 6, 163, 170.89, 32.96, 116.37, 6.13, 6.15, 9.28, '2026-03-08 11:00:08', 164, 171.27, 33.34, 116.37, 6.13, 6.15, 9.28, '2026-03-08 12:00:08', 0.38, 0.38, 0.00, 0.00, 0.00, 0.00, '2026-03-08 12:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(152, 1, 1, NULL, NULL, 6, 164, 171.27, 33.34, 116.37, 6.13, 6.15, 9.28, '2026-03-08 12:00:08', 165, 171.67, 33.34, 116.77, 6.13, 6.15, 9.28, '2026-03-08 13:00:08', 0.40, 0.00, 0.40, 0.00, 0.00, 0.00, '2026-03-08 13:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(153, 1, 1, NULL, NULL, 6, 165, 171.67, 33.34, 116.77, 6.13, 6.15, 9.28, '2026-03-08 13:00:08', 166, 172.12, 33.34, 117.22, 6.13, 6.15, 9.28, '2026-03-08 14:00:08', 0.45, 0.00, 0.45, 0.00, 0.00, 0.00, '2026-03-08 14:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(154, 1, 1, NULL, NULL, 6, 166, 172.12, 33.34, 117.22, 6.13, 6.15, 9.28, '2026-03-08 14:00:08', 167, 172.47, 33.34, 117.57, 6.13, 6.15, 9.28, '2026-03-08 15:00:08', 0.35, 0.00, 0.35, 0.00, 0.00, 0.00, '2026-03-08 15:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(155, 1, 1, NULL, NULL, 6, 167, 172.47, 33.34, 117.57, 6.13, 6.15, 9.28, '2026-03-08 15:00:08', 168, 172.81, 33.34, 117.91, 6.13, 6.15, 9.28, '2026-03-08 16:00:08', 0.34, 0.00, 0.34, 0.00, 0.00, 0.00, '2026-03-08 16:00:08', '2026-03-29 15:28:38', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(156, 1, 1, NULL, NULL, 6, 168, 172.81, 33.34, 117.91, 6.13, 6.15, 9.28, '2026-03-08 16:00:08', 169, 173.18, 33.34, 118.28, 6.13, 6.15, 9.28, '2026-03-08 17:00:08', 0.37, 0.00, 0.37, 0.00, 0.00, 0.00, '2026-03-08 17:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(157, 1, 1, NULL, NULL, 6, 169, 173.18, 33.34, 118.28, 6.13, 6.15, 9.28, '2026-03-08 17:00:08', 170, 173.61, 33.34, 118.28, 6.56, 6.15, 9.28, '2026-03-08 18:00:08', 0.43, 0.00, 0.00, 0.43, 0.00, 0.00, '2026-03-08 18:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(158, 1, 1, NULL, NULL, 6, 170, 173.61, 33.34, 118.28, 6.56, 6.15, 9.28, '2026-03-08 18:00:08', 171, 174.06, 33.34, 118.28, 7.01, 6.15, 9.28, '2026-03-08 19:00:08', 0.45, 0.00, 0.00, 0.45, 0.00, 0.00, '2026-03-08 19:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(159, 1, 1, NULL, NULL, 6, 171, 174.06, 33.34, 118.28, 7.01, 6.15, 9.28, '2026-03-08 19:00:08', 172, 174.50, 33.34, 118.28, 7.45, 6.15, 9.28, '2026-03-08 20:00:08', 0.44, 0.00, 0.00, 0.44, 0.00, 0.00, '2026-03-08 20:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(160, 1, 1, NULL, NULL, 6, 172, 174.50, 33.34, 118.28, 7.45, 6.15, 9.28, '2026-03-08 20:00:08', 173, 174.84, 33.34, 118.28, 7.79, 6.15, 9.28, '2026-03-08 21:00:08', 0.34, 0.00, 0.00, 0.34, 0.00, 0.00, '2026-03-08 21:00:08', '2026-03-29 15:28:39', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(161, 1, 1, NULL, NULL, 6, 173, 174.84, 33.34, 118.28, 7.79, 6.15, 9.28, '2026-03-08 21:00:08', 174, 175.23, 33.34, 118.28, 7.79, 6.54, 9.28, '2026-03-08 22:00:08', 0.39, 0.00, 0.00, 0.00, 0.39, 0.00, '2026-03-08 22:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(162, 1, 1, NULL, NULL, 6, 174, 175.23, 33.34, 118.28, 7.79, 6.54, 9.28, '2026-03-08 22:00:08', 175, 175.65, 33.34, 118.28, 7.79, 6.96, 9.28, '2026-03-08 23:00:08', 0.42, 0.00, 0.00, 0.00, 0.42, 0.00, '2026-03-08 23:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(163, 1, 1, NULL, NULL, 6, 175, 175.65, 33.34, 118.28, 7.79, 6.96, 9.28, '2026-03-08 23:00:08', 176, 175.81, 33.34, 118.28, 7.79, 6.96, 9.44, '2026-03-09 00:00:08', 0.16, 0.00, 0.00, 0.00, 0.00, 0.16, '2026-03-09 00:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(164, 1, 1, NULL, NULL, 6, 176, 175.81, 33.34, 118.28, 7.79, 6.96, 9.44, '2026-03-09 00:00:08', 177, 175.98, 33.34, 118.28, 7.79, 6.96, 9.61, '2026-03-09 01:00:08', 0.17, 0.00, 0.00, 0.00, 0.00, 0.17, '2026-03-09 01:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(165, 1, 1, NULL, NULL, 6, 177, 175.98, 33.34, 118.28, 7.79, 6.96, 9.61, '2026-03-09 01:00:08', 178, 176.12, 33.34, 118.28, 7.79, 6.96, 9.75, '2026-03-09 02:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-09 02:00:08', '2026-03-29 15:28:40', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(166, 1, 1, NULL, NULL, 6, 178, 176.12, 33.34, 118.28, 7.79, 6.96, 9.75, '2026-03-09 02:00:08', 179, 176.27, 33.34, 118.28, 7.79, 6.96, 9.90, '2026-03-09 03:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-09 03:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(167, 1, 1, NULL, NULL, 6, 179, 176.27, 33.34, 118.28, 7.79, 6.96, 9.90, '2026-03-09 03:00:08', 180, 176.42, 33.34, 118.28, 7.79, 6.96, 10.05, '2026-03-09 04:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-09 04:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(168, 1, 1, NULL, NULL, 6, 180, 176.42, 33.34, 118.28, 7.79, 6.96, 10.05, '2026-03-09 04:00:08', 181, 176.57, 33.34, 118.28, 7.79, 6.96, 10.20, '2026-03-09 05:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-09 05:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(169, 1, 1, NULL, NULL, 6, 181, 176.57, 33.34, 118.28, 7.79, 6.96, 10.20, '2026-03-09 05:00:08', 182, 176.70, 33.34, 118.28, 7.79, 7.09, 10.20, '2026-03-09 06:00:08', 0.13, 0.00, 0.00, 0.00, 0.13, 0.00, '2026-03-09 06:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(170, 1, 1, NULL, NULL, 6, 182, 176.70, 33.34, 118.28, 7.79, 7.09, 10.20, '2026-03-09 06:00:08', 183, 176.84, 33.34, 118.28, 7.79, 7.23, 10.20, '2026-03-09 07:00:08', 0.14, 0.00, 0.00, 0.00, 0.14, 0.00, '2026-03-09 07:00:08', '2026-03-29 15:28:41', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(171, 1, 1, NULL, NULL, 6, 183, 176.84, 33.34, 118.28, 7.79, 7.23, 10.20, '2026-03-09 07:00:08', 184, 176.98, 33.34, 118.42, 7.79, 7.23, 10.20, '2026-03-09 08:00:08', 0.14, 0.00, 0.14, 0.00, 0.00, 0.00, '2026-03-09 08:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(172, 1, 1, NULL, NULL, 6, 184, 176.98, 33.34, 118.42, 7.79, 7.23, 10.20, '2026-03-09 08:00:08', 185, 180.08, 33.34, 121.52, 7.79, 7.23, 10.20, '2026-03-09 09:00:08', 3.10, 0.00, 3.10, 0.00, 0.00, 0.00, '2026-03-09 09:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(173, 1, 1, NULL, NULL, 6, 185, 180.08, 33.34, 121.52, 7.79, 7.23, 10.20, '2026-03-09 09:00:08', 186, 183.60, 33.34, 125.04, 7.79, 7.23, 10.20, '2026-03-09 10:00:08', 3.52, 0.00, 3.52, 0.00, 0.00, 0.00, '2026-03-09 10:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(174, 1, 1, NULL, NULL, 6, 186, 183.60, 33.34, 125.04, 7.79, 7.23, 10.20, '2026-03-09 10:00:08', 187, 186.32, 36.06, 125.04, 7.79, 7.23, 10.20, '2026-03-09 11:00:08', 2.72, 2.72, 0.00, 0.00, 0.00, 0.00, '2026-03-09 11:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(175, 1, 1, NULL, NULL, 6, 187, 186.32, 36.06, 125.04, 7.79, 7.23, 10.20, '2026-03-09 11:00:08', 188, 189.94, 39.68, 125.04, 7.79, 7.23, 10.20, '2026-03-09 12:00:08', 3.62, 3.62, 0.00, 0.00, 0.00, 0.00, '2026-03-09 12:00:08', '2026-03-29 15:28:42', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(176, 1, 1, NULL, NULL, 6, 188, 189.94, 39.68, 125.04, 7.79, 7.23, 10.20, '2026-03-09 12:00:08', 189, 192.76, 39.68, 127.86, 7.79, 7.23, 10.20, '2026-03-09 13:00:08', 2.82, 0.00, 2.82, 0.00, 0.00, 0.00, '2026-03-09 13:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(177, 1, 1, NULL, NULL, 6, 189, 192.76, 39.68, 127.86, 7.79, 7.23, 10.20, '2026-03-09 13:00:08', 190, 195.99, 39.68, 131.09, 7.79, 7.23, 10.20, '2026-03-09 14:00:08', 3.23, 0.00, 3.23, 0.00, 0.00, 0.00, '2026-03-09 14:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(178, 1, 1, NULL, NULL, 6, 190, 195.99, 39.68, 131.09, 7.79, 7.23, 10.20, '2026-03-09 14:00:08', 191, 199.41, 39.68, 134.51, 7.79, 7.23, 10.20, '2026-03-09 15:00:08', 3.42, 0.00, 3.42, 0.00, 0.00, 0.00, '2026-03-09 15:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(179, 1, 1, NULL, NULL, 6, 191, 199.41, 39.68, 134.51, 7.79, 7.23, 10.20, '2026-03-09 15:00:08', 192, 202.77, 39.68, 137.87, 7.79, 7.23, 10.20, '2026-03-09 16:00:08', 3.36, 0.00, 3.36, 0.00, 0.00, 0.00, '2026-03-09 16:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(180, 1, 1, NULL, NULL, 6, 192, 202.77, 39.68, 137.87, 7.79, 7.23, 10.20, '2026-03-09 16:00:08', 193, 206.32, 39.68, 141.42, 7.79, 7.23, 10.20, '2026-03-09 17:00:08', 3.55, 0.00, 3.55, 0.00, 0.00, 0.00, '2026-03-09 17:00:08', '2026-03-29 15:28:43', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(181, 1, 1, NULL, NULL, 6, 193, 206.32, 39.68, 141.42, 7.79, 7.23, 10.20, '2026-03-09 17:00:08', 194, 206.46, 39.68, 141.42, 7.93, 7.23, 10.20, '2026-03-09 18:00:08', 0.14, 0.00, 0.00, 0.14, 0.00, 0.00, '2026-03-09 18:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(182, 1, 1, NULL, NULL, 6, 194, 206.46, 39.68, 141.42, 7.93, 7.23, 10.20, '2026-03-09 18:00:08', 195, 206.61, 39.68, 141.42, 8.08, 7.23, 10.20, '2026-03-09 19:00:08', 0.15, 0.00, 0.00, 0.15, 0.00, 0.00, '2026-03-09 19:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(183, 1, 1, NULL, NULL, 6, 195, 206.61, 39.68, 141.42, 8.08, 7.23, 10.20, '2026-03-09 19:00:08', 196, 206.76, 39.68, 141.42, 8.23, 7.23, 10.20, '2026-03-09 20:00:08', 0.15, 0.00, 0.00, 0.15, 0.00, 0.00, '2026-03-09 20:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(184, 1, 1, NULL, NULL, 6, 196, 206.76, 39.68, 141.42, 8.23, 7.23, 10.20, '2026-03-09 20:00:08', 197, 206.92, 39.68, 141.42, 8.39, 7.23, 10.20, '2026-03-09 21:00:08', 0.16, 0.00, 0.00, 0.16, 0.00, 0.00, '2026-03-09 21:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(185, 1, 1, NULL, NULL, 6, 197, 206.92, 39.68, 141.42, 8.39, 7.23, 10.20, '2026-03-09 21:00:08', 198, 207.05, 39.68, 141.42, 8.39, 7.36, 10.20, '2026-03-09 22:00:08', 0.13, 0.00, 0.00, 0.00, 0.13, 0.00, '2026-03-09 22:00:08', '2026-03-29 15:28:44', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(186, 1, 1, NULL, NULL, 6, 198, 207.05, 39.68, 141.42, 8.39, 7.36, 10.20, '2026-03-09 22:00:08', 199, 207.19, 39.68, 141.42, 8.39, 7.50, 10.20, '2026-03-09 23:00:08', 0.14, 0.00, 0.00, 0.00, 0.14, 0.00, '2026-03-09 23:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(187, 1, 1, NULL, NULL, 6, 199, 207.19, 39.68, 141.42, 8.39, 7.50, 10.20, '2026-03-09 23:00:08', 200, 207.33, 39.68, 141.42, 8.39, 7.50, 10.34, '2026-03-10 00:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-10 00:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(188, 1, 1, NULL, NULL, 6, 200, 207.33, 39.68, 141.42, 8.39, 7.50, 10.34, '2026-03-10 00:00:08', 201, 207.48, 39.68, 141.42, 8.39, 7.50, 10.49, '2026-03-10 01:00:08', 0.15, 0.00, 0.00, 0.00, 0.00, 0.15, '2026-03-10 01:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(189, 1, 1, NULL, NULL, 6, 201, 207.48, 39.68, 141.42, 8.39, 7.50, 10.49, '2026-03-10 01:00:08', 202, 207.65, 39.68, 141.42, 8.39, 7.50, 10.66, '2026-03-10 02:00:08', 0.17, 0.00, 0.00, 0.00, 0.00, 0.17, '2026-03-10 02:00:08', '2026-03-29 15:28:45', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(190, 1, 1, NULL, NULL, 6, 202, 207.65, 39.68, 141.42, 8.39, 7.50, 10.66, '2026-03-10 02:00:08', 203, 207.78, 39.68, 141.42, 8.39, 7.50, 10.79, '2026-03-10 03:00:08', 0.13, 0.00, 0.00, 0.00, 0.00, 0.13, '2026-03-10 03:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(191, 1, 1, NULL, NULL, 6, 203, 207.78, 39.68, 141.42, 8.39, 7.50, 10.79, '2026-03-10 03:00:08', 204, 207.91, 39.68, 141.42, 8.39, 7.50, 10.92, '2026-03-10 04:00:08', 0.13, 0.00, 0.00, 0.00, 0.00, 0.13, '2026-03-10 04:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(192, 1, 1, NULL, NULL, 6, 204, 207.91, 39.68, 141.42, 8.39, 7.50, 10.92, '2026-03-10 04:00:08', 205, 208.05, 39.68, 141.42, 8.39, 7.50, 11.06, '2026-03-10 05:00:08', 0.14, 0.00, 0.00, 0.00, 0.00, 0.14, '2026-03-10 05:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(193, 1, 1, NULL, NULL, 6, 205, 208.05, 39.68, 141.42, 8.39, 7.50, 11.06, '2026-03-10 05:00:08', 206, 208.21, 39.68, 141.42, 8.39, 7.66, 11.06, '2026-03-10 06:00:08', 0.16, 0.00, 0.00, 0.00, 0.16, 0.00, '2026-03-10 06:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(194, 1, 1, NULL, NULL, 6, 206, 208.21, 39.68, 141.42, 8.39, 7.66, 11.06, '2026-03-10 06:00:08', 207, 208.38, 39.68, 141.42, 8.39, 7.83, 11.06, '2026-03-10 07:00:08', 0.17, 0.00, 0.00, 0.00, 0.17, 0.00, '2026-03-10 07:00:08', '2026-03-29 15:28:46', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(195, 1, 1, NULL, NULL, 6, 207, 208.38, 39.68, 141.42, 8.39, 7.83, 11.06, '2026-03-10 07:00:08', 208, 208.55, 39.68, 141.59, 8.39, 7.83, 11.06, '2026-03-10 08:00:08', 0.17, 0.00, 0.17, 0.00, 0.00, 0.00, '2026-03-10 08:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(196, 1, 1, NULL, NULL, 6, 208, 208.55, 39.68, 141.59, 8.39, 7.83, 11.06, '2026-03-10 08:00:08', 209, 211.30, 39.68, 144.34, 8.39, 7.83, 11.06, '2026-03-10 09:00:08', 2.75, 0.00, 2.75, 0.00, 0.00, 0.00, '2026-03-10 09:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(197, 1, 1, NULL, NULL, 6, 209, 211.30, 39.68, 144.34, 8.39, 7.83, 11.06, '2026-03-10 09:00:08', 210, 214.47, 39.68, 147.51, 8.39, 7.83, 11.06, '2026-03-10 10:00:08', 3.17, 0.00, 3.17, 0.00, 0.00, 0.00, '2026-03-10 10:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(198, 1, 1, NULL, NULL, 6, 210, 214.47, 39.68, 147.51, 8.39, 7.83, 11.06, '2026-03-10 10:00:08', 211, 217.83, 43.04, 147.51, 8.39, 7.83, 11.06, '2026-03-10 11:00:08', 3.36, 3.36, 0.00, 0.00, 0.00, 0.00, '2026-03-10 11:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(199, 1, 1, NULL, NULL, 6, 211, 217.83, 43.04, 147.51, 8.39, 7.83, 11.06, '2026-03-10 11:00:08', 212, 221.09, 46.30, 147.51, 8.39, 7.83, 11.06, '2026-03-10 12:00:08', 3.26, 3.26, 0.00, 0.00, 0.00, 0.00, '2026-03-10 12:00:08', '2026-03-29 15:28:47', 0);
INSERT INTO energy_electric_meter_power_consume_record
(id, meter_id, is_calculate, calculate_type, account_id, space_id, begin_record_id, begin_power, begin_power_higher, begin_power_high, begin_power_low, begin_power_lower, begin_power_deep_low, begin_record_time, end_record_id, end_power, end_power_higher, end_power_high, end_power_low, end_power_lower, end_power_deep_low, end_record_time, consume_power, consume_power_higher, consume_power_high, consume_power_low, consume_power_lower, consume_power_deep_low, meter_consume_time, create_time, is_deleted)
VALUES(200, 1, 1, NULL, NULL, 6, 212, 221.09, 46.30, 147.51, 8.39, 7.83, 11.06, '2026-03-10 12:00:08', 213, 224.55, 46.30, 150.97, 8.39, 7.83, 11.06, '2026-03-10 13:00:08', 3.46, 0.00, 3.46, 0.00, 0.00, 0.00, '2026-03-10 13:00:08', '2026-03-29 15:28:48', 0);