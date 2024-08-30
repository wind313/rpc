package com.yjc.registry.impl;

import com.yjc.registry.ServiceRegistry;
import com.yjc.registry.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import java.net.InetSocketAddress;

public class ServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress, boolean canRetry) {
        String path = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, path);
        if(canRetry)
        {
            path = CuratorUtils.ZK_REGISTER_ROOT_PATH +"/"+CuratorUtils.RETRY+"/"+rpcServiceName;
            CuratorUtils.createPersistentNode(zkClient,path);
        }
    }
}
