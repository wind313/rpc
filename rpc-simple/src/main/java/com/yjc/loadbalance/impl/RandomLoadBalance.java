package com.yjc.loadbalance.impl;

import com.yjc.loadbalance.AbstractLoadBalance;
import com.yjc.transport.pojo.RpcRequest;
import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
