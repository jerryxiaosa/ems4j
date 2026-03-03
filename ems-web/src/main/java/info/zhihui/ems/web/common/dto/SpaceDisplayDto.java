package info.zhihui.ems.web.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 空间展示信息
 */
@Data
@Accessors(chain = true)
public class SpaceDisplayDto {

    private Integer id;

    private String name;

    private List<String> parentsNames;
}
