package info.zhihui.ems.components.redis.properties.server;

import lombok.Data;
import org.redisson.config.ReadMode;

@Data
public class ClusterServersConfig {

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * master最小空闲连接数
     */
    private int masterConnectionMinimumIdleSize;

    /**
     * master连接池大小
     */
    private int masterConnectionPoolSize;

    /**
     * slave最小空闲连接数
     */
    private int slaveConnectionMinimumIdleSize;

    /**
     * slave连接池大小
     */
    private int slaveConnectionPoolSize;

    /**
     * 连接空闲超时，单位：毫秒
     */
    private int idleConnectionTimeout;

    /**
     * 命令等待超时，单位：毫秒
     */
    private int timeout;

    /**
     * 读取模式
     */
    private ReadMode readMode;

}
