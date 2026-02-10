package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.common.enums.CalculateTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ElectricMeterQueryDto {

    /**
     * 电表id
     */
    private Integer meterId;

    /**
     * 电表名称
     */
    private String meterName;

    /**
     * 电表编号，系统生成
     */
    private String meterNo;

    /**
     * 所属账户id
     */
    private Integer accountId;

    /**
     * iot服务里的id
     */
    private String iotId;

    /**
     * 区域id
     */
    private Integer areaId;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 是否断闸
     */
    private Boolean isCutOff;

    /**
     * 是否计量
     * 汇总时是否计算在内，和calculate_type无关
     */
    private Boolean isCalculate;


    /**
     * 计量类型
     * 用量类型，和is_calculate无关
     */
    private CalculateTypeEnum calculateType;

    /**
     * 是否为预付费
     */
    private Boolean isPrepay;


    /**
     * 移动设备IMEI
     */
    private String imei;

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
     * 不包括的meterId
     */
    private Integer neId;

    /**
     * 包括的meterId
     */
    private List<Integer> inIds;

    /**
     * meterName、meter_no搜索关键字
     */
    private String searchKey;

    /**
     * 空间id
     */
    private List<Integer> spaceIds;

}
