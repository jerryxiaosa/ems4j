package info.zhihui.ems.business.order.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 订单详情查询结果
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class OrderDetailDto extends OrderListDto {
}
