package info.zhihui.ems.web.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import info.zhihui.ems.components.translate.annotation.BizLabel;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum;
import info.zhihui.ems.web.common.resolver.OrganizationNameResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户原始信息 VO。
 */
@Data
@Accessors(chain = true)
@Schema(name = "UserRawVo", description = "用户原始信息数据")
public class UserRawVo {

    @Schema(description = "用户ID")
    private Integer id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "所属机构ID")
    private Integer organizationId;

    @Schema(description = "所属机构名称")
    @BizLabel(source = "organizationId", resolver = OrganizationNameResolver.class)
    private String organizationName;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String userPhone;

    @Schema(description = "性别编码，参考 userGender")
    private Integer userGender;

    @Schema(description = "证件类型编码，参考 certificatesType")
    private Integer certificatesType;

    @Schema(description = "证件类型名称")
    @EnumLabel(source = "certificatesType", enumClass = CertificatesTypeEnum.class)
    private String certificatesTypeText;

    @Schema(description = "证件号码")
    private String certificatesNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "角色列表")
    private List<UserRoleVo> roles;
}
