package com.yjc.proxy;

import com.yjc.config.RpcConfig;
import com.yjc.enums.RpcResponseCodeEnum;
import com.yjc.transport.remoting.RpcSender;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.pojo.RpcResponse;
import lombok.SneakyThrows;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
public class RpcClientProxy implements InvocationHandler {
    private final RpcConfig rpcConfig;
    private final RpcSender rpcSender;
    public RpcClientProxy(RpcSender rpcSender,RpcConfig rpcConfig) {
        this.rpcSender = rpcSender;
        this.rpcConfig = rpcConfig;
    }
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @SneakyThrows
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .parameterTypes(method.getParameterTypes())
                .interfaceName(method.getDeclaringClass().getName())
                .requestId(UUID.randomUUID().toString())
                .name(rpcConfig.getName())
                .build();
        RpcResponse rocResponse = null;
        CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcSender.send(rpcRequest);
        RpcResponse rpcResponse = completableFuture.get();

        this.check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }
    private void check(RpcResponse rpcResponse, RpcRequest rpcRequest)
    {
        if (rpcResponse == null)
        {
            throw new RuntimeException("服务调用失败，serviceName: " + rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId()))
        {
            throw new RuntimeException("请求与响应不匹配");
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode()))
        {
            throw new RuntimeException("服务调用失败，serviceName: " + rpcRequest.getInterfaceName());
        }
    }
}
