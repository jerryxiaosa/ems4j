package info.zhihui.ems.foundation.space.dto;

import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class SpaceQueryDto {
    private Set<Integer> ids;

    private Integer pid;

    private String name;

    private List<SpaceTypeEnum> type;
}
