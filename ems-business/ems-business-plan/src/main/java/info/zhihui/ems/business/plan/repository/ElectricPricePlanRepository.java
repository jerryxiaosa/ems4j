package info.zhihui.ems.business.plan.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.plan.entity.ElectricPricePlanEntity;
import info.zhihui.ems.business.plan.qo.ElectricPricePlanQo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectricPricePlanRepository extends BaseMapper<ElectricPricePlanEntity> {

    List<ElectricPricePlanEntity> findList(ElectricPricePlanQo query);

}