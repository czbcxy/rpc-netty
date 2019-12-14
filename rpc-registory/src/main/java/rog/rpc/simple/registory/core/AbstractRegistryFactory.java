package rog.rpc.simple.registory.core;

import java.util.Map;
import java.util.Properties;

public interface AbstractRegistryFactory {

    void doStartRegistoryLogger(Properties config);

    void push(String name, String ip, String clazzName);

    Map<String, String> pull(String name);

    String pullServer(String name);

    boolean contantKey(String name);
}
