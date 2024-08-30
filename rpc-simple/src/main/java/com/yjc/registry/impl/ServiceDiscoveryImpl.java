package com.yjc.registry.impl;

import com.yjc.enums.LoadBalanceEnum;
import com.yjc.extension.ExtensionLoader;
import com.yjc.loadbalance.LoadBalance;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.registry.ServiceDiscovery;
import com.yjc.registry.utils.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;



    public ServiceDiscoveryImpl(){
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOAD_BALANCE.getName());
    }
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (serviceUrlList == null || serviceUrlList.size() == 0) {
            throw new RuntimeException("找不到服务:"+rpcServiceName);
        }
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("成功找到服务:{}",targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }

    @Override
    public boolean checkRetry(String serviceName){
        boolean canRetry = false;
        try{
            CuratorFramework client = CuratorUtils.getZkClient();
            boolean exists = client.checkExists().forPath(CuratorUtils.ZK_REGISTER_ROOT_PATH +"/" + CuratorUtils.RETRY) != null;
            if(exists){
                log.warn("服务:{}可以重试",serviceName);
                canRetry = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return canRetry;
    }
}
