package info.zhihui.ems.business.device.bo;

import info.zhihui.ems.common.enums.CalculateTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.common.model.OperatorInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电表业务对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ElectricMeterBo extends OperatorInfo {

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
     * 最近一次确认在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 是否断闸
     */
    private Boolean isCutOff;

    /**
     * 备注
     */
    private String remark;

    /**
     * iot服务里的id
     */
    private String iotId;

    /**
     * 是否计量
     * 汇总时是否计算在内，和calculate_type无关
     */
    private Boolean isCalculate;

    /**
     * 用量类型，和is_calculate无关
     * 目前是设备品类
     */
    private CalculateTypeEnum calculateType;

    /**
     * 是否为预付费
     */
    private Boolean isPrepay;

    /**
     * 是否保电，即欠费不断电
     */
    private Boolean protectedModel;

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
    private WarnTypeEnum warnType;

    /**
     * 所属账户id
     */
    private Integer accountId;

    /**
     * ct变比
     */
    private Integer ct;

    /**
     * 所属区域ID
     */
    private Integer ownAreaId;
}
