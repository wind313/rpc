package com.yjc.registry;

import com.yjc.extension.SPI;
import java.net.InetSocketAddress;
@SPI
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress, boolean canRetry);
}
