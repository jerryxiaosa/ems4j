package info.zhihui.ems.web.mini.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 小程序首页趋势点视图对象。
 */
@Data
@Accessors(chain = true)
public class MiniTrendPointVo {

    private String date;

    private BigDecimal value;
}
