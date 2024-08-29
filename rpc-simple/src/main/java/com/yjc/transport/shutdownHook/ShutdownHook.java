package com.yjc.transport.shutdownHook;

import com.yjc.registry.utils.CuratorUtils;
import com.yjc.transport.remoting.netty.server.RpcServer;
import lombok.extern.slf4j.Slf4j;
import java.net.InetAddress;
import java.net.InetSocketAddress;
@Slf4j
public class ShutdownHook {
    private static final ShutdownHook CUSTOM_SHUTDOWN_HOOK = new ShutdownHook();
    public static ShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }
    public void clearAll() {
        log.info("添加关闭钩子");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), RpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (Exception e) {
            }
        }));
    }
}
