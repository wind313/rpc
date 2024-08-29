package com.yjc;

import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.serialize.kryo.KryoSerializer;

import java.util.UUID;

class KryoSerializerTest {

    public static void main(String[] args) {
        RpcRequest target = RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"sayhelooloo", "sayhelooloosayhelooloo"})
                .interfaceName("github.javaguide.HelloService")
                .parameterTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .name("group1")
                .build();
        KryoSerializer kryoSerializer = new KryoSerializer();
        byte[] bytes = kryoSerializer.serialize(target);
        RpcRequest actual = kryoSerializer.deserialize(bytes, RpcRequest.class);
        System.out.println(actual);
    }
}