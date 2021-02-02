package org.rpc.simple.server.regitry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.Properties;

/*
 * JedisPool工具类
 *   加载配置文件，配置连接池参数
 *   提供获取连接的方法
 */
public class RegisotyZookeeper implements AbstractRegistryFactory {
    private static JedisPool jedisPool;

    public void doStartRegistryFactory(Properties properties) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.parseInt(properties.getProperty("redis.maxTotal")));
        config.setMaxIdle(Integer.parseInt(properties.getProperty("redis.maxIdle")));
        jedisPool = new JedisPool(config, properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
    }

    @Override
    public void push(String name, String ip, String clazzName) {

    }

    @Override
    public Map<String, String> pull(String name) {
        return null;
    }

    @Override
    public String pullServer(String name) {
        return null;
    }

    @Override
    public boolean contentKey(String name) {
        return false;
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
