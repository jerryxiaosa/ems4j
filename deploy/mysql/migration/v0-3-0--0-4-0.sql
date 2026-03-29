-- 单电表用电趋势接口索引变更
-- 用途：
-- 1. 替换按 record_time 的单列索引
-- 2. 为按 meter_id + record_time 的趋势查询提供索引支持

ALTER TABLE energy_electric_meter_power_record
    DROP INDEX idx_record_time,
    ADD INDEX idx_meter_record_time (meter_id, record_time);

ALTER TABLE energy_electric_meter_power_consume_record
    DROP INDEX idx_meter_id,
    DROP INDEX idx_meter_consume_time,
    ADD INDEX idx_meter_meter_consume_time (meter_id, meter_consume_time);
