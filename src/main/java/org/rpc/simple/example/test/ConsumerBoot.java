package org.rpc.simple.example.test;

import org.rpc.simple.example.api.AppApi;
import org.rpc.simple.example.api.AppApi2;
import org.rpc.simple.example.api.AppApi3;
import org.rpc.simple.server.consumer.RpcConsumer;

import java.io.IOException;

public class ConsumerBoot {

    public static void main(String[] args) throws IOException {
        for (int i = 0;i < 100 ; i++) {
            AppApi api = new RpcConsumer().create(AppApi.class);
            String world = api.doAction("World");
            System.out.println(world);
            AppApi2 api1 = new RpcConsumer().create(AppApi2.class);
            String world1 = api1.doAction("World");
            System.out.println(world1);
            AppApi3 api3 = new RpcConsumer().create(AppApi3.class);
            String world3 = api3.doAction("World");
            System.out.println(world3);
        }
    }

}
