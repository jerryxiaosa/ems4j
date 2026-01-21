package info.zhihui.ems.foundation.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 菜单路径闭包表实体
 */
@Data
@Accessors(chain = true)
@TableName("sys_menu_path")
public class MenuPathEntity {
    private Integer id;

    /**
     * 祖先菜单ID
     */
    private Integer ancestorId;

    /**
     * 后代菜单ID
     */
    private Integer descendantId;

    /**
     * 祖先到后代的距离（自环为 0）
     */
    private Integer depth;
}