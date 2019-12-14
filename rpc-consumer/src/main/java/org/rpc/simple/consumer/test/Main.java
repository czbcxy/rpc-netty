package org.rpc.simple.consumer.test;

import org.rpc.simple.api.AppApi;
import org.rpc.simple.consumer.core.RpcConsumer;

public class Main {
    public static void main(String[] args) {
        for (int i = 0;i < 100 ; i++) {
            AppApi api = new RpcConsumer().create(AppApi.class);
            String world = api.doAction("World");
            System.out.println(world);
        }
    }
}
