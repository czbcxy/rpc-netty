package org.rpc.simple.consumer.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import org.rpc.simple.protocol.PortocolMetaData;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.rpc.simple.consumer.core.RpcConsumer.registryFactory;

@Slf4j
public class proxyInstance implements InvocationHandler {
    private Class<?> clazz;

    public proxyInstance(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(clazz, args);
        }
        return rpcInvoker(proxy, method, args);
    }

    private Object rpcInvoker(Object proxy, Method method, Object[] args) {
        //封装传输协议
        PortocolMetaData metaData = new PortocolMetaData();
        metaData.setClassName(clazz.getName());
        metaData.setMethodName(method.getName());
        metaData.setParams(method.getParameterTypes());
        metaData.setValues(args);
        RpcConsumerHandler handler = new RpcConsumerHandler();
        transmit(metaData, handler);
        return handler.getResponse();
    }

    private void transmit(PortocolMetaData metaData, final RpcConsumerHandler handler) {
        EventLoopGroup workGroup = null;
        try {
            workGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pip = socketChannel.pipeline();
                    pip.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pip.addLast(new LengthFieldPrepender(4));
                    pip.addLast("encoder", new ObjectEncoder());
                    pip.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
                    pip.addLast(handler);
                }
            });
            String[] split = getHost();
            ChannelFuture future = bootstrap.connect(split[0], Integer.parseInt(split[1])).sync();
            future.channel().writeAndFlush(metaData).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("consumer error {}", e.getCause().getMessage());
            e.printStackTrace();
        } finally {
            if (workGroup != null) {
                workGroup.shutdownGracefully();
            }
        }
    }

    private String[] getHost() {
        Map<String, String> mps = registryFactory.pull(clazz.getName());
        List list = new ArrayList();
        for (String key : mps.keySet()) {
            list.add(key);
        }
        Random random = new Random();
        int i = random.nextInt(list.size());
        String ipPort = (String) list.get(i);
        String addr = mps.get(ipPort);
        return addr.split(":");
    }
}
