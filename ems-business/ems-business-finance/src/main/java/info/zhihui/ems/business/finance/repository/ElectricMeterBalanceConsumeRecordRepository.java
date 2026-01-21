package info.zhihui.ems.business.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.qo.ElectricPowerConsumeRecordQo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 余额消费记录数据访问层
 *
 * @author jerryxiaosa
 */
@Repository
public interface ElectricMeterBalanceConsumeRecordRepository extends BaseMapper<ElectricMeterBalanceConsumeRecordEntity> {

    /**
     * 根据查询条件查询电量消费记录
     *
     * @param qo 查询条件
     * @return 电量消费记录列表
     */
    List<ElectricMeterBalanceConsumeRecordEntity> selectByQo(ElectricPowerConsumeRecordQo qo);
}