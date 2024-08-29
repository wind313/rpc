package com.yjc.loadbalance;

import com.yjc.transport.pojo.RpcRequest;
import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if (serviceUrlList == null || serviceUrlList.size() == 0) {
            return null;
        }
        if (serviceUrlList.size() == 1) {
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList, rpcRequest);
    }
    protected abstract String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest);
}
