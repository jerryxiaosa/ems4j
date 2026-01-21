package info.zhihui.ems.foundation.system.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 系统配置查询 QO
 */
@Data
@Accessors(chain = true)
public class ConfigQueryQo {

    private Set<Integer> ids;

    private String configModuleName;

    private String configKeyLike;

    private String configNameLike;

    private Boolean isSystem;
}
