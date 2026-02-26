package info.zhihui.ems.business.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 主体空间租赁关系实体
 */
@Data
@Accessors(chain = true)
@TableName("energy_owner_space_rel")
public class OwnerSpaceRelEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 主体类型（见 OwnerTypeEnum code）
     */
    private Integer ownerType;

    /**
     * 主体ID
     */
    private Integer ownerId;

    /**
     * 空间ID
     */
    private Integer spaceId;

    private Integer createUser;

    private String createUserName;

    private LocalDateTime createTime;

    private Integer updateUser;

    private String updateUserName;

    private LocalDateTime updateTime;
}
