package com.yjc.transport.remoting;

import com.yjc.extension.SPI;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.pojo.RpcResponse;

import java.util.concurrent.ExecutionException;

@SPI
public interface RpcSender {
    RpcResponse send(RpcRequest rpcRequest) throws ExecutionException, InterruptedException;
}
