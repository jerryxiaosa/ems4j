package info.zhihui.ems.foundation.user.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户查询条件QO
 */
@Data
@Accessors(chain = true)
public class UserQueryQo {
    private String userNameLike;
    private String realNameLike;
    private String userName;
    private String userPhone;
    private Integer organizationId;
    private List<Integer> ids;
}