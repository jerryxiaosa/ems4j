package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.common.enums.CalculateTypeEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricMeterCreateDto {

    /**
     * 区域id
     */
    @NotNull(message = "空间id不能为空")
    private Integer spaceId;

    /**
     * 电表名称
     */
    @NotBlank(message = "电表名称不能为空")
    @Size(max = 20, message = "电表名称不能超过20个字")
    private String meterName;

    /**
     * 设备编号，设备上报标识
     */
    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    /**
     * 是否计量
     * 汇总时是否计算在内，和calculate_type无关
     */
    @NotNull(message = "是否计量不能为空")
    private Boolean isCalculate;

    /**
     * 计量类型
     * 用量类型，和is_calculate无关
     */
    private CalculateTypeEnum calculateType;

    /**
     * 是否为预付费
     */
    @NotNull(message = "是否为预付费不能为空")
    private Boolean isPrepay;

    /**
     * 电表型号id
     */
    @NotNull(message = "设备型号不能为空")
    private Integer modelId;

    /**
     * 网关id
     */
    private Integer gatewayId;

    /**
     * 串口号
     */
    private Integer portNo;

    /**
     * 电表通讯地址
     * 电表通讯地址和串口号唯一标识了在某个网关中的电表
     */
    private Integer meterAddress;

    /**
     * 移动设备IMEI
     */
    private String imei;

    /**
     * 电流互感器变比
     */
    @Positive(message = "CT变比必须大于0")
    @Max(value = 65535, message = "CT变比不能超过65535")
    private Integer ct;


}
