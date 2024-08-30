package com.yjc.provider.impl;

import com.yjc.config.RpcConfig;
import com.yjc.enums.ServiceEnum;
import com.yjc.extension.ExtensionLoader;
import com.yjc.transport.remoting.netty.server.RpcServer;
import com.yjc.provider.ServiceProvider;
import com.yjc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZKServiceProvider implements ServiceProvider {
    private final Set<String> registeredService;
    private final Map<String,Object> serviceMap;
    private final ServiceRegistry serviceRegistry;
    public ZKServiceProvider() {
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceMap = new ConcurrentHashMap<>();
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceEnum.ZK.getName());
    }
    @Override
    public void addService(RpcConfig rpcConfig) {
        String rpcServiceName = rpcConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcConfig.getService());
        log.info("添加服务：{} 和接口：{}",rpcServiceName,rpcConfig.getService().getClass().getInterfaces());
    }
    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if(null == service){
            throw new RuntimeException("没有找到服务：" + rpcServiceName);
        }
        return service;
    }
    @Override
    public void publishService(RpcConfig rpcConfig) {
        String hostAddress = null;
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcConfig);
            this.serviceRegistry.registerService(rpcConfig.getRpcServiceName(),new InetSocketAddress(hostAddress, RpcServer.PORT),rpcConfig.isCanRetry());
        } catch (UnknownHostException e) {
            log.error("获取本地主机地址失败",e);
        }
    }
}
