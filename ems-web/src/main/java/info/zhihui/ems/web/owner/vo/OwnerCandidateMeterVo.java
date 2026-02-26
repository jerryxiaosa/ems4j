package info.zhihui.ems.web.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 主体候选电表信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "OwnerCandidateMeterVo", description = "主体候选电表信息")
public class OwnerCandidateMeterVo {

    @Schema(description = "电表ID")
    private Integer id;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "电表编号")
    private String meterNo;

    @Schema(description = "空间ID")
    private Integer spaceId;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "空间父级名称列表")
    private List<String> spaceParentNames;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "是否预付费")
    private Boolean isPrepay;
}
