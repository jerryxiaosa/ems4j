package info.zhihui.ems.mq.api.message.order.status;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TerminationSuccessMessage extends BaseOrderStatusMessage {

    /**
     * 账户id
     */
    @NotNull(message = "账户id不能为空")
    private Integer accountId;

    /**
     * 电费计费类型
     */
    @NotNull(message = "电费计费类型不能为空")
    private ElectricAccountTypeEnum electricAccountType;

    /**
     * 销表数量
     */
    @NotNull(message = "电费数量不能为空")
    @Min(value = 1, message = "电费数量不能小于1")
    private Integer electricMeterAmount;

    /**
     * 是否全部销户
     */
    @NotNull(message = "是否全取消不能为空")
    private Boolean fullCancel;

    /**
     * 电表id列表
     */
    @NotEmpty(message = "电表id列表不能为空")
    private List<Integer> meterIdList;


}
