package org.rpc.simple.provider.Test;

import org.rpc.simple.provider.core.RpcProvider;

public class Main {

    public static void main(String[] args) {
        new RpcProvider(8089).start();
    }
}
