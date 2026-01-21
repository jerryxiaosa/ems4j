package info.zhihui.ems.business.finance.repository.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends BaseMapper<OrderEntity> {
    /**
     * 根据订单编号查询订单信息
     * @param orderSn 订单编号
     * @return 订单信息
     */
    OrderEntity selectByOrderSn(String orderSn);

    /**
     * 根据订单编号更新订单信息
     *
     * @param orderEntity 订单信息
     * @return 更新结果
     */
    int updateByOrderSn(OrderEntity orderEntity);

    /**
     * 根据查询条件查找订单列表
     *
     * @param query 订单查询参数对象
     * @return 订单实体列表
     */
    List<OrderEntity> findList(OrderQueryDto query);
}

