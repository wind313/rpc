package com.yjc.retry;

import com.github.rholder.retry.*;
import com.yjc.enums.RpcResponseCodeEnum;
import com.yjc.transport.pojo.RpcRequest;
import com.yjc.transport.pojo.RpcResponse;
import com.yjc.transport.remoting.RpcSender;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuavaRetry {
    public RpcResponse sendWithRetry(RpcRequest rpcRequest,RpcSender rpcSender){
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfResult(result -> Objects.equals(result.getCode(), RpcResponseCodeEnum.FAIL.getCode()))
                .retryIfException()
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.warn("第{}次尝试", attempt.getAttemptNumber());
                    }
                })
                .build();
        try {
            return retryer.call(()->rpcSender.send(rpcRequest));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return RpcResponse.fail(rpcRequest.getRequestId());
    }

}
