package info.zhihui.ems.business.device.qo;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccountMeterStepQo {

    private Integer accountId;

    private Integer meterType;

    private Integer meterId;

}
