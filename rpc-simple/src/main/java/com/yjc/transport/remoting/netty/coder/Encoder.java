package com.yjc.transport.remoting.netty.coder;

import com.yjc.transport.compress.Compress;
import com.yjc.transport.constants.RpcConstants;
import com.yjc.enums.CompressTypeEnum;
import com.yjc.enums.SerializationTypeEnum;
import com.yjc.extension.ExtensionLoader;
import com.yjc.transport.pojo.RpcMessage;
import com.yjc.transport.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
public class Encoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        try {
            byteBuf.writeBytes(RpcConstants.MAGIC_NUMBER);
            byteBuf.writeByte(RpcConstants.VERSION);
            byteBuf.writerIndex(byteBuf.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            byteBuf.writeByte(messageType);
            byteBuf.writeByte(rpcMessage.getSerialize());
            byteBuf.writeByte(rpcMessage.getCompress());
            byteBuf.writeInt(atomicInteger.getAndIncrement());
            int length = RpcConstants.HEAD_LENGTH;
            byte[] bodyBytes = null;
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                String SerializeName = SerializationTypeEnum.getName(rpcMessage.getSerialize());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(SerializeName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                length += bodyBytes.length;
            }

            if (bodyBytes != null) {
                byteBuf.writeBytes(bodyBytes);
            }
            int writeIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(writeIndex - length + RpcConstants.MAGIC_NUMBER.length + 1);
            byteBuf.writeInt(length);
            byteBuf.writerIndex(writeIndex);
        }catch (Exception e){
            log.error("编码失败!",e);
        }
    }


}
