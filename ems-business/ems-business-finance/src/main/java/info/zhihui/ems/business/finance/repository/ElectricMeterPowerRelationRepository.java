package info.zhihui.ems.business.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerRelationEntity;
import org.springframework.stereotype.Repository;

/**
 * 电表电量关系数据访问层
 *
 * @author jerryxiaosa
 */
@Repository
public interface ElectricMeterPowerRelationRepository extends BaseMapper<ElectricMeterPowerRelationEntity> {

}