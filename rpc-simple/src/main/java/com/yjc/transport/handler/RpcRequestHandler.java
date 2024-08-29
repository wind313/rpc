package com.yjc.transport.handler;

import com.yjc.factory.SingletonFactory;
import com.yjc.provider.ServiceProvider;
import com.yjc.provider.impl.ZKServiceProvider;
import com.yjc.transport.pojo.RpcRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        this.serviceProvider = SingletonFactory.getInstance(ZKServiceProvider.class);
    }

    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeMethod(rpcRequest,service);

    }

    private Object invokeMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            result = method.invoke(service,rpcRequest.getParameters());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
        return result;
    }
}
