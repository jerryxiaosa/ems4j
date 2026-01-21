package info.zhihui.ems.foundation.integration.core.bo;

import info.zhihui.ems.common.model.OperatorInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DeviceTypeBo extends OperatorInfo {

    private Integer id;
    private Integer pid;
    /**
     *  祖级id，不包括自身
     */
    private String ancestorId;
    private String typeName;
    private String typeKey;
    /** 层级 */
    private Integer level;
}
