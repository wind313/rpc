package com.yjc.transport.remoting.netty.client;

import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.pojo.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class UnprocessedRequest {
    private static final Map<String, CompletableFuture<RpcResponse>> UNPROCESSED_REQUEST = new ConcurrentHashMap<>();
    public void put(String requestId, CompletableFuture<RpcResponse> future)
    {
        UNPROCESSED_REQUEST.put(requestId, future);
    }
    public void complete(RpcResponse rpcResponse)
    {
        CompletableFuture<RpcResponse> future = UNPROCESSED_REQUEST.remove(rpcResponse.getRequestId());
        if (future != null) {
            future.complete(rpcResponse);
        }
        else {
            throw new IllegalStateException();
        }
    }
}
