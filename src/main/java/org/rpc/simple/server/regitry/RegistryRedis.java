package org.rpc.simple.server.regitry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * JedisPool工具类
 *   加载配置文件，配置连接池参数
 *   提供获取连接的方法
 */
public class RegistryRedis implements AbstractRegistryFactory {
    private static Object redisLock = new Object();
    private static JedisPool jedisPool;
    private static final String prefix = "hash_";
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    public void doStartRegistryFactory(Properties properties) {
        synchronized (redisLock) {
            if (jedisPool == null) {
                synchronized (redisLock) {
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(Integer.parseInt(properties.getProperty("redis.maxTotal")));
                    config.setMaxIdle(Integer.parseInt(properties.getProperty("redis.maxIdle")));
                    String password = properties.getProperty("redis.password");
                    if ( password != null ) {
                        jedisPool = new JedisPool(config, properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")),3000,password,0);
                    }else {
                        jedisPool = new JedisPool(config, properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
                    }
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
//            jedis.set(name, clazzName);
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
            return jedis.hgetAll(prefix + name);
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
    public boolean contentKey(String name) {
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
