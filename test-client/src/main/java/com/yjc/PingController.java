package com.yjc;

import com.yjc.annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class PingController {

    @RpcReference(name = "ping")
    private PingService pingService;
    public void ping()
    {
        for(int i = 0; i < 100; i++){
            System.out.println(pingService.ping());
        }


    }
}
