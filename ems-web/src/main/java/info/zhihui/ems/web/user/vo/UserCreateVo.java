package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户新增请求 VO
 */
@Data
@Schema(name = "UserCreateVo", description = "用户新增请求参数")
public class UserCreateVo {

    @NotBlank
    @Size(max = 40)
    @Schema(description = "用户名")
    private String userName;

    @NotBlank
    @Size(min = 6, max = 20)
    @Schema(description = "登录密码")
    private String password;

    @NotBlank
    @Size(max = 30)
    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "手机号码")
    private String userPhone;

    @NotNull
    @Schema(description = "性别编码，参考 userGender")
    private Integer userGender;

    @Schema(description = "证件类型编码，参考 certificatesType")
    private Integer certificatesType;

    @Size(max = 20)
    @Schema(description = "证件号码")
    private String certificatesNo;

    @Size(max = 500)
    @Schema(description = "备注")
    private String remark;

    @NotNull
    @Schema(description = "所属机构ID")
    private Integer organizationId;

    @Schema(description = "角色ID列表")
    private List<Integer> roleIds;
}
