package org.rpc.simple.server.provider;

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
import org.rpc.simple.server.regitry.AbstractApplicationContent;

import java.io.IOException;

@Slf4j
public class RpcProvider extends AbstractApplicationContent {

    public RpcProvider(int port) throws IOException, ClassNotFoundException {
        synchronized (PROVIDER_LOCK) {
            AbstractApplicationContent.port = port;
            registryServer();
            registryFactory();
            doRegistry();
        }
    }

    public void registryServer() {
        port = port == 0 ? Integer.parseInt(String.valueOf(config.getProperty("netty.server.port"))) : port;
        doScanServerClazz(config.getProperty(RPC_SERVICE));
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workGroup);
        server.channel(NioServerSocketChannel.class);
        server.option(ChannelOption.SO_BACKLOG, 128).option(ChannelOption.SO_KEEPALIVE, true);
        server.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pip = socketChannel.pipeline();
                pip.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pip.addLast(new LengthFieldPrepender(4));

                pip.addLast("encoder", new ObjectEncoder());
                pip.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                pip.addLast(new RpcProviderHandler());
            }
        });
        ChannelFuture future = server.bind(super.port).sync();
        log.info("Netty server start successful . port = {}", super.port);
        future.channel().closeFuture().sync();
    }

}
