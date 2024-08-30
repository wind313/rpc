package com.yjc.config;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RpcConfig {
    private String name="";
    private Object service;
    private boolean canRetry = false;

    public String getServiceName(){
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
    public String getRpcServiceName(){
        return this.getServiceName()+this.getName();
    }
}
