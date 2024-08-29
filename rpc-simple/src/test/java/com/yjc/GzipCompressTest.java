package com.yjc;

import java.util.UUID;

import com.yjc.transport.compress.Compress;
import com.yjc.transport.compress.gzip.GzipCompress;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.serialize.kryo.KryoSerializer;

class GzipCompressTest {
    public static void main(String[] args) {
        Compress gzipCompress = new GzipCompress();
        RpcRequest rpcRequest = RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"sayhelooloo", "sayhelooloosayhelooloo"})
                .interfaceName("github.javaguide.HelloService")
                .parameterTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .name("group1")
                .build();
        KryoSerializer kryoSerializer = new KryoSerializer();
        byte[] rpcRequestBytes = kryoSerializer.serialize(rpcRequest);
        byte[] compressRpcRequestBytes = gzipCompress.compress(rpcRequestBytes);
        byte[] decompressRpcRequestBytes = gzipCompress.decompress(compressRpcRequestBytes);
        System.out.println(rpcRequestBytes.length+" "+compressRpcRequestBytes.length+" "+decompressRpcRequestBytes.length);
    }


}