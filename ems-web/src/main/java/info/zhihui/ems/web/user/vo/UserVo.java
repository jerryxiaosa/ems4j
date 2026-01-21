package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户信息 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "UserVo", description = "用户信息数据")
public class UserVo {

    @Schema(description = "用户ID")
    private Integer id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "所属机构ID")
    private Integer organizationId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String userPhone;

    @Schema(description = "性别编码，参考 userGender")
    private Integer userGender;

    @Schema(description = "证件类型编码，参考 certificatesType")
    private Integer certificatesType;

    @Schema(description = "证件号码")
    private String certificatesNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "角色列表")
    private List<UserRoleVo> roles;
}
