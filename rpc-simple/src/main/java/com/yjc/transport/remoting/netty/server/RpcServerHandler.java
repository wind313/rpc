package com.yjc.transport.remoting.netty.server;

import com.yjc.enums.RpcResponseCodeEnum;
import com.yjc.factory.SingletonFactory;
import com.yjc.transport.constants.RpcConstants;
import com.yjc.enums.CompressTypeEnum;
import com.yjc.enums.SerializationTypeEnum;
import com.yjc.transport.handler.RpcRequestHandler;
import com.yjc.transport.pojo.RpcMessage;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.pojo.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;
    public RpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setSerialize(SerializationTypeEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info("得到结果:{}", result);

                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse response = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(response);
                    } else {
                        RpcResponse response = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(response);
                        log.error("当前不能写，消息失效");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.READER_IDLE){
                log.info("读空闲");
                ctx.close();
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端异常!");
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }

}
