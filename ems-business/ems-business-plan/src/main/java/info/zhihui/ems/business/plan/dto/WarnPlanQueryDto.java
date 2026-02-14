package info.zhihui.ems.business.plan.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class WarnPlanQueryDto {
    private String name;

    private List<Integer> ids;
}
