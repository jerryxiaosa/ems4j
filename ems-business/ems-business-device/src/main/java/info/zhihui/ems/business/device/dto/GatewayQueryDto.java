package info.zhihui.ems.business.device.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
public class GatewayQueryDto {

    private String searchKey;

    private String sn;

    private Boolean isOnline;

    private String iotId;

    private List<Integer> spaceIds;

}
