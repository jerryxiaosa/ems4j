package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayUpdateDto extends GatewayCreateDto {

    @NotNull(message = "网关id不能为空")
    private Integer id;

    /**
     * 网关配置信息,json字符串
     */
    private String configInfo;

    @Override
    public String getConfigInfo() {
        return configInfo;
    }
}
