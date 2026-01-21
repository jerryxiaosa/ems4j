package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 网关查询条件
 */
@Data
@Schema(name = "GatewayQueryVo", description = "网关查询条件")
public class GatewayQueryVo {

    @Schema(description = "搜索关键字")
    private String searchKey;

    @Schema(description = "网关序列号")
    private String sn;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "物联网ID")
    private Long iotId;

    @Schema(description = "空间ID列表")
    private List<Integer> spaceIds;
}
