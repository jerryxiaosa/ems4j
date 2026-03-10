package info.zhihui.ems.business.order.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.order.entity.OrderEntity;
import info.zhihui.ems.business.order.entity.OrderThirdPartyPrepayEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderThirdPartyPrepayRepository extends BaseMapper<OrderThirdPartyPrepayEntity> {
    int updateByOrderSn(OrderThirdPartyPrepayEntity orderThirdPartyPrepayEntity);
}

