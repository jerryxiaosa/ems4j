package info.zhihui.ems.components.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import info.zhihui.ems.components.redis.handler.KeyPrefixHandler;
import info.zhihui.ems.components.redis.properties.RedissonProperties;
import info.zhihui.ems.components.redis.properties.server.ClusterServersConfig;
import info.zhihui.ems.components.redis.properties.server.SingleServerConfig;
import info.zhihui.ems.common.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;

import java.util.List;

/**
 * Redisson 配置属性
 *
 * @author Lion Li
 */

@Slf4j
public class RedissonCustomizer implements RedissonAutoConfigurationCustomizer {
    private final RedissonProperties redissonProperties;
    private static final List<String> DEFAULT_ALLOWLIST_PACKAGES = List.of(
            "info.zhihui.ems",
            "cn.dev33.satoken",
            "java.util",
            "java.lang",
            "java.time",
            "java.math"
    );

    public RedissonCustomizer(RedissonProperties redissonProperties) {
        this.redissonProperties = redissonProperties;
    }

    @Override
    public void customize(Config config) {
        ObjectMapper om = JacksonUtil.getObjectMapper();
        PolymorphicTypeValidator validator = resolveValidator();
        om.activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL);
        // 指定序列化输入的类型，类必须是非final修饰的。序列化时将对象全类名一起保存下来
        TypedJsonJacksonCodec jsonCodec = new TypedJsonJacksonCodec(Object.class, om);
        // 组合序列化 key 使用 String 内容使用通用 json 格式
        CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, jsonCodec, jsonCodec);
        config.setThreads(redissonProperties.getThreads())
                .setNettyThreads(redissonProperties.getNettyThreads())
                .setUseScriptCache(true)
                .setCodec(codec);

        // 单机模式
        SingleServerConfig singleServerConfig = redissonProperties.getSingleServerConfig();
        if (singleServerConfig != null) {
            config.useSingleServer()
                    //设置redis key前缀
                    .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                    .setTimeout(singleServerConfig.getTimeout())
                    .setClientName(singleServerConfig.getClientName())
                    .setIdleConnectionTimeout(singleServerConfig.getIdleConnectionTimeout())
                    .setConnectionMinimumIdleSize(singleServerConfig.getConnectionMinimumIdleSize())
                    .setConnectionPoolSize(singleServerConfig.getConnectionPoolSize());

            log.info("redis单机模式初始化完成");
            return;
        }

        // 集群模式
        ClusterServersConfig clusterServersConfig = redissonProperties.getClusterServersConfig();
        if (clusterServersConfig != null) {
            config.useClusterServers()
                    .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                    .setTimeout(clusterServersConfig.getTimeout())
                    .setClientName(clusterServersConfig.getClientName())
                    .setIdleConnectionTimeout(clusterServersConfig.getIdleConnectionTimeout())
                    .setMasterConnectionMinimumIdleSize(clusterServersConfig.getMasterConnectionMinimumIdleSize())
                    .setMasterConnectionPoolSize(clusterServersConfig.getMasterConnectionPoolSize())
                    .setSlaveConnectionMinimumIdleSize(clusterServersConfig.getSlaveConnectionMinimumIdleSize())
                    .setSlaveConnectionPoolSize(clusterServersConfig.getSlaveConnectionPoolSize())
                    .setReadMode(clusterServersConfig.getReadMode());

            log.info("redis集群模式初始化完成");
        }

    }

    private PolymorphicTypeValidator resolveValidator() {
        if (!redissonProperties.isAllowlistEnabled()) {
            return LaissezFaireSubTypeValidator.instance;
        }

        List<String> allowlistPackages = redissonProperties.getAllowlistPackages();
        if (allowlistPackages == null || allowlistPackages.isEmpty()) {
            log.warn("redisson.allowlist-packages 未配置，使用默认白名单");
            allowlistPackages = DEFAULT_ALLOWLIST_PACKAGES;
        }

        BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder();
        for (String item : allowlistPackages) {
            builder.allowIfSubType(item);
        }
        return builder.build();
    }
}
