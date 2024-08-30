package com.yjc;

import com.yjc.annotation.RpcScan;
import com.yjc.transport.remoting.netty.server.RpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
@RpcScan(basePackage = {"com.yjc"})
public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Main.class);
        RpcServer nettyRpcServer = (RpcServer) applicationContext.getBean("rpcServer");
        // Register service manually
        nettyRpcServer.run();
    }
}