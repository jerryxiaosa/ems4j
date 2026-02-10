package info.zhihui.ems.business.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.AreaBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 电表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("energy_electric_meter")
public class ElectricMeterEntity extends AreaBaseEntity {

    private Integer id;

    /**
     * 空间id
     */
    private Integer spaceId;

    /**
     * 电表名称
     */
    private String meterName;

    /**
     * 电表编号，系统生成
     */
    private String meterNo;

    /**
     * 设备编号，设备上报标识
     */
    private String deviceNo;

    /**
     * 型号id
     */
    private Integer modelId;

    /**
     * 产品唯一标识
     */
    private String productCode;

    /**
     * 通信模式
     */
    private String communicateModel;

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
     * 485表没有这个属性
     */
    private String imei;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 是否断闸
     */
    private Boolean isCutOff;

    /**
     * 所属账户id
     */
    private Integer accountId;

    /**
     * iot服务里的id
     */
    private String iotId;

    /**
     * ct变比
     */
    private Integer ct;

    /**
     * 是否计量
     * 汇总时是否计算在内，和calculate_type无关
     */
    private Boolean isCalculate;


    /**
     * 计量类型
     * 用量类型，和is_calculate无关
     */
    private Integer calculateType;

    /**
     * 是否保电，即欠费不断电
     */
    private Boolean protectedModel;

    /**
     * 是否为预付费
     */
    private Boolean isPrepay;

    /**
     * 计费方案id
     */
    private Integer pricePlanId;

    /**
     * 预警方案id
     */
    private Integer warnPlanId;

    /**
     * 预警类型，在第几阶段
     */
    private String warnType;

    /**
     * 备注
     */
    private String remark;
}
