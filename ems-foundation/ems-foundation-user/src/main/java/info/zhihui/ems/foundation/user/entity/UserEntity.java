package info.zhihui.ems.foundation.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("sys_user")
public class UserEntity extends BaseEntity {

    private Integer id;

    /**
     * 登录账号（唯一）
     */
    private String userName;

    /**
     * 密码散列
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 电话号码
     */
    private String userPhone;

    /**
     * 性别：1男 2女
     */
    private Integer userGender;

    /**
     * 证件类型
     */
    private Integer certificatesType;

    /**
     * 证件号码
     */
    private String certificatesNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 所属机构ID
     */
    private Integer organizationId;
}