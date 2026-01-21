package info.zhihui.ems.business.device.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.qo.ElectricMeterBatchUpdateQo;
import info.zhihui.ems.business.device.qo.ElectricMeterQo;
import info.zhihui.ems.business.device.qo.ElectricMeterResetAccountQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectricMeterRepository extends BaseMapper<ElectricMeterEntity> {

    List<ElectricMeterEntity> findList(ElectricMeterQo query);

    int batchUpdate(ElectricMeterBatchUpdateQo qo);

    int resetMeterAccountInfo(ElectricMeterResetAccountQo qo);

    /**
     * 更新电表信息，支持calculateType的动态控制
     *
     * @param entity             电表实体对象
     * @param resetCalculateType 是否重置计量类型：
     *                           - true: 将 calculate_type 设置为 NULL
     *                           - false: 根据 entity.calculateType 的值决定是否更新该列
     * @return 更新的记录数
     */
    int updateWithCalculateTypeControl(@Param("entity") ElectricMeterEntity entity, @Param("resetCalculateType") boolean resetCalculateType);

}