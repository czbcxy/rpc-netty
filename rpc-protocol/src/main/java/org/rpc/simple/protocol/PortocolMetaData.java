package org.rpc.simple.protocol;

import lombok.Data;
import java.io.Serializable;

/**
 * Hello world!
 */

@Data
public class PortocolMetaData implements Serializable {

    private String className;
    private String methodName;
    private Class<?>[] params;
    private Object[] values;
}
