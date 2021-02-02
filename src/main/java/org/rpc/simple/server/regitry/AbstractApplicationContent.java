package org.rpc.simple.server.regitry;

import lombok.extern.slf4j.Slf4j;
import org.rpc.simple.server.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hello world!
 */
@Slf4j
public abstract class AbstractApplicationContent {
    protected static final Object PROVIDER_LOCK = new Object();
    protected static final Object CONSUMER_LOCK = new Object();
    protected static final String application = "application.properties";
    protected static final Set<String> clazzNames = new LinkedHashSet<String>();
    public static final Properties config = new Properties();
    public static final String RPC_SERVICE = "rpc.server";
    public static Integer port = 0;
    public static AbstractRegistryFactory registryFactory = null;
    public static final ConcurrentHashMap<String, String> singletonObjectsMap = new ConcurrentHashMap<>(16);

    public AbstractApplicationContent() {
        if (config.size() <= 0) {
            try {
                doLoadProperties();
                port = port == 0 ? Integer.parseInt(String.valueOf(config.getProperty("netty.server.port"))) : port;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void doLoadProperties() throws IOException {
        try (InputStream io = this.getClass().getClassLoader().getResourceAsStream(application)) {
            config.load(io);
        } catch (Exception e) {
            log.error("config init error : {}", e.getCause().getMessage());
            throw e;
        }
    }

    protected void registryFactory() {
        if (config.containsKey("redis.host")) {
            registryFactory = new RegistryRedis();
        } else if (config.containsKey("zookeeper.host")) {
            registryFactory = new RegisotyZookeeper();
        }
        if (registryFactory == null) {
            log.error("The registered factory is not configured ");
            throw new RuntimeException("The registered factory is not configured");
        }
        registryFactory.doStartRegistryFactory(config);
    }


    protected void doRegistry() throws UnknownHostException, ClassNotFoundException {
        if (StringUtils.isEmpty(clazzNames)) {
            log.debug("clazzNames is not null: {}", clazzNames);
            return;
        }
        for (String clazzName : clazzNames) {
            Class<?> clazz = Class.forName(clazzName);
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length <= 0) {
                continue;
            }
            String name = interfaces[0].getName();
            String ip = InetAddress.getLocalHost().getHostName() + StringUtils.SEPARATED + port;
            registryFactory.push(name, ip, clazzName);
            singletonObjectsMap.put(name, clazzName);
        }
    }

    protected void doScanServerClazz(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            log.debug("rpc.server is not setting: {}", packageName);
            return;
        }
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll(StringUtils.POINT, StringUtils.FOLDER_SEPARATOR));
        assert url != null;
        File files = new File(url.getFile());
        for (File file : Objects.requireNonNull(files.listFiles())) {
            if (file.isDirectory()) {
                doScanServerClazz(packageName + StringUtils.POINT + file.getName());
            }
            clazzNames.add(packageName + StringUtils.EXTENSION_SEPARATOR + file.getName().replace(StringUtils.CLASS, StringUtils.EMPTY));
        }
    }

}
