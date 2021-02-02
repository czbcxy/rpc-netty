package org.rpc.simple.server.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * Hello world!
 */

@Data
public class PorticoMetaData implements Serializable {

    private String className;
    private String methodName;
    private Class<?>[] params;
    private Object[] values;
}
