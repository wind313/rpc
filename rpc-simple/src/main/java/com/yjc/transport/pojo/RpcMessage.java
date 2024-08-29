package com.yjc.transport.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcMessage {
    private byte messageType;
    private byte serialize;
    private byte compress;
    private int requestId;
    private Object data;
}
