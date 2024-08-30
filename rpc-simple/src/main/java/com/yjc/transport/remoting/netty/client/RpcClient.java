package com.yjc.transport.remoting.netty.client;

import com.yjc.transport.constants.RpcConstants;
import com.yjc.enums.CompressTypeEnum;
import com.yjc.enums.SerializationTypeEnum;
import com.yjc.enums.ServiceEnum;
import com.yjc.extension.ExtensionLoader;
import com.yjc.factory.SingletonFactory;
import com.yjc.transport.pojo.RpcResponse;
import com.yjc.transport.remoting.RpcSender;
import com.yjc.transport.remoting.netty.coder.Decoder;
import com.yjc.transport.remoting.netty.coder.Encoder;
import com.yjc.transport.pojo.RpcMessage;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.registry.ServiceDiscovery;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.xml.ws.Response;

@Slf4j
public class RpcClient implements RpcSender {
    private final ServiceDiscovery serviceDiscovery;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup workerGroup;
    private final UnprocessedRequest unprocessedRequest;
    public RpcClient() {
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new Encoder());
                        pipeline.addLast(new Decoder());
                        pipeline.addLast(new RpcClientHandler());
                    }
                });

        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceEnum.ZK.getName());
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        this.unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
    }
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端[{}]连接成功", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }
    @Override
    public RpcResponse send(RpcRequest rpcRequest) throws ExecutionException, InterruptedException {
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            unprocessedRequest.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder()
                    .data(rpcRequest)
                    .serialize(SerializationTypeEnum.KRYO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("客户端[{}]发送消息成功", inetSocketAddress.toString());
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("客户端[{}]发送消息失败", inetSocketAddress.toString());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture.get();
    }
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }
}
