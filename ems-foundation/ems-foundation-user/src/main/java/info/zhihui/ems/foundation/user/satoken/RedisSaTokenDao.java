package info.zhihui.ems.foundation.user.satoken;

import cn.dev33.satoken.dao.auto.SaTokenDaoBySessionFollowObject;
import cn.dev33.satoken.util.SaFoxUtil;
import info.zhihui.ems.components.redis.utils.RedisUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sa-Token持久层接口
 * <p>
 * 使用redis存储
 *
 * @author jerryxiaosa
 */
public class RedisSaTokenDao implements SaTokenDaoBySessionFollowObject {

    @Override
    public String get(String key) {
        Object o = RedisUtil.getCacheObject(key);
        return (String) o;
    }

    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            RedisUtil.setCacheObject(key, value);
        } else {
            RedisUtil.setCacheObject(key, value, Duration.ofSeconds(timeout));
        }
    }

    @Override
    public void update(String key, String value) {
        if (RedisUtil.hasKey(key)) {
            RedisUtil.setCacheObject(key, value, true);
        }
    }

    @Override
    public void delete(String key) {
        RedisUtil.deleteObject(key);
    }

    @Override
    public long getTimeout(String key) {
        long timeout = RedisUtil.getTimeToLive(key);
        return timeout < 0 ? timeout : timeout / 1000;
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        RedisUtil.expire(key, Duration.ofSeconds(timeout));
    }

    @Override
    public Object getObject(String key) {
        return RedisUtil.getCacheObject(key);
    }

    @Override
    public <T> T getObject(String key, Class<T> classType) {
        Object o = RedisUtil.getCacheObject(key);
        if (o == null) {
            return null;
        }

        if (classType.isInstance(o)) {
            return classType.cast(o);
        } else {
            throw new ClassCastException("Cached object cannot be cast to " + classType.getName());
        }
    }


    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            RedisUtil.setCacheObject(key, object);
        } else {
            RedisUtil.setCacheObject(key, object, Duration.ofSeconds(timeout));
        }
    }

    @Override
    public void updateObject(String key, Object object) {
        if (RedisUtil.hasKey(key)) {
            RedisUtil.setCacheObject(key, object, true);
        }
    }

    @Override
    public void deleteObject(String key) {
        RedisUtil.deleteObject(key);
    }

    @Override
    public long getObjectTimeout(String key) {
        long timeout = RedisUtil.getTimeToLive(key);
        return timeout < 0 ? timeout : timeout / 1000;
    }

    @Override
    public void updateObjectTimeout(String key, long timeout) {
        RedisUtil.expire(key, Duration.ofSeconds(timeout));
    }

    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        String keyStr = prefix + "*" + keyword + "*";
        Collection<String> keys = RedisUtil.keys(keyStr);
        List<String> list = new ArrayList<>(keys);
        return SaFoxUtil.searchList(list, start, size, sortType);
    }
}