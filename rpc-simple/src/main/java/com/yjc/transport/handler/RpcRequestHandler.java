package com.yjc.transport.handler;

import com.yjc.enums.RpcResponseCodeEnum;
import com.yjc.factory.SingletonFactory;
import com.yjc.provider.ServiceProvider;
import com.yjc.provider.impl.ZKServiceProvider;
import com.yjc.rateLimit.RateLimit;
import com.yjc.rateLimit.RateLimitProvider;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.pojo.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;
    private RateLimitProvider rateLimitProvider;

    public RpcRequestHandler() {
        this.serviceProvider = SingletonFactory.getInstance(ZKServiceProvider.class);
        this.rateLimitProvider = SingletonFactory.getInstance(RateLimitProvider.class);
    }

    public RpcResponse handle(RpcRequest rpcRequest){
        String interfaceName = rpcRequest.getInterfaceName();
        RateLimit rateLimit = rateLimitProvider.getRateLimit(interfaceName);
        if (!rateLimit.getToken()){
            log.warn("服务限流");
            return RpcResponse.fail(RpcResponseCodeEnum.RATE_LIMIT);
        }
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        RpcResponse response = invokeMethod(rpcRequest, service);
        return response;

    }

    private RpcResponse invokeMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            result = method.invoke(service,rpcRequest.getParameters());
            log.info("得到结果:{}", result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
        return RpcResponse.success(result, rpcRequest.getRequestId());
    }
}
