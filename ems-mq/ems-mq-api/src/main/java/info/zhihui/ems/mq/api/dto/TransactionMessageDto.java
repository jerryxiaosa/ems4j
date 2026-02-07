package info.zhihui.ems.mq.api.dto;

import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 事务消息新增数据传输对象
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class TransactionMessageDto {

    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    private TransactionMessageBusinessTypeEnum businessType;

    /**
     * 序列号
     */
    @NotEmpty(message = "序列号不能为空")
    private String sn;

    /**
     * 消息内容
     */
    @Valid
    @NotNull(message = "消息内容不能为空")
    private MqMessage message;
}
