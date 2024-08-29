package com.yjc.transport.remoting.netty.server;

import com.yjc.config.RpcConfig;
import com.yjc.factory.SingletonFactory;
import com.yjc.provider.ServiceProvider;
import com.yjc.provider.impl.ZKServiceProvider;
import com.yjc.transport.shutdownHook.ShutdownHook;
import com.yjc.transport.remoting.netty.coder.Decoder;
import com.yjc.transport.remoting.netty.coder.Encoder;
import com.yjc.util.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class RpcServer {
    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZKServiceProvider.class);

    public void registerService(RpcConfig rpcConfig) {
        serviceProvider.publishService(rpcConfig);
    }

    @SneakyThrows
    public void run() {
        ShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2, ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false));
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new Encoder());
                            pipeline.addLast(new Decoder());
                            pipeline.addLast(serviceHandlerGroup,new RpcServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            ChannelFuture f = b.bind(host,PORT).sync(); // (7)

            f.channel().closeFuture().sync();

        } catch (InterruptedException e){
            log.info("启动服务异常：",e);
        }
        finally {
            log.error("关闭bossGroup和workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }

}
