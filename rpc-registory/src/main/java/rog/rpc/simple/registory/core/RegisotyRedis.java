package rog.rpc.simple.registory.core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * JedisPool工具类
 *   加载配置文件，配置连接池参数
 *   提供获取连接的方法
 */
public class RegisotyRedis implements AbstractRegistryFactory {
    private static Object redisLock = new Object();
    private static JedisPool jedisPool;
    private static final String prefix = "hash_";
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    public void doStartRegistoryLogger(Properties properties) {
        synchronized (redisLock) {
            if (jedisPool == null) {
                synchronized (redisLock) {
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(Integer.parseInt(properties.getProperty("redis.maxTotal")));
                    config.setMaxIdle(Integer.parseInt(properties.getProperty("redis.maxIdle")));
                    jedisPool = new JedisPool(config, properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
                }
            }
        }
    }

    @Override
    public void push(String name, String ip, String clazzName) {
        Jedis jedis = null;
        try {
            jedis = jedisClient();
            int i = ATOMIC_INTEGER.addAndGet(1);
            jedis.hset(prefix + name, String.valueOf(i), ip);
            jedis.set(name, clazzName);
        } finally {
            if (jedis != null) {
                jedisClose(jedis);
            }
        }
    }

    @Override
    public Map<String, String> pull(String name) {
        Jedis jedis = null;
        try {
            jedis = jedisClient();
            Map<String, String> map = jedis.hgetAll(prefix + name);
            return map;
        } finally {
            if (jedis != null) {
                jedisClose(jedis);
            }
        }
    }

    @Override
    public String pullServer(String name) {
        Jedis jedis = null;
        try {
            jedis = jedisClient();
            return jedis.get(name);
        } finally {
            if (jedis != null) {
                jedisClose(jedis);
            }
        }
    }

    @Override
    public boolean contantKey(String name) {
        Jedis jedis = null;
        try {
            jedis = jedisClient();
            return jedis.exists(name);
        } finally {
            if (jedis != null) {
                jedisClose(jedis);
            }
        }
    }

    private static void jedisClose(Jedis jedis) {
        jedis.close();
    }

    private static Jedis jedisClient() {
        return jedisPool.getResource();
    }
}
