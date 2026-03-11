package info.zhihui.ems.business.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerRecordEntity;
import info.zhihui.ems.business.billing.qo.ElectricMeterPowerRecordQo;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 电表电量记录数据访问层
 *
 * @author jerryxiaosa
 */
@Repository
public interface ElectricMeterPowerRecordRepository extends BaseMapper<ElectricMeterPowerRecordEntity> {
    List<ElectricMeterPowerRecordEntity> findRecordList(ElectricMeterPowerRecordQo qo);
}
