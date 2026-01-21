package info.zhihui.ems.mq.api.bo;

import info.zhihui.ems.common.model.MqMessage;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 事务消息业务对象
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class TransactionMessageBo {

    private Integer id;

    /**
     * 业务类型
     */
    private TransactionMessageBusinessTypeEnum businessType;

    /**
     * 序列号
     */
    private String sn;

    /**
     * 消息内容
     */
    private MqMessage message;

    /**
     * 消息载荷类型
     */
    private String payloadType;

    /**
     * 最后运行时间
     */
    private LocalDateTime lastRunAt;

    /**
     * 是否成功
     */
    private Boolean isSuccess;

    /**
     * 尝试次数
     */
    private Integer tryTimes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
