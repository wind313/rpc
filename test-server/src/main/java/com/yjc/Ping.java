package com.yjc;

import com.yjc.annotation.RpcService;

@RpcService(name = "ping", canRetry = false)
public class Ping implements PingService{
    public String ping(){
        System.out.println("ping");
        return "pong";
    }
}
