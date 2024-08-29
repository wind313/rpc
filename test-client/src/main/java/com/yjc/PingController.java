package com.yjc;

import com.yjc.annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class PingController {

    @RpcReference
    private PingService pingService;
    public void ping()
    {
        System.out.println(pingService.ping());

    }
}
