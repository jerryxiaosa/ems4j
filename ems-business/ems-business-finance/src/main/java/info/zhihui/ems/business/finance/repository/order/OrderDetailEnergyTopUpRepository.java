package info.zhihui.ems.business.finance.repository.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.order.OrderDetailEnergyTopUpEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailEnergyTopUpRepository extends BaseMapper<OrderDetailEnergyTopUpEntity> {
    
    /**
     * 根据订单号查询能耗充值订单详情
     *
     * @param orderSn 订单号
     * @return 能耗充值订单详情实体
     */
    OrderDetailEnergyTopUpEntity selectByOrderSn(String orderSn);
}

