package com.yjc.proxy;

import com.yjc.config.RpcConfig;
import com.yjc.enums.RpcResponseCodeEnum;
import com.yjc.enums.ServiceEnum;
import com.yjc.extension.ExtensionLoader;
import com.yjc.factory.SingletonFactory;
import com.yjc.registry.ServiceDiscovery;
import com.yjc.retry.GuavaRetry;
import com.yjc.transport.remoting.RpcSender;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.pojo.RpcResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final RpcConfig rpcConfig;
    private final RpcSender rpcSender;

    private final ServiceDiscovery serviceDiscovery;
    public RpcClientProxy(RpcSender rpcSender,RpcConfig rpcConfig) {
        this.rpcSender = rpcSender;
        this.rpcConfig = rpcConfig;
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceEnum.ZK.getName());
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
        RpcResponse rpcResponse;
        if(serviceDiscovery.checkRetry(rpcRequest.getInterfaceName())){
            rpcResponse = SingletonFactory.getInstance(GuavaRetry.class).sendWithRetry(rpcRequest,rpcSender);
        }
        else {
            rpcResponse = rpcSender.send(rpcRequest);
        }
        this.check(rpcResponse, rpcRequest);

        return rpcResponse.getData();
    }
    private void check(RpcResponse rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode()))
        {
            log.warn("服务调用成功: " + rpcRequest.getInterfaceName());
            return;
        }
        if (rpcResponse.getCode().equals(RpcResponseCodeEnum.RATE_LIMIT.getCode()))
        {
            log.warn("服务限流: " + rpcRequest.getInterfaceName());
            return;
        }
        if (rpcResponse == null)
        {
            throw new RuntimeException("服务调用失败，serviceName: " + rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId()))
        {
            throw new RuntimeException("请求与响应不匹配");
        }
        if (rpcResponse.getCode() == null || rpcResponse.getCode().equals(RpcResponseCodeEnum.FAIL.getCode()))
        {
            throw new RuntimeException("服务调用失败，serviceName: " + rpcRequest.getInterfaceName());
        }

    }
}
