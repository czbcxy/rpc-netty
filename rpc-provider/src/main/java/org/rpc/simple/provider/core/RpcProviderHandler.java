package org.rpc.simple.provider.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.rpc.simple.protocol.PortocolMetaData;

import java.lang.reflect.Method;

import static org.rpc.simple.provider.core.RpcProvider.registryFactory;

public class RpcProviderHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        PortocolMetaData metaData = (PortocolMetaData) msg;
        if (registryFactory.contantKey(metaData.getClassName())) {
            String server = registryFactory.pullServer(metaData.getClassName());
            Class<?> clazz = Class.forName(server);
            Method method = clazz.getMethod(metaData.getMethodName(), metaData.getParams());
            result = method.invoke(clazz.newInstance(), metaData.getValues());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(cause.getMessage());
    }
}
