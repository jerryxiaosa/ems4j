package info.zhihui.ems.foundation.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 枚举项 VO：包含值与描述。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnumItemDto {
    private Object value;
    private String info;
}