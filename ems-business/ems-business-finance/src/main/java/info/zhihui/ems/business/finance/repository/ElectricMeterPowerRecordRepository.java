package info.zhihui.ems.business.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerRecordEntity;
import info.zhihui.ems.business.finance.qo.ElectricMeterPowerRecordQo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 电表电量记录数据访问层
 *
 * @author jerryxiaosa
 */
@Repository
public interface ElectricMeterPowerRecordRepository extends BaseMapper<ElectricMeterPowerRecordEntity> {
    List<ElectricMeterPowerRecordEntity> findRecordList(ElectricMeterPowerRecordQo qo);
}