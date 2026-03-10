package info.zhihui.ems.business.order.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.order.entity.OrderEntity;
import info.zhihui.ems.business.order.qo.OrderListItemQo;
import info.zhihui.ems.business.order.qo.OrderQueryQo;
import org.apache.ibatis.annotations.Param;
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
     * @param qo 订单查询对象
     * @return 订单列表查询结果
     */
    List<OrderListItemQo> findList(@Param("qo") OrderQueryQo qo);

    /**
     * 根据订单编号查询订单详情
     *
     * @param orderSn 订单编号
     * @return 订单详情查询结果
     */
    OrderListItemQo selectDetailByOrderSn(@Param("orderSn") String orderSn);
}
