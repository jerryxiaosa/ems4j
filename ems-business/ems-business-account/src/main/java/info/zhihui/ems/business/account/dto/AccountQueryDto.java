package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AccountQueryDto {

    private Boolean includeDeleted;

    private OwnerTypeEnum ownerType;

    private List<Integer> ownerIds;

    private String ownerNameLike;

    private ElectricAccountTypeEnum electricAccountType;

    private Integer warnPlanId;
}
