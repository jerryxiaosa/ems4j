package info.zhihui.ems.business.device.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class ElectricMeterBatchUpdateQo {

    /**
     * 电表id
     */
    private List<Integer> meterIds;

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
     * 当前预警类型
     */
    private String warnType;

    /**
     * 更新人
     */
    private Integer updateUser;

    /**
     * 更新人名称
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
