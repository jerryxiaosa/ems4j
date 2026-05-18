package info.zhihui.ems.web.mini.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 小程序首页趋势视图对象。
 */
@Data
@Accessors(chain = true)
public class MiniHomeTrendVo {

    private String metric;

    private String unit;

    private List<MiniTrendPointVo> list;

    private String tip;
}
