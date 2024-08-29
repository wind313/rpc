package com.yjc.registry;

import com.yjc.extension.SPI;
import com.yjc.transport.pojo.RpcRequest;
import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
