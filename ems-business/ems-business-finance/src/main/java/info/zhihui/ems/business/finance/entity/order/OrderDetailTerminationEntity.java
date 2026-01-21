package info.zhihui.ems.business.finance.entity.order;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销户/销表结算订单详情表实体
 */
@Data
@Accessors(chain = true)
@TableName("order_detail_termination")
public class OrderDetailTerminationEntity {

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 销户编号
     */
    private String cancelNo;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 账户归属者id
     */
    private Integer ownerId;

    /**
     * 账户类型，0企业1个人
     */
    private Integer ownerType;

    /**
     * 所有人名称
     */
    private String ownerName;

    /**
     * 结算金额
     */
    private BigDecimal settlementAmount;

    /**
     * 电费计费类型，0按需、1包月、2合并计费
     */
    private Integer electricAccountType;


    /**
     * 销表数量
     */
    private Integer electricMeterAmount;

    /**
     * 是否全部销户
     */
    private Boolean fullCancel;

    /**
     * 终止原因
     */
    private String closeReason;

    /**
     * 结算快照明细(JSON)
     */
    private String snapshotPayload;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
