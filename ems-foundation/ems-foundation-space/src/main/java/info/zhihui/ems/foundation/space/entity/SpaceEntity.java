package info.zhihui.ems.foundation.space.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.AreaBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 空间表实体类
 * 对应数据库表：space
 *
 * @author system
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("sys_space")
public class SpaceEntity extends AreaBaseEntity {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父ID
     */
    private Integer pid;

    /**
     * 全路径，逗号隔开，包括自己
     */
    private String fullPath;

    /**
     * 空间类型：1主区域、2内部区域、房间、自定义区域
     */
    private Integer type;

    /**
     * 面积
     */
    private BigDecimal area;

    /**
     * 排序索引
     */
    private Integer sortIndex;
}