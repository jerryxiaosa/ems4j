package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户更新请求 VO（不含 ID，ID 从路径获取）
 */
@Data
@Schema(name = "UserUpdateVo", description = "用户信息更新请求参数")
public class UserUpdateVo {

    @Size(max = 30)
    @Schema(description = "真实姓名")
    private String realName;

    @Size(max = 20)
    @Schema(description = "手机号码")
    private String userPhone;

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

    @Schema(description = "所属机构ID")
    private Integer organizationId;

    @Schema(description = "角色ID列表")
    private List<Integer> roleIds;
}
