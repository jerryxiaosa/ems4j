package info.zhihui.ems.foundation.user.dto;

import info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum;
import info.zhihui.ems.foundation.user.enums.UserGenderEnum;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserUpdateDto {
    /**
     * 用户ID
     */
    @NotNull
    private Integer id;

    /**
     * 真实姓名（≤30）
     */
    @Size(max = 30)
    private String realName;

    /**
     * 电话号码（≤20）
     */
    @Size(max = 20)
    private String userPhone;

    /**
     * 性别枚举（1男 2女）
     */
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
     * 所属机构ID（可选）
     */
    private Integer organizationId;

    /**
     * 角色ID列表
     */
    private List<Integer> roleIds;
}