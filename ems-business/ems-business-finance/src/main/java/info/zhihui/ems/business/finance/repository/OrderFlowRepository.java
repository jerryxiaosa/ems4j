package info.zhihui.ems.business.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.OrderFlowEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrderFlowRepository extends BaseMapper<OrderFlowEntity> {

    /**
     * 按消费单号回填余额快照
     *
     * @param consumeId 消费单号
     * @param beginBalance 处理前余额
     * @param endBalance 处理后余额
     * @return 更新行数
     */
    Integer updateBalanceSnapshotByConsumeId(@Param("consumeId") String consumeId,
                                             @Param("beginBalance") BigDecimal beginBalance,
                                             @Param("endBalance") BigDecimal endBalance);
}
