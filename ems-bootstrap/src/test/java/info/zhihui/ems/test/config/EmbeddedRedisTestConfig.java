package info.zhihui.ems.test.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import redis.embedded.RedisServer;
import redis.embedded.core.RedisServerBuilder;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 集成测试环境嵌入式 Redis 配置，避免 RedissonClient 初始化时依赖外部 Redis。
 */
@TestConfiguration
@Profile("integrationtest")
@Slf4j
public class EmbeddedRedisTestConfig {

    private static volatile RedisServer redisServer;
    private static volatile boolean shutdownHookRegistered;

    @Bean(destroyMethod = "stop")
    public RedisServer redisServer(Environment environment) throws IOException {
        synchronized (EmbeddedRedisTestConfig.class) {
            if (redisServer != null && redisServer.isActive()) {
                log.info("复用已启动的嵌入式 Redis，监听端口：{}", redisServer.ports());
                return redisServer;
            }

            String host = require(environment, "spring.data.redis.host");
            int port = parseInt(environment, "spring.data.redis.port");
            int database = parseInt(environment, "spring.data.redis.database");

            int databaseCount = Math.max(database + 1, 16);

            RedisServerBuilder builder = RedisServer.newRedisServer()
                    .bind(host)
                    .port(port)
                    .setting("databases " + databaseCount)
                    .setting("maxmemory 128M")
                    .soutListener(line -> log.debug("[embedded-redis] {}", line))
                    .serrListener(line -> log.error("[embedded-redis][stderr] {}", line));

            RedisServer server = builder.build();
            try {
                server.start();
                waitUntilReady(host, port, Duration.ofSeconds(5));
                log.info("嵌入式 Redis 启动完成，监听 {}:{}", host, port);
            } catch (RuntimeException | IOException ex) {
                try {
                    server.stop();
                } catch (Exception ignore) {
                    // ignore
                }
                throw new IllegalStateException("嵌入式 Redis 启动失败，host=" + host + ", port=" + port, ex);
            }

            redisServer = server;
            registerShutdownHook();
            return redisServer;
        }
    }

    /**
     * 确保 Redis 相关 Bean 在嵌入式 Redis 启动后再初始化。
     */
    @Bean
    public static BeanFactoryPostProcessor redisBeanDependsOnEmbeddedRedis() {
        return beanFactory -> {
            for (String beanName : new String[]{"redisson", "redisConnectionFactory", "redisTemplate", "stringRedisTemplate"}) {
                if (beanFactory.containsBeanDefinition(beanName)) {
                    BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
                    String[] dependsOn = definition.getDependsOn();
                    definition.setDependsOn(merge(dependsOn, "redisServer"));
                }
            }
        };
    }

    private String require(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("集成测试缺少必要的 Redis 配置：" + key);
        }
        return value;
    }

    private int parseInt(Environment environment, String key) {
        String value = require(environment, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("集成测试 Redis 配置格式错误：" + key + "=" + value, ex);
        }
    }

    private void waitUntilReady(String host, int port, Duration timeout) throws IOException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            try (Socket ignored = new Socket(host, port)) {
                return;
            } catch (IOException ex) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("等待嵌入式 Redis 启动被中断", ie);
                }
            }
        }
        throw new IOException("嵌入式 Redis 在限定时间内未对外提供服务，host=" + host + ", port=" + port);
    }

    private static String[] merge(String[] dependsOn, String beanName) {
        if (dependsOn == null || dependsOn.length == 0) {
            return new String[]{beanName};
        }
        for (String depend : dependsOn) {
            if (beanName.equals(depend)) {
                return dependsOn;
            }
        }
        String[] merged = new String[dependsOn.length + 1];
        System.arraycopy(dependsOn, 0, merged, 0, dependsOn.length);
        merged[dependsOn.length] = beanName;
        return merged;
    }

    private void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        shutdownHookRegistered = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (EmbeddedRedisTestConfig.class) {
                if (redisServer != null && redisServer.isActive()) {
                    try {
                        redisServer.stop();
                        log.info("嵌入式 Redis 已在 JVM 退出时停止");
                    } catch (Exception ex) {
                        log.warn("停止嵌入式 Redis 失败: {}", ex.getMessage());
                    }
                }
            }
        }, "embedded-redis-shutdown"));
    }
}
