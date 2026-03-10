package info.zhihui.ems.business.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.billing.entity.BalanceEntity;
import info.zhihui.ems.business.billing.qo.BalanceListQueryQo;
import info.zhihui.ems.business.billing.qo.BalanceQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceRepository extends BaseMapper<BalanceEntity> {
    Integer balanceTopUp(BalanceQo balanceQo);

    List<BalanceEntity> findListByQuery(@Param("qo") BalanceListQueryQo queryQo);

    Integer deleteBalance(BalanceQo balanceQo);
}
