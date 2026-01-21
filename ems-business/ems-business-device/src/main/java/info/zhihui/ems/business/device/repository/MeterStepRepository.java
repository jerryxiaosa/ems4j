package info.zhihui.ems.business.device.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.device.entity.MeterStepEntity;
import info.zhihui.ems.business.device.qo.AccountMeterStepQo;
import org.springframework.stereotype.Repository;

@Repository
public interface MeterStepRepository extends BaseMapper<MeterStepEntity> {

    MeterStepEntity getOne(AccountMeterStepQo query);

    void clearLatestFlag(AccountMeterStepQo query);
}
