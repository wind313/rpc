package com.yjc.transport.remoting.netty.coder;

import com.yjc.enums.CompressTypeEnum;
import com.yjc.enums.SerializationTypeEnum;
import com.yjc.extension.ExtensionLoader;
import com.yjc.transport.compress.Compress;
import com.yjc.transport.constants.RpcConstants;
import com.yjc.transport.pojo.RpcMessage;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.pojo.RpcResponse;
import com.yjc.transport.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class Decoder extends LengthFieldBasedFrameDecoder {
    public Decoder() {
        super(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decode;
            if (frame.readableBytes() >= RpcConstants.HEAD_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("解码错误!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decode;
    }

    private Object decodeFrame(ByteBuf frame) {
        checkMagicNumber(frame);
        checkVersion(frame);
        int length = frame.readInt();
        byte messageType = frame.readByte();
        byte serializerType = frame.readByte();
        byte compressType = frame.readByte();
        int requestId = frame.readInt();
        RpcMessage rpcMessage = RpcMessage.builder().messageType(messageType).serialize(serializerType).requestId(requestId).build();
        if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = length - RpcConstants.HEAD_LENGTH;
        if(bodyLength > 0){
            byte[] bytes = new byte[bodyLength];
            frame.readBytes(bytes);
            // 解析数据
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            bytes = compress.decompress(bytes);
            String serializeName = SerializationTypeEnum.getName(serializerType);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializeName);
            if(messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest request = serializer.deserialize(bytes, RpcRequest.class);
                rpcMessage.setData(request);
            }else {
                RpcResponse response = serializer.deserialize(bytes, RpcResponse.class);
                rpcMessage.setData(response);
            }
        }
        return rpcMessage;
    }

    private void checkMagicNumber(ByteBuf frame) {
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        frame.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new RuntimeException("未知的魔数：" + Arrays.toString(tmp));
            }
        }
    }

    private void checkVersion(ByteBuf frame) {
        byte version = frame.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("版本不兼容：" + version);
        }
    }
}
