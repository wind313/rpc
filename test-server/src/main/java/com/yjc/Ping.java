package com.yjc;

import com.yjc.annotation.RpcService;

@RpcService
public class Ping implements PingService{
    public String ping(){
        System.out.println("ping");
        return "pong";
    }
}
