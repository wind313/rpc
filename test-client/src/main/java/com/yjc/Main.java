package com.yjc;

import com.yjc.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
@RpcScan(basePackage = {"com.yjc"})
public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Main.class);
        PingController helloController = (PingController) applicationContext.getBean("pingController");
        helloController.ping();
    }
}