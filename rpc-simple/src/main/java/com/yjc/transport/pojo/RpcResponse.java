package com.yjc.transport.pojo;

import com.yjc.enums.RpcResponseCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Slf4j
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 15619401053L;
    private String requestId;
    private Integer code;
    private String message;
    private Object data;
    public static  RpcResponse success(Object data, String requestId) {
        RpcResponse response = new RpcResponse();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }

        return response;
    }
    public static  RpcResponse fail(RpcResponseCodeEnum rpcResponseCodeEnum, String requestId) {
        RpcResponse response = new RpcResponse();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        response.setRequestId(requestId);
        return response;
    }

    public static  RpcResponse fail(String requestId) {
        RpcResponse response = new RpcResponse();
        response.setCode(RpcResponseCodeEnum.FAIL.getCode());
        response.setMessage(RpcResponseCodeEnum.FAIL.getMessage());
        response.setRequestId(requestId);
        return response;
    }
}
