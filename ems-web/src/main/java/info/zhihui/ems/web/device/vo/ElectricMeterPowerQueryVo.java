package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 电量查询请求
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricMeterPowerQueryVo", description = "电表电量查询条件")
public class ElectricMeterPowerQueryVo {

    @NotEmpty
    @Schema(description = "电量类型编码列表，参考 electricDegreeType", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> types;
}
