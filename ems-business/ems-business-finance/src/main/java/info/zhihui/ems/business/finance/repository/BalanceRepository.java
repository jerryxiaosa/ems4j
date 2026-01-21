package info.zhihui.ems.business.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.BalanceEntity;
import info.zhihui.ems.business.finance.qo.BalanceQo;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceRepository extends BaseMapper<BalanceEntity> {
    Integer balanceTopUp(BalanceQo balanceQo);

    BalanceEntity balanceQuery(BalanceQo balanceQo);

    Integer deleteBalance(BalanceQo balanceQo);
}