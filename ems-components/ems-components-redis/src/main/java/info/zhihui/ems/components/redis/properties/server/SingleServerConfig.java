package info.zhihui.ems.components.redis.properties.server;

import lombok.Data;

@Data
public class SingleServerConfig {

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 最小空闲连接数
     */
    private int connectionMinimumIdleSize;

    /**
     * 连接池大小
     */
    private int connectionPoolSize;

    /**
     * 连接空闲超时，单位：毫秒
     */
    private int idleConnectionTimeout;

    /**
     * 命令等待超时，单位：毫秒
     */
    private int timeout;
}
