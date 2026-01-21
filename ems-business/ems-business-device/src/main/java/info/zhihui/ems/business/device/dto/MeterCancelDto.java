package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class MeterCancelDto {
    @Size(max = 100, message = "最多允许同时销户100个电表")
    @Valid
    private List<MeterCancelDetailDto> meterCloseDetail;

    /**
     * 销户编号
     */
    @NotEmpty
    private String cancelNo;

    /**
     * 账户id
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 账户归属者id
     */
    @NotNull(message = "账户归属者id不能为空")
    private Integer ownerId;

    /**
     * 账户归属者类型
     */
    @NotNull(message = "账户归属者类型不能为空")
    private OwnerTypeEnum ownerType;

    /**
     * 账户归属者名称
     */
    @NotEmpty(message = "账户归属者名称不能为空")
    private String ownerName;

    /**
     * 电费账户类型
     */
    @NotNull(message = "电表账户类型不能为空")
    private ElectricAccountTypeEnum electricAccountType;
}
