package info.zhihui.ems.business.plan.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class WarnPlanQo {
    private String name;

    private List<Integer> ids;
}
