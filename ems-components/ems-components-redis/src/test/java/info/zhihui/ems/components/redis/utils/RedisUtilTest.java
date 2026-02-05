package info.zhihui.ems.components.redis.utils;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RedisUtilTest {

    @AfterEach
    void tearDown() throws Exception {
        resetClient();
    }

    @Test
    @DisplayName("getClient-首次调用懒加载并缓存")
    void testGetClient_LazyInit() throws Exception {
        resetClient();
        RedissonClient redissonClient = mock(RedissonClient.class);
        try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class)) {
            springMock.when(() -> SpringUtil.getBean(RedissonClient.class)).thenReturn(redissonClient);

            RedissonClient first = RedisUtil.getClient();
            RedissonClient second = RedisUtil.getClient();

            assertSame(redissonClient, first);
            assertSame(redissonClient, second);
            springMock.verify(() -> SpringUtil.getBean(RedissonClient.class), times(1));
        }
    }

    @Test
    @DisplayName("getClient-缺少RedissonClient时抛异常")
    void testGetClient_MissingBean() throws Exception {
        resetClient();
        try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class)) {
            springMock.when(() -> SpringUtil.getBean(RedissonClient.class)).thenReturn(null);

            assertThrows(IllegalStateException.class, RedisUtil::getClient);
        }
    }

    @Test
    @DisplayName("setCacheObject-保留TTL且key不存在时走普通set")
    void testSetCacheObject_SaveTtl_KeyNotExist() throws Exception {
        String key = "k1";
        String value = "v1";
        RedissonClient redissonClient = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);

        when(redissonClient.getBucket(key)).thenReturn(bucket);
        doThrow(new RuntimeException("fail")).when(bucket).setAndKeepTTL(value);
        when(bucket.remainTimeToLive()).thenReturn(-2L);

        setClient(redissonClient);
        RedisUtil.setCacheObject(key, value, true);

        verify(bucket, times(1)).set(value);
        verify(redissonClient, times(2)).getBucket(key);
    }

    @Test
    @DisplayName("setCacheObject-保留TTL且无过期时走普通set")
    void testSetCacheObject_SaveTtl_NoExpire() throws Exception {
        String key = "k2";
        String value = "v2";
        RedissonClient redissonClient = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);

        when(redissonClient.getBucket(key)).thenReturn(bucket);
        doThrow(new RuntimeException("fail")).when(bucket).setAndKeepTTL(value);
        when(bucket.remainTimeToLive()).thenReturn(-1L);

        setClient(redissonClient);
        RedisUtil.setCacheObject(key, value, true);

        verify(bucket, times(1)).set(value);
        verify(redissonClient, times(2)).getBucket(key);
    }

    @Test
    @DisplayName("setCacheObject-保留TTL且存在过期时走TTL写入")
    void testSetCacheObject_SaveTtl_WithExpire() throws Exception {
        String key = "k3";
        String value = "v3";
        RedissonClient redissonClient = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);
        Duration ttl = Duration.ofMillis(5000L);
        RBatch batch = mock(RBatch.class);
        RBucketAsync<Object> bucketAsync = mock(RBucketAsync.class);

        when(redissonClient.getBucket(key)).thenReturn(bucket);
        when(redissonClient.createBatch()).thenReturn(batch);
        when(batch.getBucket(key)).thenReturn(bucketAsync);
        doThrow(new RuntimeException("fail")).when(bucket).setAndKeepTTL(value);
        when(bucket.remainTimeToLive()).thenReturn(5000L);

        setClient(redissonClient);
        RedisUtil.setCacheObject(key, value, true);

        verify(bucketAsync, times(1)).setAsync(value);
        verify(bucketAsync, times(1)).expireAsync(ttl);
        verify(batch, times(1)).execute();
        verify(bucket, never()).set(value);
    }

    private void resetClient() throws Exception {
        Field field = RedisUtil.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(null, null);
    }

    private void setClient(RedissonClient redissonClient) throws Exception {
        Field field = RedisUtil.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(null, redissonClient);
    }
}
