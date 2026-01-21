package info.zhihui.ems.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class MqMessage {
    private String messageDestination;

    private String routingIdentifier;

    private Object payload;
}
