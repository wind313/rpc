package com.yjc;

import com.yjc.extension.ExtensionLoader;
import com.yjc.loadbalance.LoadBalance;

public class Extension {
    public static void main(String[] args) {
        LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
        System.out.println(loadBalance);
    }
}
