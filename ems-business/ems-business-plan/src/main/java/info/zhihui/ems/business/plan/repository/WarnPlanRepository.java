package info.zhihui.ems.business.plan.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.plan.entity.WarnPlanEntity;
import info.zhihui.ems.business.plan.qo.WarnPlanQo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarnPlanRepository extends BaseMapper<WarnPlanEntity> {

    List<WarnPlanEntity> getList(WarnPlanQo query);
}