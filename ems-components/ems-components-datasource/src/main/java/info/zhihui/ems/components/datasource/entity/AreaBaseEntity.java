package info.zhihui.ems.components.datasource.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AreaBaseEntity extends BaseEntity {

    private Integer ownAreaId;
}
