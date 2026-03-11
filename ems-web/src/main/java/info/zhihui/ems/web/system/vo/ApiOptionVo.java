package info.zhihui.ems.web.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统接口选项 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "ApiOptionVo", description = "系统接口选项")
public class ApiOptionVo {

    @Schema(description = "接口键，格式为 HTTP_METHOD:path")
    private String key;

    @Schema(description = "权限标识")
    private String permissionCode;
}
