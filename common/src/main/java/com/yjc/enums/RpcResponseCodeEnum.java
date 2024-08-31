package com.yjc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcResponseCodeEnum {
    SUCCESS(200,"调用方法成功"),
    RATE_LIMIT(407,"服务限流"),
    FAIL(500,"调用方法失败");

    private final int code;
    private final String message;
}
