package info.zhihui.ems.mq.rabbitmq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 事务消息实体
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@TableName("sys_transaction_message")
public class TransactionMessageEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 编号
     */
    private String sn;

    /**
     * 消息载荷类型
     */
    private String payloadType;

    /**
     * 消息载荷
     */
    private String payload;

    /**
     * 消息目标地址
     */
    private String destination;

    /**
     * 消息路由信息
     */
    private String route;

    /**
     * 最后运行时间
     */
    private LocalDateTime lastRunAt;

    /**
     * 尝试次数
     */
    private Integer tryTimes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否成功
     */
    private Boolean isSuccess;
}
