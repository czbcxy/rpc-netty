package org.rpc.simple.server.consumer;

import lombok.extern.slf4j.Slf4j;
import org.rpc.simple.server.regitry.AbstractApplicationContent;

import java.io.IOException;
import java.lang.reflect.Proxy;

/**
 * Hello world!
 */
@Slf4j
public class RpcConsumer extends AbstractApplicationContent {

    public <T> T create(final Class<?> clazz) throws IOException {
        synchronized (CONSUMER_LOCK) {
            registryFactory();
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                    new proxyInstance(clazz));
        }
    }


}
