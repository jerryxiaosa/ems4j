package info.zhihui.ems.foundation.user.bo;

import info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum;
import info.zhihui.ems.foundation.user.enums.UserGenderEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class UserBo {
    /**
     * 主键ID
     */
    private Integer id;
    /**
     * 登录账号（唯一）
     */
    private String userName;
    /**
     * 所属机构ID
     */
    private Integer organizationId;
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
    private UserGenderEnum userGender;
    /**
     * 证件类型
     */
    private CertificatesTypeEnum certificatesType;
    /**
     * 证件号码
     */
    private String certificatesNo;
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 用户关联的角色列表
     */
    private List<RoleSimpleBo> roles;
}
