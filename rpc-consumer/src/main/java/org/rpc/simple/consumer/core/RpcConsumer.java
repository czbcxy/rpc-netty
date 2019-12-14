package org.rpc.simple.consumer.core;

import lombok.extern.slf4j.Slf4j;
import rog.rpc.simple.registory.core.AbstractApplicationContent;
import rog.rpc.simple.registory.core.AbstractRegistryFactory;
import rog.rpc.simple.registory.core.RegisotyRedis;
import rog.rpc.simple.registory.core.RegisotyZookeeper;

import java.lang.reflect.Proxy;

/**
 * Hello world!
 */
@Slf4j
public class RpcConsumer extends AbstractApplicationContent {

    public static AbstractRegistryFactory registryFactory = null;

    public <T> T create(final Class<?> clazz) {
        consumerInit();
        doStartRegistoryLogger();
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new proxyInstance(clazz));
    }
    private void doStartRegistoryLogger() {
        if (config.containsKey("redis.host")) {
            registryFactory = new RegisotyRedis();
        } else if (config.containsKey("zookeeper.host")) {
            registryFactory = new RegisotyZookeeper();
        }
        if (registryFactory == null) {
            log.error("The registered factory is not configured ");
            throw new RuntimeException("The registered factory is not configured");
        }
        doStartRegistoryLogger(registryFactory);
    }
}
