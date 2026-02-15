package info.zhihui.ems.business.account.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("energy_account")
public class AccountEntity extends BaseEntity {

    private Integer id;

    /**
     * 账户类型：0企业，1个人
     */
    private Integer ownerType;

    /**
     * 账户归属者id
     */
    private Integer ownerId;

    /**
     * 账户归属者名称
     */
    private String ownerName;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系方式
     */
    private String contactPhone;

    /**
     * 预警方案id
     */
    private Integer warnPlanId;

    /**
     * 电价方案id
     */
    private Integer electricPricePlanId;

    /**
     * 电费预警级别
     */
    private String electricWarnType;

    /**
     * 电费计费类型
     */
    private Integer electricAccountType;

    /**
     * 包月费用
     */
    private BigDecimal monthlyPayAmount;

    /**
     * 删除时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime deleteTime;

}
