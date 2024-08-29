package com.yjc.provider;

import com.yjc.config.RpcConfig;

public interface ServiceProvider {
    void addService(RpcConfig rpcConfig);
    Object getService(String rpcServiceName);
    void publishService(RpcConfig rpcConfig);
}
