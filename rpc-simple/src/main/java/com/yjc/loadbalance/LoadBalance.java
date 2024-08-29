package com.yjc.loadbalance;

import com.yjc.extension.SPI;
import com.yjc.transport.pojo.RpcRequest;
import java.util.List;

@SPI
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
