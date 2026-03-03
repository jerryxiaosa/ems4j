package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 网关详情
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "GatewayDetailVo", description = "网关详情信息")
public class GatewayDetailVo extends GatewayVo {

    @Schema(description = "接入该网关的电表列表")
    private List<GatewayMeterVo> meterList;
}
