package com.yjc.annotation.spring;

import com.yjc.annotation.RpcReference;
import com.yjc.annotation.RpcService;
import com.yjc.config.RpcConfig;
import com.yjc.enums.RpcTransportEnum;
import com.yjc.extension.ExtensionLoader;
import com.yjc.factory.SingletonFactory;
import com.yjc.provider.impl.ZKServiceProvider;
import com.yjc.transport.remoting.RpcSender;
import com.yjc.provider.ServiceProvider;
import com.yjc.proxy.RpcClientProxy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;

@Component
@Slf4j
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcSender rpcSender;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZKServiceProvider.class);
        this.rpcSender = ExtensionLoader.getExtensionLoader(RpcSender.class).getExtension(RpcTransportEnum.NETTY.getName());
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("扫描到[{}]被RpcService注解",bean.getClass().getName());
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            RpcConfig rpcConfig = RpcConfig.builder().name(annotation.name()).service(bean).build();
            serviceProvider.publishService(rpcConfig);

        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for(Field declaredField : declaredFields){
            RpcReference annotation = declaredField.getAnnotation(RpcReference.class);
            if(annotation != null){
                RpcConfig rpcConfig = RpcConfig.builder().name(annotation.name()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcSender, rpcConfig);
                Object proxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
