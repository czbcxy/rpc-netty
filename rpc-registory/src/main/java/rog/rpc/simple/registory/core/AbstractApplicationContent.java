package rog.rpc.simple.registory.core;

import lombok.extern.slf4j.Slf4j;
import rog.rpc.simple.registory.core.AbstractRegistryFactory;
import rog.rpc.simple.registory.core.StringUtils;

import javax.sound.sampled.Port;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

/**
 * Hello world!
 */
@Slf4j
public abstract class AbstractApplicationContent {
    private static final Object lock = new Object();
    public static int port;
    private static final String application = "Application.properties";
    protected static final Properties config = new Properties();
    private static final Set<String> clazzNames = new LinkedHashSet<String>();
    public AbstractRegistryFactory registryFactory = null;

    public void providerInit() {
        synchronized (lock) {
            doLoadProperties();
            doScanServerClazz(config.getProperty("rpc.server"));
        }
    }

    public void consumerInit() {
        synchronized (lock) {
            doLoadProperties();
        }
    }

    protected void doRegistory() {
        if (StringUtils.isEmpty(clazzNames)) {
            log.debug("clazzNames is not null: {}", clazzNames);
        }
        try {
            for (String clazzName : clazzNames) {
                Class<?> clazz = Class.forName(clazzName);
                Class<?>[] interfaces = clazz.getInterfaces();
                if (interfaces.length <= 0) {
                    continue;
                }
                String name = interfaces[0].getName();
                String ip = InetAddress.getLocalHost().getHostName() + ":" + port;
                registryFactory.push(name, ip, clazzName);
            }
        } catch (Exception e) {
            log.error("registory failure {}", e);
            e.printStackTrace();
        }
    }

    protected void doScanServerClazz(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            log.debug("rpc.server is not setting: {}", packageName);
        }
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File files = new File(url.getFile());
        for (File file : files.listFiles()) {
            if (file.isDirectory()) {
                doScanServerClazz(packageName + "." + file.getName());
            }
            clazzNames.add(packageName + "." + file.getName().replace(".class", ""));
        }
    }

    public void doLoadProperties() {
        InputStream io = null;
        try {
            io = this.getClass().getClassLoader().getResourceAsStream(application);
            config.load(io);
        } catch (Exception e) {
            log.error("cofig init error : {}", e.getCause().getMessage());
        } finally {
            try {
                if (io != null) {
                    io.close();
                }
            } catch (IOException e) {
                log.error("{}", e);
            }
        }
    }

    protected void doStartRegistoryLogger(AbstractRegistryFactory registryFactory) {
        registryFactory.doStartRegistoryLogger(config);
        this.registryFactory = registryFactory;
        this.doRegistory();
    }

}
