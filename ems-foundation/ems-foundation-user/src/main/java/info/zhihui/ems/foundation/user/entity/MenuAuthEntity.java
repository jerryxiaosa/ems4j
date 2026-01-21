package info.zhihui.ems.foundation.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单权限实体
 */
@Data
@TableName("sys_menu_auth")
public class MenuAuthEntity {

    /** 主键ID */
    @TableId
    private Integer id;

    /** 关联菜单ID */
    private Integer menuId;

    /** 接口权限标识 */
    private String permissionCode;

    /** 创建时间 */
    private LocalDateTime createTime;
}
