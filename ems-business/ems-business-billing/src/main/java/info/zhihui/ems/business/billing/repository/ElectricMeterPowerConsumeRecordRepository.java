package info.zhihui.ems.business.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.billing.qo.ElectricMeterPowerConsumeRecordQo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 电表电量消费记录数据访问层
 *
 * @author jerryxiaosa
 */
@Repository
public interface ElectricMeterPowerConsumeRecordRepository extends BaseMapper<ElectricMeterPowerConsumeRecordEntity> {
    ElectricMeterPowerConsumeRecordEntity getMeterLastConsumeRecord(Integer meterId);

    List<ElectricMeterPowerConsumeRecordEntity> findTrendRecordList(ElectricMeterPowerConsumeRecordQo qo);
}
