package info.zhihui.ems.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电表电量关系实体
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@TableName("energy_electric_meter_power_relation")
public class ElectricMeterPowerRelationEntity {
    private Integer id;

    /**
     * 记录ID
     */
    private Integer recordId;

    /**
     * 电表ID
     */
    private Integer meterId;

    /**
     * 是否计算
     */
    private Boolean isCalculate;

    /**
     * 计算类型
     */
    private Integer calculateType;

    /**
     * 计算类型名称
     */
    private String calculateTypeName;

    /**
     * 空间ID
     */
    private Integer spaceId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 父级空间ID列表
     */
    private String spaceParentIds;

    /**
     * 父级空间名称列表
     */
    private String spaceParentNames;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 归属者ID
     */
    private Integer ownerId;

    /**
     * 归属者类型
     */
    private Integer ownerType;

    /**
     * 归属者名称
     */
    private String ownerName;

    /**
     * 电账户类型
     */
    private Integer electricAccountType;

    /**
     * 抄表时间
     */
    private LocalDateTime recordTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
}