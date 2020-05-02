package rog.rpc.simple.registory.core;

import lombok.extern.slf4j.Slf4j;

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
    private static final Object                  PROVIDER_LOCK   = new Object();
    private static final Object                  CONSUMER_LOCK   = new Object();
    private static final String                  application     = "Application.properties";
    private static final Properties              config          = new Properties();
    private static final Set<String>             clazzNames      = new LinkedHashSet<String>();
    public static final  String                  RPC_SERVICE     = "rpc.server";
    public static        int                     port;
    public               AbstractRegistryFactory registryFactory = null;

    public void providerInit() {
        synchronized (PROVIDER_LOCK) {
            consumerInit();
            doScanServerClazz(config.getProperty(RPC_SERVICE));
        }
    }

    public void consumerInit() {
        if (config.size() <= 0) {
            synchronized (CONSUMER_LOCK) {
                if (config.size() <= 0) {
                    doLoadProperties();
                }
            }
        }
    }

    protected void doRegistory() {
        if (StringUtils.isEmpty(clazzNames)) {
            log.debug("clazzNames is not null: {}", clazzNames);
        }
        try {
            for (String clazzName : clazzNames) {
                Class<?>   clazz      = Class.forName(clazzName);
                Class<?>[] interfaces = clazz.getInterfaces();
                if (interfaces.length <= 0) {
                    continue;
                }
                String name = interfaces[0].getName();
                String ip   = InetAddress.getLocalHost().getHostName() + StringUtils.SEPARATED + port;
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
        URL  url   = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File files = new File(url.getFile());
        for (File file : files.listFiles()) {
            if (file.isDirectory()) {
                doScanServerClazz(packageName + "." + file.getName());
            }
            clazzNames.add(packageName + StringUtils.POINT + file.getName().replace(StringUtils.CLASS, StringUtils.EMPTY));
        }
    }

    public void doLoadProperties() {
        try (InputStream io = this.getClass().getClassLoader().getResourceAsStream(application)) {
            config.load(io);
        } catch (Exception e) {
            log.error("cofig init error : {}", e.getCause().getMessage());
        }
    }

    protected void doStartRegistoryLogger(AbstractRegistryFactory registryFactory) {
        registryFactory.doStartRegistoryLogger(config);
        this.registryFactory = registryFactory;
        this.doRegistory();
    }

}
