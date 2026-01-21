package info.zhihui.ems.business.device.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
public class GatewayQo {

    private String searchKey;

    private String eqSn;

    private String sn;

    private Boolean isOnline;

    private Integer iotId;

    private List<Integer> spaceIds;

}
