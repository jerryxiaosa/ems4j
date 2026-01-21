package info.zhihui.ems.business.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.AreaBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("energy_gateway")
public class GatewayEntity extends AreaBaseEntity {

    private Integer id;

    /**
     * 空间id
     */
    private Integer spaceId;

    /**
     * 编号
     */
    private String gatewayNo;

    /**
     * 设备编号，设备上报标识
     */
    private String deviceNo;

    /**
     * 网关名称
     */
    private String gatewayName;

    /**
     * 网关型号id
     */
    private Integer modelId;

    /**
     * 产品唯一标识
     */
    private String productCode;

    /**
     * 通讯方式
     */
    private String communicateModel;

    /**
     * 网关设备序列号
     */
    private String sn;

    /**
     * 网关imei
     */
    private String imei;

    /**
     * iot服务里的id
     */
    private Integer iotId;

    /**
     * 网关是否在线
     */
    private Boolean isOnline;

    /**
     * 网关配置信息,json字符串
     */
    private String configInfo;

    /**
     * 网关备注
     */
    private String remark;


}
