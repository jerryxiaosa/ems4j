package info.zhihui.ems.business.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 账户空间租赁关系实体
 */
@Data
@Accessors(chain = true)
@TableName("energy_account_space_rel")
public class AccountSpaceRelEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 账户ID
     */
    private Integer accountId;

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
