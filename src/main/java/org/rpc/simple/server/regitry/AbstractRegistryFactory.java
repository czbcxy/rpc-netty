package org.rpc.simple.server.regitry;

import java.util.Map;
import java.util.Properties;

public interface AbstractRegistryFactory {

    void doStartRegistryFactory(Properties config);

    void push(String name, String ip, String clazzName);

    Map<String, String> pull(String name);

    String pullServer(String name);

    boolean contentKey(String name);
}
