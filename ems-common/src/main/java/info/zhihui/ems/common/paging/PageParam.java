package info.zhihui.ems.common.paging;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PageParam {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
