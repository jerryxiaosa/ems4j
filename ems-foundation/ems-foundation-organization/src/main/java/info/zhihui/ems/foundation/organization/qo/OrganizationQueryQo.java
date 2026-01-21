package info.zhihui.ems.foundation.organization.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
public class OrganizationQueryQo {
    private Set<Integer> ids;
    private String organizationNameLike;
    private String creditCode;
    private String managerNameLike;
}