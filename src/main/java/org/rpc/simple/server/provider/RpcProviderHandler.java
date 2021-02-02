package org.rpc.simple.server.provider;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.rpc.simple.server.protocol.PorticoMetaData;
import org.rpc.simple.server.regitry.AbstractApplicationContent;

import java.lang.reflect.Method;

@Slf4j
public class RpcProviderHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        PorticoMetaData metaData = (PorticoMetaData) msg;
        if (AbstractApplicationContent.singletonObjectsMap.containsKey(metaData.getClassName())) {
            Class<?> clazz = Class.forName(AbstractApplicationContent.singletonObjectsMap.get(metaData.getClassName()));
            Method method = clazz.getMethod(metaData.getMethodName(), metaData.getParams());
            result = method.invoke(clazz.newInstance(), metaData.getValues());
            log.info("接收请求 {}，返回结果 {}", msg, result);
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Netty server error {}", cause.getMessage());
    }
}
