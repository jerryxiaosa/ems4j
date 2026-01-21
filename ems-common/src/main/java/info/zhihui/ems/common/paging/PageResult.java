package info.zhihui.ems.common.paging;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PageResult<T> {
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private List<T> list;
}
