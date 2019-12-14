package org.rpc.simple.provider.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import rog.rpc.simple.registory.core.AbstractApplicationContent;
import rog.rpc.simple.registory.core.AbstractRegistryFactory;
import rog.rpc.simple.registory.core.RegisotyRedis;
import rog.rpc.simple.registory.core.RegisotyZookeeper;

@Slf4j
public class RpcProvider extends AbstractApplicationContent {
    public static AbstractRegistryFactory registryFactory = null;

    public RpcProvider(int port) {
        providerInit();
        if (port != 0) {
            super.port = port;
        } else {
            super.port = Integer.parseInt(String.valueOf(config.getProperty("netty.server.port")));
        }
        doStartRegistoryLogger();
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGourp = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workGourp).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pip = socketChannel.pipeline();
                pip.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pip.addLast(new LengthFieldPrepender(4));

                pip.addLast("encoder", new ObjectEncoder());
                pip.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                pip.addLast(new RpcProviderHandler());
            }
        }).option(ChannelOption.SO_BACKLOG, 128).option(ChannelOption.SO_KEEPALIVE, true);

        try {
            ChannelFuture future = server.bind(super.port).sync();
            log.info("Netty server start successful . port = {}", super.port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Netty server start error : {}", e.getCause().getMessage());
        }

    }

    private void doStartRegistoryLogger() {
        if (config.containsKey("redis.host")) {
            registryFactory = new RegisotyRedis();
        } else if (config.containsKey("zookeeper.host")) {
            registryFactory = new RegisotyZookeeper();
        }
        if (registryFactory == null) {
            log.error("The registered factory is not configured ");
            throw new RuntimeException("The registered factory is not configured");
        }
        doStartRegistoryLogger(registryFactory);
    }


}
