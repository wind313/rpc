package com.yjc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceEnum {
    ZK("zk");
    private final String name;
}
