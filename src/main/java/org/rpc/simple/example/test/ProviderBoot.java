package org.rpc.simple.example.test;

import org.rpc.simple.server.provider.RpcProvider;
import java.io.IOException;

public class ProviderBoot {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        RpcProvider rpcProvider = new RpcProvider(8089);
        rpcProvider.start();
    }
}
