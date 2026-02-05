package info.zhihui.ems.components.redis.properties;

import info.zhihui.ems.components.redis.properties.server.ClusterServersConfig;
import info.zhihui.ems.components.redis.properties.server.SingleServerConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Redisson 配置属性
 *
 * @author Lion Li
 */
@Data
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    /**
     * redis缓存key前缀
     */
    private String keyPrefix;

    /**
     * 线程池数量,默认值 = 当前处理核数量 * 2
     */
    private int threads;

    /**
     * Netty线程池数量,默认值 = 当前处理核数量 * 2
     */
    private int nettyThreads;

    /**
     * 单机服务配置
     */
    private SingleServerConfig singleServerConfig;

    /**
     * 集群服务配置
     */
    private ClusterServersConfig clusterServersConfig;

    /**
     * 是否启用反序列化白名单
     */
    private boolean allowlistEnabled;

    /**
     * 允许反序列化的包前缀
     */
    private List<String> allowlistPackages;

}
