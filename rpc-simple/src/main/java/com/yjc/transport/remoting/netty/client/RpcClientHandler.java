package com.yjc.transport.remoting.netty.client;

import com.yjc.enums.CompressTypeEnum;
import com.yjc.enums.SerializationTypeEnum;
import com.yjc.factory.SingletonFactory;
import com.yjc.transport.constants.RpcConstants;
import com.yjc.transport.pojo.RpcMessage;
import com.yjc.transport.pojo.RpcResponse;
import com.yjc.transport.serialize.Serializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequest unprocessedRequest;

    private final RpcClient rpcClient;

    public RpcClientHandler() {
        this.unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
        this.rpcClient = SingletonFactory.getInstance(RpcClient.class);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                byte messageType = ((RpcMessage) msg).getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("心跳"+(RpcMessage)msg);
                } else if(messageType == RpcConstants.RESPONSE_TYPE){
                    RpcResponse rpcResponse = (RpcResponse)((RpcMessage) msg).getData();
                    unprocessedRequest.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent)evt).state();
            if(state == IdleState.WRITER_IDLE){
                log.info("发送心跳[{}]", ctx.channel().remoteAddress());
                Channel channel = rpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setSerialize(SerializationTypeEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PONG);
                ctx.writeAndFlush(RpcMessage.builder().messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE).data(RpcConstants.PING).build()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端异常:",cause);
        cause.printStackTrace();
        ctx.fireExceptionCaught(cause);
    }
}
