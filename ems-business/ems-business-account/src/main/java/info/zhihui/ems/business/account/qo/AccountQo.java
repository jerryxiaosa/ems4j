package info.zhihui.ems.business.account.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AccountQo {

    private Boolean includeDeleted;

    private Integer ownerType;

    private List<Integer> ownerIds;

    private Integer electricAccountType;

    private Integer warnPlanId;
}
