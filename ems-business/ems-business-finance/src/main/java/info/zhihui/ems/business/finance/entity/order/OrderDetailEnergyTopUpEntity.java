package info.zhihui.ems.business.finance.entity.order;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 能耗充值订单详情表
 */
@Data
@Accessors(chain = true)
@TableName("order_detail_energy_top_up")
public class OrderDetailEnergyTopUpEntity {
    /**
     * 主键id
     */
    private Integer id;
    /**
     * 订单号
     */
    private String orderSn;
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
     * 账户id
     */
    private Integer accountId;
    /**
     * 电费账户类型：0按需、1包月、2合并计费
     */
    private Integer electricAccountType;
    /**
     * 表类型：1电 2水
     */
    private Integer meterType;
    /**
     * 表id
     */
    private Integer meterId;
    /**
     * 电表名称
     */
    private String meterName;
    /**
     * 电表编号，系统生成
     */
    private String deviceNo;
    /**
     * 空间id
     */
    private Integer spaceId;
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 父级id
     */
    private String spaceParentIds;
    /**
     * 父级名称
     */
    private String spaceParentNames;
    /**
     * 余额类型：0账户余额，1电表余额
     */
    private Integer balanceType;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
