package info.zhihui.ems.components.lock.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lock")
public class LockProperties {
    boolean distributed;
    LocalLockProperties localLockProperties;
}
