package com.yjc.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtensionLoader<T> {
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

    private final Class<?> type;

    private static final Map<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    private final Map<String,Holder<Object>> cacheInstance = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    public ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if(type==null){
            throw new IllegalArgumentException("扩展类型不能为空");
        }
        if(!type.isInterface()){
            throw new IllegalArgumentException("扩展类型必须是接口");
        }
        if(type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("扩展类型必须包含@SPI注解");
        }
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if(extensionLoader==null){
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    public T getExtension(String name){
        if(name == null || name.trim().length()==0){
            throw new IllegalArgumentException("扩展名不能为空");
        }
        Holder<Object> holder = cacheInstance.get(name);
        if(holder == null){
            cacheInstance.putIfAbsent(name,new Holder<>());
            holder = cacheInstance.get(name);
        }
        Object instance = holder.getValue();
        if(instance==null){
            synchronized (holder){
                instance = holder.getValue();
                if(instance==null){
                    instance = createExtension(name);
                    holder.setValue(instance);
                }
            }
        }
        return (T)instance;
    }

    private T createExtension(String name){
        Class<?> clazz = getExtensionClasses().get(name);
        if(clazz == null){
            throw new RuntimeException("扩展类"+name+"不存在");
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if(instance == null){
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz,clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.getValue();
        if(classes == null){
            synchronized (cachedClasses){
                classes = cachedClasses.getValue();
                if(classes == null){
                    classes = new ConcurrentHashMap<>();
                    loadDirectory(classes);
                    cachedClasses.setValue(classes);
                }
            }
        }
        return classes;
    }

    private void loadDirectory(Map<String,Class<?>> extensionClasses){
        String filename = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            Enumeration<URL> urls = classLoader.getResources(filename);
            if(urls != null){
                while (urls.hasMoreElements()){
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses,classLoader,resourceUrl);
                }
            }
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    private void loadResource(Map<String,Class<?>> extensionClasses,ClassLoader classLoader,URL resourceUrl){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), StandardCharsets.UTF_8))){
            String line ;
            while((line = reader.readLine()) != null){
                final int index = line.indexOf("#");
                if(index >= 0){
                    line = line.substring(0,index);
                }
                line = line.trim();
                if(line.length() > 0){
                    try{
                        final int ei = line.indexOf("=");
                        String name = line.substring(0,ei).trim();
                        String clazzName = line.substring(ei+1).trim();
                        if(name.length() > 0 && clazzName.length() > 0){
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name,clazz);
                        }
                        log.info("加载扩展类："+clazzName);
                    }
                    catch (ClassNotFoundException e){
                        log.error(e.getMessage());
                    }
                }
            }
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

}
