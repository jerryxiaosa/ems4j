package info.zhihui.ems.foundation.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserQueryDto {
    /**
     * 用户名模糊匹配（≤40）
     */
    @Size(max = 40)
    private String userNameLike;

    /**
     * 真实姓名模糊匹配（≤30）
     */
    @Size(max = 30)
    private String realNameLike;

    /**
     * 电话号码（≤20）
     */
    @Size(max = 20)
    private String userPhone;

    /**
     * 机构ID
     */
    private Integer organizationId;

    /**
     * 用户ID集合
     */
    private List<Integer> ids;
}