package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.common.enums.CalculateTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class ElectricMeterUpdateDto {

    /**
     * 电表id
     */
    @NotNull(message = "电表id不能为空")
    private Integer id;

    /**
     * 区域id
     */
    private Integer spaceId;

    /**
     * 电表名称
     */
    @Size(max = 20, message = "电表名称不能超过20个字")
    private String meterName;

    /**
     * 设备编号，设备上报标识
     */
    private String deviceNo;

    /**
     * 是否计量
     * 汇总时是否计算在内，和calculate_type无关
     */
    private Boolean isCalculate;

    /**
     * 计量类型
     * 和is_calculate无关
     */
    private CalculateTypeEnum calculateType;

    /**
     * 是否为预付费
     */
    private Boolean isPrepay;

}
