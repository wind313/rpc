package com.yjc.transport.remoting;

import com.yjc.extension.SPI;
import com.yjc.transport.pojo.RpcRequest;
@SPI
public interface RpcSender {
    Object send(RpcRequest rpcRequest);
}
