package com.yjc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcTransportEnum {

    NETTY("netty"),
    SOCKET("socket");

    private final String name;
}
