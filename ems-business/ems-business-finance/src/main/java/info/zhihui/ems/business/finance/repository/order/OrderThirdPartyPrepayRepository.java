package info.zhihui.ems.business.finance.repository.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyPrepayEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderThirdPartyPrepayRepository extends BaseMapper<OrderThirdPartyPrepayEntity> {
    int updateByOrderSn(OrderThirdPartyPrepayEntity orderThirdPartyPrepayEntity);
}

