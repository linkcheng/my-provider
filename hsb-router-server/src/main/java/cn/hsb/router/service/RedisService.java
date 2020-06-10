package cn.hsb.router.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * String 类型缓存获取
     */
    public String get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * String 普通缓存获取
     */
    public String get(String key, String defaultValue) {
        String value = key == null ? defaultValue : redisTemplate.opsForValue().get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * String 多key缓存获取
     */
    public List<String> multiGet(Collection<String> keys) {
        return keys == null ? Lists.newArrayList() : redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * set 类型缓存获取
     */
    public Set<String> members(String key) {
        return key == null ? null : redisTemplate.opsForSet().members(key);
    }

    /**
     * String 普通缓存放入
     */
    public boolean set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    /**
     * String 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, String value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    /**
     * String 多 key 缓存放入
     */
    public boolean multiSet(Map<String, String> map) {
        try {
            redisTemplate.opsForValue().multiSet(map);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    /**
     * set 类型缓存放入
     */
    public long sAdd(String key, String ... value) {
        try {
            return redisTemplate.opsForSet().add(key, value);
        } catch (Exception e) {
            log.error(e.toString());
            return 0;
        }
    }

    /**
     * set 类型缓存放入
     */
    public long sRem(String key, Object... value) {
        try {
            return redisTemplate.opsForSet().remove(key, value);
        } catch (Exception e) {
            log.error(e.toString());
            return 0;
        }
    }

    /**
     * 删除key
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除多个key
     */
    public void deleteKey(String... keys) {
        Set<String> kSet = Sets.newHashSet(keys);
        redisTemplate.delete(kSet);
    }

    /**
     * 删除Key的集合
     */
    public void deleteKey(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 设置key的生命周期
     */
    public void expireKey(String key, long time, TimeUnit timeUnit) {
        redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 指定key在指定的日期过期
     */
    public void expireKeyAt(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    /**
     * 查询key的生命周期
     */
    public long getKeyExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 将key设置为永久有效
     */
    public void persistKey(String key) {
        redisTemplate.persist(key);
    }
}
