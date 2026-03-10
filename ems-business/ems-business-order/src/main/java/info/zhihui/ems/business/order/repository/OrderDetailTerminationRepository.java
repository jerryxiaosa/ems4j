package info.zhihui.ems.business.order.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.order.entity.OrderDetailTerminationEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailTerminationRepository extends BaseMapper<OrderDetailTerminationEntity> {
    
    /**
     * 根据订单号查询销户/销表结算订单详情
     *
     * @param orderSn 订单号
     * @return 销户/销表结算订单详情实体
     */
    OrderDetailTerminationEntity selectByOrderSn(String orderSn);
}

