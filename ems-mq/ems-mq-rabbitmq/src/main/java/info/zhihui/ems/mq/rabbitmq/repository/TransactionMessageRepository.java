package info.zhihui.ems.mq.rabbitmq.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.mq.rabbitmq.entity.TransactionMessageEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事务消息Repository
 *
 * @author jerryxiaosa
 */
@Repository
public interface TransactionMessageRepository extends BaseMapper<TransactionMessageEntity> {

    /**
     * 根据业务类型和编号查询
     *
     * @param businessType 业务类型
     * @param sn           编号
     * @return 事务消息实体
     */
    TransactionMessageEntity getByBusinessTypeAndSn(@Param("businessType") String businessType, @Param("sn") String sn);

    /**
     * 根据成功状态查询消息
     *
     * @param maxRetry 最大重试次数
     * @param pastTime 查询最久的时间
     * @param limit    查询数量
     * @return 事务消息列表
     */
    List<TransactionMessageEntity> getPastUnsuccessful(@Param("maxRetry") Integer maxRetry, @Param("pastTime") LocalDateTime pastTime, @Param("limit") Integer limit);

    /**
     * 更新事务消息
     * 通过业务类型和序列号定位记录，更新最后运行时间、成功状态和尝试次数
     *
     * @param entity 事务消息实体，包含更新的字段值
     * @return 更新影响的行数
     */
    int updateTransactionMessage(TransactionMessageEntity entity);

}
