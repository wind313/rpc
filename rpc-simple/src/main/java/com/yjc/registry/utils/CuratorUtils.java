package com.yjc.registry.utils;

import com.yjc.enums.RpcConfigEnum;
import com.yjc.util.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
@Slf4j
public class CuratorUtils {
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "139.9.136.188:2181";
    private static CuratorFramework zkClient;
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static final Map<String,List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    public static final String ZK_REGISTER_ROOT_PATH = "/my/rpc";
    public static CuratorFramework getZkClient(){
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeper = DEFAULT_ZOOKEEPER_ADDRESS;
        if(properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue())!=null){
            zookeeper = properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue());
        }
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }
        ExponentialBackoffRetry exponentialBackoffRetry = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeper)
                .retryPolicy(exponentialBackoffRetry)
                .build();
        zkClient.start();
        try {
            if(!zkClient.blockUntilConnected(30, TimeUnit.MINUTES)){
                throw new RuntimeException("Zookeeper连接超时");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }
    public static void createPersistentNode(CuratorFramework zkClient,String path){

        try {
            if(REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("[{}]已经存在，无需创建", path);
            }
            else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("创建[{}]成功", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("创建[{}]失败", path);
        }

    }
    public static List<String> getChildrenNodes(CuratorFramework zkClient,String serviceName){
        if(SERVICE_ADDRESS_MAP.containsKey(serviceName)){
            return SERVICE_ADDRESS_MAP.get(serviceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH+"/"+serviceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName,result);
            registerWatcher(serviceName,zkClient);
        } catch (Exception e) {
            log.error("获取路径[{}]的子节点失败", servicePath);
        }

        return result;
    }
    private static void registerWatcher(String serviceName,CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH+"/"+serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener =(curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName,serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if(p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            }catch (Exception e){
                log.error("删除zookeeper中地址[{}]失败", p);
            }
        });
        log.info("服务端[{}]注销成功", REGISTERED_PATH_SET.toString());
    }


}
