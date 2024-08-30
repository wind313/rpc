package com.yjc;

import com.yjc.annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class PingController {

    @RpcReference(name = "ping")
    private PingService pingService;
    public void ping()
    {
        System.out.println(pingService.ping());

    }
}
