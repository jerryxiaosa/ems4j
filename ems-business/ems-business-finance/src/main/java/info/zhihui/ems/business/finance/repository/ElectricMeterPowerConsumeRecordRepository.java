package info.zhihui.ems.business.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerConsumeRecordEntity;
import org.springframework.stereotype.Repository;

/**
 * 电表电量消费记录数据访问层
 *
 * @author jerryxiaosa
 */
@Repository
public interface ElectricMeterPowerConsumeRecordRepository extends BaseMapper<ElectricMeterPowerConsumeRecordEntity> {
    ElectricMeterPowerConsumeRecordEntity getMeterLastConsumeRecord(Integer meterId);
}