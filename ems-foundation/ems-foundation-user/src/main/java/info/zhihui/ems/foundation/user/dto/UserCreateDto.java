package info.zhihui.ems.foundation.user.dto;

import info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum;
import info.zhihui.ems.foundation.user.enums.UserGenderEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserCreateDto {
    /**
     * 登录账号（唯一，≤40）
     */
    @NotBlank
    @Size(max = 40)
    private String userName;

    /**
     * 登录密码（6~64），入库前会进行散列
     */
    @NotBlank
    @Size(min = 6, max = 20)
    private String password;

    /**
     * 真实姓名（≤30）
     */
    @NotBlank
    @Size(max = 30)
    private String realName;

    /**
     * 电话号码（≤20）
     */
    @NotBlank
    @Size(max = 20)
    private String userPhone;

    /**
     * 性别枚举（1男 2女）
     */
    @NotNull
    private UserGenderEnum userGender;

    /**
     * 证件类型枚举
     */
    private CertificatesTypeEnum certificatesType;

    /**
     * 证件号码（≤20）
     */
    @Size(max = 20)
    private String certificatesNo;

    /**
     * 备注（≤500）
     */
    @Size(max = 500)
    private String remark;

    /**
     * 所属机构ID
     */
    @NotNull
    private Integer organizationId;
    
    /**
     * 角色ID列表
     */
    private List<Integer> roleIds;
}