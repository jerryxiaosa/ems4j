SET NAMES utf8mb4;

-- 菜单
truncate table sys_menu;

INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(11, '账户管理', 'account_management', 0, 1, '', 1, 1, 'accounts', '', 0, 0, 'account_management', 1, '管理员', '2026-03-12 14:46:19', 1, '管理员', '2026-03-12 15:26:03');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(12, '账户信息', 'account_management_account_info', 11, 1, '/accounts/info', 1, 1, '', '', 0, 0, 'account_management_account_info', 1, '管理员', '2026-03-12 14:46:19', 1, '管理员', '2026-04-13 14:56:01');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(13, '销户记录', 'account_management_cancel_records', 11, 2, '/accounts/cancel-records', 1, 1, '', '', 0, 0, 'account_management_cancel_records', 1, '管理员', '2026-03-12 14:46:19', 1, '管理员', '2026-04-13 15:18:45');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(14, '设备管理', 'device_management', 0, 2, '', 1, 1, 'devices', '', 0, 0, 'device_management', 1, '管理员', '2026-03-12 14:46:19', 1, '管理员', '2026-03-12 15:26:03');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(15, '智能电表', 'device_management_electric_meter', 14, 1, '/devices/electric-meters', 1, 1, '', '', 0, 0, 'device_management_electric_meter', 1, '管理员', '2026-03-12 14:46:19', 1, '管理员', '2026-04-13 15:31:46');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(16, '智能网关', 'device_management_gateway', 14, 2, '/devices/gateways', 1, 1, '', '', 0, 0, 'device_management_gateway', 1, '管理员', '2026-03-12 14:46:19', 1, '管理员', '2026-04-13 15:50:51');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(17, '设备品类', 'device_management_category', 14, 3, '/devices/categories', 1, 1, '', '', 0, 0, 'device_management_category', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:04:38');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(18, '设备操作', 'device_management_command_record', 14, 4, '/device-operations', 1, 1, '', '', 0, 0, 'device_management_command_record', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:14:10');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(19, '方案管理', 'plan_management', 0, 3, '', 1, 1, 'plans', '', 0, 0, 'plan_management', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-03-12 15:26:05');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(20, '电价方案', 'plan_management_electric_price_plan', 19, 1, '/plans/electric', 1, 1, '', '', 1, 0, 'plan_management_electric_price_plan', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:18:52');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(21, '预警方案', 'plan_management_warn_plan', 19, 2, '/plans/warn', 1, 1, '', '', 0, 0, 'plan_management_warn_plan', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:31:50');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(22, '交易管理', 'trade_management', 0, 4, '', 1, 1, 'trade', '', 0, 0, 'trade_management', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-03-12 15:26:06');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(23, '电费充值', 'trade_management_electric_recharge', 22, 1, '/trade/recharge', 1, 1, '', '', 0, 0, 'trade_management_electric_recharge', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:43:48');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(24, '订单流水', 'trade_management_order_flow', 22, 2, '/trade/order-flows', 1, 1, '', '', 0, 0, 'trade_management_order_flow', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:48:58');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(25, '消费记录', 'trade_management_consumption_record', 22, 3, '/trade/consumption-records', 1, 1, '', '', 0, 0, 'trade_management_consumption_record', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:51:19');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(26, '报表统计', 'report_statistics', 0, 5, '', 1, 1, 'reports', '', 0, 0, 'report_statistics', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-03-12 15:26:06');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(27, '电费报表', 'report_statistics_electric_bill_report', 26, 1, '/reports/electric-bill', 1, 1, '', '', 0, 0, 'report_statistics_electric_bill_report', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:53:13');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(29, '系统管理', 'system_management', 0, 6, '', 1, 1, 'system', '', 0, 0, 'system_management', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-03-12 23:48:45');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(30, '用户管理', 'system_management_user_management', 29, 1, '/system/users', 1, 1, '', '', 0, 0, 'system_management_user_management', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:59:17');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(31, '角色管理', 'system_management_role_management', 29, 2, '/system/roles', 1, 1, '', '', 0, 0, 'system_management_role_management', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 17:04:56');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(32, '菜单管理', 'system_management_menu_management', 29, 3, '/system/menus', 1, 1, '', '', 0, 0, 'system_management_menu_management', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 17:08:37');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(33, '空间管理', 'system_management_space_management', 29, 4, '/system/spaces', 1, 1, '', '', 0, 0, 'system_management_space_management', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 17:11:43');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(34, '机构管理', 'system_management_organization_management', 29, 5, '/system/organizations', 1, 1, '', '', 0, 0, 'system_management_organization_management', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 17:15:32');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(35, '开户', 'account_management_account_info_open', 12, 1, '', 1, 2, '', '', 0, 0, 'account_management_account_info_open', 1, '管理员', '2026-03-12 15:26:03', 1, '管理员', '2026-04-13 15:07:11');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(36, '详情', 'account_management_account_info_detail', 12, 2, '', 1, 2, '', '', 0, 0, 'account_management_account_info_detail', 1, '管理员', '2026-03-12 15:26:03', 1, '管理员', '2026-04-13 15:10:36');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(37, '销户', 'account_management_account_info_cancel', 12, 3, '', 1, 2, '', '', 0, 0, 'account_management_account_info_cancel', 1, '管理员', '2026-03-12 15:26:03', 1, '管理员', '2026-04-13 15:14:16');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(38, '离线销户', 'account_management_account_info_offline_cancel', 12, 4, '', 1, 2, '', '', 0, 0, 'account_management_account_info_offline_cancel', 1, '管理员', '2026-03-12 15:26:03', 1, '管理员', '2026-04-13 15:17:55');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(39, '详情', 'account_management_cancel_records_detail', 13, 1, '', 1, 2, '', '', 0, 0, 'account_management_cancel_records_detail', 1, '管理员', '2026-03-12 15:26:03', 1, '管理员', '2026-04-13 15:19:07');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(40, '添加', 'device_management_electric_meter_create', 15, 1, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_create', 1, '管理员', '2026-03-12 15:26:03', 1, '管理员', '2026-04-13 15:35:06');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(41, '详情', 'device_management_electric_meter_detail', 15, 2, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_detail', 1, '管理员', '2026-03-12 15:26:03', 1, '管理员', '2026-04-13 15:36:45');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(42, '编辑', 'device_management_electric_meter_edit', 15, 3, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_edit', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:38:03');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(43, '删除', 'device_management_electric_meter_delete', 15, 4, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_delete', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:38:55');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(44, '合闸', 'device_management_electric_meter_switch_on', 15, 5, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_switch_on', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:40:36');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(45, '断闸', 'device_management_electric_meter_switch_off', 15, 6, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_switch_off', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:40:58');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(46, '设置CT', 'device_management_electric_meter_set_ct', 15, 7, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_set_ct', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:42:12');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(47, '保电', 'device_management_electric_meter_enable_protection', 15, 8, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_enable_protection', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:43:38');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(48, '取消保电', 'device_management_electric_meter_disable_protection', 15, 9, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_disable_protection', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:44:57');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(49, '批量合闸', 'device_management_electric_meter_batch_switch_on', 15, 10, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_batch_switch_on', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:47:58');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(50, '批量断闸', 'device_management_electric_meter_batch_switch_off', 15, 11, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_batch_switch_off', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:48:21');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(51, '批量保电', 'device_management_electric_meter_batch_enable_protection', 15, 12, '', 1, 2, '', '', 0, 0, 'device_management_electric_meter_batch_enable_protection', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:48:51');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(52, '批量取消保电', 'device_management_electric_meter_batch_disable_protection', 15, 13, '', 1, 2, '', '', 1, 0, 'device_management_electric_meter_batch_disable_protection', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:49:35');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(53, '添加', 'device_management_gateway_create', 16, 1, '', 1, 2, '', '', 0, 0, 'device_management_gateway_create', 1, '管理员', '2026-03-12 15:26:04', 1, '管理员', '2026-04-13 15:52:35');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(54, '详情', 'device_management_gateway_detail', 16, 2, '', 1, 2, '', '', 0, 0, 'device_management_gateway_detail', 1, '管理员', '2026-03-12 15:26:05', 1, '管理员', '2026-04-13 15:53:13');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(55, '编辑', 'device_management_gateway_edit', 16, 3, '', 1, 2, '', '', 0, 0, 'device_management_gateway_edit', 1, '管理员', '2026-03-12 15:26:05', 1, '管理员', '2026-04-13 15:53:58');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(56, '删除', 'device_management_gateway_delete', 16, 4, '', 1, 2, '', '', 0, 0, 'device_management_gateway_delete', 1, '管理员', '2026-03-12 15:26:05', 1, '管理员', '2026-04-13 15:54:59');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(57, '添加', 'plan_management_electric_price_plan_create', 20, 1, '', 1, 2, '', '', 0, 0, 'plan_management_electric_price_plan_create', 1, '管理员', '2026-03-12 15:26:05', 1, '管理员', '2026-04-13 16:20:23');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(58, '详情', 'plan_management_electric_price_plan_detail', 20, 2, '', 1, 2, '', '', 0, 0, 'plan_management_electric_price_plan_detail', 1, '管理员', '2026-03-12 15:26:05', 1, '管理员', '2026-04-13 16:21:02');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(59, '编辑', 'plan_management_electric_price_plan_edit', 20, 3, '', 1, 2, '', '', 0, 0, 'plan_management_electric_price_plan_edit', 1, '管理员', '2026-03-12 15:26:05', 1, '管理员', '2026-04-13 16:21:55');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(60, '删除', 'plan_management_electric_price_plan_delete', 20, 4, '', 1, 2, '', '', 0, 0, 'plan_management_electric_price_plan_delete', 1, '管理员', '2026-03-12 15:26:05', 1, '管理员', '2026-04-13 16:23:08');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(61, '标准电价查看', 'plan_management_electric_price_plan_standard_price_detail', 20, 5, '', 1, 2, '', '', 0, 0, 'plan_management_electric_price_plan_standard_price_detail', 1, '管理员', '2026-03-12 15:26:05', 1, '管理员', '2026-04-13 16:26:32');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(62, '标准电价设置', 'plan_management_electric_price_plan_standard_price_setting', 20, 6, '', 1, 2, '', '', 0, 0, 'plan_management_electric_price_plan_standard_price_setting', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:27:08');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(63, '尖峰平谷时间查看', 'plan_management_electric_price_plan_time_period_detail', 20, 7, '', 1, 2, '', '', 0, 0, 'plan_management_electric_price_plan_time_period_detail', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:27:32');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(64, '尖峰平谷时间设置', 'plan_management_electric_price_plan_time_period_setting', 20, 8, '', 1, 2, '', '', 0, 0, 'plan_management_electric_price_plan_time_period_setting', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:28:01');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(65, '添加', 'plan_management_warn_plan_create', 21, 1, '', 1, 2, '', '', 0, 0, 'plan_management_warn_plan_create', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:32:38');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(66, '详情', 'plan_management_warn_plan_detail', 21, 2, '', 1, 2, '', '', 0, 0, 'plan_management_warn_plan_detail', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:33:08');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(67, '编辑', 'plan_management_warn_plan_edit', 21, 3, '', 1, 2, '', '', 0, 0, 'plan_management_warn_plan_edit', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:34:00');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(68, '删除', 'plan_management_warn_plan_delete', 21, 4, '', 1, 2, '', '', 0, 0, 'plan_management_warn_plan_delete', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:35:02');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(70, '电费服务费设置', 'trade_management_electric_recharge_service_fee_setting', 23, 2, '', 1, 2, '', '', 0, 0, 'trade_management_electric_recharge_service_fee_setting', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:48:18');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(72, '详情', 'trade_management_order_flow_detail', 24, 1, '', 1, 2, '', '', 0, 0, 'trade_management_order_flow_detail', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:50:21');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(73, '详情', 'trade_management_consumption_record_detail', 25, 1, '', 1, 2, '', '', 0, 0, 'trade_management_consumption_record_detail', 1, '管理员', '2026-03-12 15:26:06', 1, '管理员', '2026-04-13 16:52:08');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(74, '新增', 'system_management_user_management_create', 30, 1, '', 1, 2, '', '', 0, 0, 'system_management_user_management_create', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:00:56');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(75, '详情', 'system_management_user_management_detail', 30, 2, '', 1, 2, '', '', 0, 0, 'system_management_user_management_detail', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:01:43');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(76, '编辑', 'system_management_user_management_edit', 30, 3, '', 1, 2, '', '', 0, 0, 'system_management_user_management_edit', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:02:20');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(77, '删除', 'system_management_user_management_delete', 30, 4, '', 1, 2, '', '', 0, 0, 'system_management_user_management_delete', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:02:46');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(78, '重置密码', 'system_management_user_management_reset_password', 30, 5, '', 1, 2, '', '', 0, 0, 'system_management_user_management_reset_password', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:03:48');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(79, '添加', 'system_management_role_management_create', 31, 1, '', 1, 2, '', '', 0, 0, 'system_management_role_management_create', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:05:40');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(80, '编辑', 'system_management_role_management_edit', 31, 2, '', 1, 2, '', '', 0, 0, 'system_management_role_management_edit', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:06:19');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(81, '删除', 'system_management_role_management_delete', 31, 3, '', 1, 2, '', '', 0, 0, 'system_management_role_management_delete', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:07:08');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(82, '分配权限', 'system_management_role_management_assign_permissions', 31, 4, '', 1, 2, '', '', 0, 0, 'system_management_role_management_assign_permissions', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:07:48');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(83, '添加下级', 'system_management_menu_management_add_child', 32, 1, '', 1, 2, '', '', 0, 0, 'system_management_menu_management_add_child', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:09:34');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(84, '编辑', 'system_management_menu_management_edit', 32, 2, '', 1, 2, '', '', 0, 0, 'system_management_menu_management_edit', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:10:39');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(85, '删除', 'system_management_menu_management_delete', 32, 3, '', 1, 2, '', '', 0, 0, 'system_management_menu_management_delete', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:11:10');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(86, '添加子级', 'system_management_space_management_add_child', 33, 1, '', 1, 2, '', '', 0, 0, 'system_management_space_management_add_child', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:12:43');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(87, '编辑', 'system_management_space_management_edit', 33, 2, '', 1, 2, '', '', 0, 0, 'system_management_space_management_edit', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:13:15');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(88, '删除', 'system_management_space_management_delete', 33, 3, '', 1, 2, '', '', 0, 0, 'system_management_space_management_delete', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:13:28');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(89, '新增', 'system_management_organization_management_create', 34, 1, '', 1, 2, '', '', 0, 0, 'system_management_organization_management_create', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:15:59');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(90, '详情', 'system_management_organization_management_detail', 34, 2, '', 1, 2, '', '', 0, 0, 'system_management_organization_management_detail', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:16:26');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(91, '修改', 'system_management_organization_management_edit', 34, 3, '', 1, 2, '', '', 0, 0, 'system_management_organization_management_edit', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:16:59');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(92, '删除', 'system_management_organization_management_delete', 34, 4, '', 1, 2, '', '', 0, 0, 'system_management_organization_management_delete', 1, '管理员', '2026-03-12 15:26:07', 1, '管理员', '2026-04-13 17:17:11');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(93, '详情', 'device_management_device_operation_detail', 18, 5, '', 1, 2, '', '', 1, 0, 'device_management_device_operation_detail', 1, '管理员', '2026-04-10 21:06:43', 1, '管理员', '2026-04-10 21:06:43');
INSERT INTO sys_menu
(id, menu_name, menu_key, pid, sort_num, `path`, menu_source, menu_type, icon, remark, is_hidden, is_deleted, active_menu_key, create_user, create_user_name, create_time, update_user, update_user_name, update_time)
VALUES(94, '详情', 'report_statistics_electric_bill_report_detail', 27, 1, '', 1, 2, '', '', 0, 0, 'report_statistics_electric_bill_report_detail', 1, '管理员', '2026-03-12 14:46:20', 1, '管理员', '2026-04-13 16:57:04');


-- 菜单路径
truncate table sys_menu_path;

INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(23, 11, 11, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(24, 12, 12, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(25, 11, 12, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(26, 13, 13, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(27, 11, 13, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(28, 14, 14, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(29, 15, 15, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(30, 14, 15, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(31, 16, 16, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(32, 14, 16, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(33, 17, 17, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(34, 14, 17, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(35, 18, 18, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(36, 14, 18, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(37, 19, 19, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(38, 20, 20, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(39, 19, 20, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(40, 21, 21, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(41, 19, 21, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(42, 22, 22, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(43, 23, 23, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(44, 22, 23, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(45, 24, 24, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(46, 22, 24, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(47, 25, 25, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(48, 22, 25, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(49, 26, 26, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(50, 27, 27, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(51, 26, 27, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(54, 29, 29, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(55, 30, 30, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(56, 29, 30, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(57, 31, 31, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(58, 29, 31, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(59, 32, 32, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(60, 29, 32, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(61, 33, 33, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(62, 29, 33, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(63, 34, 34, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(64, 29, 34, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(65, 35, 35, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(66, 12, 35, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(67, 11, 35, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(69, 36, 36, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(70, 12, 36, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(71, 11, 36, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(73, 37, 37, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(74, 12, 37, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(75, 11, 37, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(77, 38, 38, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(78, 12, 38, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(79, 11, 38, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(81, 39, 39, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(82, 13, 39, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(83, 11, 39, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(85, 40, 40, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(86, 15, 40, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(87, 14, 40, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(89, 41, 41, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(90, 15, 41, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(91, 14, 41, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(93, 42, 42, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(94, 15, 42, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(95, 14, 42, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(97, 43, 43, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(98, 15, 43, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(99, 14, 43, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(101, 44, 44, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(102, 15, 44, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(103, 14, 44, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(105, 45, 45, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(106, 15, 45, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(107, 14, 45, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(109, 46, 46, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(110, 15, 46, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(111, 14, 46, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(113, 47, 47, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(114, 15, 47, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(115, 14, 47, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(117, 48, 48, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(118, 15, 48, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(119, 14, 48, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(121, 49, 49, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(122, 15, 49, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(123, 14, 49, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(125, 50, 50, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(126, 15, 50, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(127, 14, 50, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(129, 51, 51, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(130, 15, 51, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(131, 14, 51, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(133, 52, 52, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(134, 15, 52, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(135, 14, 52, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(137, 53, 53, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(138, 16, 53, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(139, 14, 53, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(141, 54, 54, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(142, 16, 54, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(143, 14, 54, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(145, 55, 55, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(146, 16, 55, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(147, 14, 55, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(149, 56, 56, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(150, 16, 56, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(151, 14, 56, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(153, 57, 57, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(154, 20, 57, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(155, 19, 57, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(157, 58, 58, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(158, 20, 58, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(159, 19, 58, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(161, 59, 59, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(162, 20, 59, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(163, 19, 59, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(165, 60, 60, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(166, 20, 60, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(167, 19, 60, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(169, 61, 61, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(170, 20, 61, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(171, 19, 61, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(173, 62, 62, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(174, 20, 62, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(175, 19, 62, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(177, 63, 63, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(178, 20, 63, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(179, 19, 63, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(181, 64, 64, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(182, 20, 64, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(183, 19, 64, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(185, 65, 65, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(186, 21, 65, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(187, 19, 65, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(189, 66, 66, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(190, 21, 66, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(191, 19, 66, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(193, 67, 67, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(194, 21, 67, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(195, 19, 67, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(197, 68, 68, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(198, 21, 68, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(199, 19, 68, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(201, 69, 69, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(202, 23, 69, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(203, 22, 69, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(205, 70, 70, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(206, 23, 70, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(207, 22, 70, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(209, 71, 71, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(210, 23, 71, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(211, 22, 71, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(213, 72, 72, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(214, 24, 72, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(215, 22, 72, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(217, 73, 73, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(218, 25, 73, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(219, 22, 73, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(221, 74, 74, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(222, 30, 74, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(223, 29, 74, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(225, 75, 75, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(226, 30, 75, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(227, 29, 75, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(229, 76, 76, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(230, 30, 76, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(231, 29, 76, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(233, 77, 77, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(234, 30, 77, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(235, 29, 77, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(237, 78, 78, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(238, 30, 78, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(239, 29, 78, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(241, 79, 79, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(242, 31, 79, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(243, 29, 79, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(245, 80, 80, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(246, 31, 80, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(247, 29, 80, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(249, 81, 81, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(250, 31, 81, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(251, 29, 81, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(253, 82, 82, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(254, 31, 82, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(255, 29, 82, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(257, 83, 83, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(258, 32, 83, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(259, 29, 83, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(261, 84, 84, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(262, 32, 84, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(263, 29, 84, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(265, 85, 85, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(266, 32, 85, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(267, 29, 85, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(269, 86, 86, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(270, 33, 86, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(271, 29, 86, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(273, 87, 87, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(274, 33, 87, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(275, 29, 87, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(277, 88, 88, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(278, 33, 88, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(279, 29, 88, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(281, 89, 89, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(282, 34, 89, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(283, 29, 89, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(285, 90, 90, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(286, 34, 90, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(287, 29, 90, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(289, 91, 91, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(290, 34, 91, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(291, 29, 91, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(293, 92, 92, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(294, 34, 92, 1);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(295, 29, 92, 2);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(296, 93, 93, 0);
INSERT INTO sys_menu_path
(id, ancestor_id, descendant_id, `depth`)
VALUES(297, 14, 93, 1);

--- 菜单权限
truncate table sys_menu_auth;

INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(1, 12, 'accounts:accounts:page', '2026-04-13 14:56:01');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(2, 35, 'organizations:organizations:list', '2026-04-13 15:07:11');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(3, 35, 'owners:meters:candidate:list', '2026-04-13 15:07:11');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(4, 35, 'plans:electric-price:list', '2026-04-13 15:07:11');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(5, 35, 'plans:warn:list', '2026-04-13 15:07:11');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(6, 35, 'accounts:accounts:open', '2026-04-13 15:07:11');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(7, 36, 'accounts:accounts:detail', '2026-04-13 15:10:36');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(8, 37, 'accounts:accounts:detail', '2026-04-13 15:14:16');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(9, 37, 'accounts:accounts:cancel', '2026-04-13 15:14:16');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(10, 37, 'accounts:accounts:page', '2026-04-13 15:14:16');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(12, 38, 'devices:meters:detail', '2026-04-13 15:17:55');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(13, 38, 'accounts:accounts:cancel', '2026-04-13 15:17:55');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(14, 13, 'accounts:cancel:page', '2026-04-13 15:18:45');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(15, 39, 'accounts:cancel:detail', '2026-04-13 15:19:07');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(16, 15, 'devices:meters:page', '2026-04-13 15:31:46');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(17, 40, 'devices:models:list', '2026-04-13 15:35:06');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(18, 40, 'devices:gateways:list', '2026-04-13 15:35:06');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(19, 40, 'spaces:spaces:tree', '2026-04-13 15:35:06');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(20, 40, 'devices:meters:add', '2026-04-13 15:35:06');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(21, 41, 'devices:meters:detail', '2026-04-13 15:36:45');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(22, 42, 'devices:meters:update', '2026-04-13 15:38:03');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(23, 43, 'devices:meters:delete', '2026-04-13 15:38:55');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(24, 44, 'devices:meters:switch', '2026-04-13 15:40:36');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(25, 45, 'devices:meters:switch', '2026-04-13 15:40:58');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(26, 46, 'devices:models:list', '2026-04-13 15:42:12');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(27, 46, 'devices:meters:ct', '2026-04-13 15:42:12');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(28, 47, 'devices:meters:protect', '2026-04-13 15:43:38');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(29, 48, 'devices:meters:protect', '2026-04-13 15:44:57');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(30, 49, 'devices:meters:switch', '2026-04-13 15:47:58');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(31, 50, 'devices:meters:switch', '2026-04-13 15:48:21');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(32, 51, 'devices:meters:protect', '2026-04-13 15:48:51');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(33, 52, 'devices:meters:protect', '2026-04-13 15:49:35');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(34, 16, 'devices:gateways:page', '2026-04-13 15:50:51');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(35, 53, 'devices:models:list', '2026-04-13 15:52:35');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(36, 53, 'spaces:spaces:tree', '2026-04-13 15:52:35');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(37, 53, 'devices:gateways:add', '2026-04-13 15:52:35');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(38, 54, 'devices:gateways:detail', '2026-04-13 15:53:13');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(39, 55, 'devices:gateways:detail', '2026-04-13 15:53:58');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(40, 55, 'devices:gateways:update', '2026-04-13 15:53:58');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(41, 56, 'devices:gateways:delete', '2026-04-13 15:54:59');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(42, 17, 'devices:types:tree', '2026-04-13 16:04:38');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(43, 17, 'devices:models:page', '2026-04-13 16:04:38');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(44, 18, 'devices:operations:detail', '2026-04-13 16:14:10');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(45, 20, 'plans:electric-price:list', '2026-04-13 16:18:52');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(46, 20, 'plans:electric-price:step:get', '2026-04-13 16:18:52');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(47, 20, 'plans:electric-price:time:get', '2026-04-13 16:18:52');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(48, 20, 'plans:electric-price:price:get', '2026-04-13 16:18:52');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(49, 57, 'plans:electric-price:add', '2026-04-13 16:20:23');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(50, 58, 'plans:electric-price:detail', '2026-04-13 16:21:02');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(51, 59, 'plans:electric-price:detail', '2026-04-13 16:21:55');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(52, 59, 'plans:electric-price:update', '2026-04-13 16:21:55');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(53, 60, 'plans:electric-price:delete', '2026-04-13 16:23:08');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(54, 61, 'plans:electric-price:price:get', '2026-04-13 16:26:32');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(55, 62, 'plans:electric-price:price:update', '2026-04-13 16:27:08');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(56, 63, 'plans:electric-price:time:get', '2026-04-13 16:27:32');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(57, 64, 'plans:electric-price:time:update', '2026-04-13 16:28:01');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(58, 21, 'plans:warn:list', '2026-04-13 16:31:50');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(59, 65, 'plans:warn:add', '2026-04-13 16:32:38');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(60, 66, 'plans:warn:detail', '2026-04-13 16:33:08');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(61, 67, 'plans:warn:detail', '2026-04-13 16:34:00');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(62, 67, 'plans:warn:update', '2026-04-13 16:34:00');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(63, 68, 'plans:warn:delete', '2026-04-13 16:35:02');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(64, 23, 'orders:service-rate:get', '2026-04-13 16:43:48');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(65, 23, 'accounts:accounts:page', '2026-04-13 16:43:48');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(66, 23, 'accounts:accounts:detail', '2026-04-13 16:43:48');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(67, 23, 'orders:orders:add-energy', '2026-04-13 16:43:48');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(68, 70, 'orders:service-rate:update', '2026-04-13 16:48:18');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(69, 24, 'orders:orders:list', '2026-04-13 16:48:58');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(70, 72, 'orders:orders:detail', '2026-04-13 16:50:21');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(71, 25, 'finance:meter-billing:page', '2026-04-13 16:51:19');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(72, 25, 'finance:account-consume:page', '2026-04-13 16:51:19');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(73, 73, 'finance:meter-billing:detail', '2026-04-13 16:52:08');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(74, 27, 'reports:electric-bill:page', '2026-04-13 16:53:13');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(75, 94, 'reports:electric-bill:detail', '2026-04-13 16:57:04');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(76, 30, 'organizations:organizations:list', '2026-04-13 16:59:17');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(77, 30, 'users:roles:list', '2026-04-13 16:59:17');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(78, 30, 'users:users:page', '2026-04-13 16:59:17');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(79, 74, 'users:users:add', '2026-04-13 17:00:56');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(80, 75, 'users:users:detail', '2026-04-13 17:01:43');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(81, 76, 'users:users:update', '2026-04-13 17:02:20');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(82, 77, 'users:users:delete', '2026-04-13 17:02:46');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(83, 78, 'users:users:password:reset', '2026-04-13 17:03:48');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(84, 31, 'users:roles:list', '2026-04-13 17:04:56');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(85, 79, 'users:roles:add', '2026-04-13 17:05:40');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(86, 80, 'users:roles:update', '2026-04-13 17:06:19');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(87, 81, 'users:roles:delete', '2026-04-13 17:07:08');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(88, 82, 'users:roles:menu', '2026-04-13 17:07:48');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(90, 32, 'users:menus:tree', '2026-04-13 17:08:37');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(91, 83, 'users:menus:add', '2026-04-13 17:09:34');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(92, 84, 'users:menus:detail', '2026-04-13 17:10:39');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(93, 84, 'users:menus:update', '2026-04-13 17:10:39');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(94, 85, 'users:menus:delete', '2026-04-13 17:11:10');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(95, 33, 'spaces:spaces:tree', '2026-04-13 17:11:43');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(96, 86, 'spaces:spaces:add', '2026-04-13 17:12:43');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(97, 87, 'spaces:spaces:update', '2026-04-13 17:13:15');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(98, 88, 'spaces:spaces:delete', '2026-04-13 17:13:28');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(99, 34, 'organizations:organizations:page', '2026-04-13 17:15:32');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(100, 89, 'organizations:organizations:add', '2026-04-13 17:15:59');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(101, 90, 'organizations:organizations:detail', '2026-04-13 17:16:26');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(102, 91, 'organizations:organizations:update', '2026-04-13 17:16:59');
INSERT INTO sys_menu_auth
(id, menu_id, permission_code, create_time)
VALUES(103, 92, 'organizations:organizations:delete', '2026-04-13 17:17:11');