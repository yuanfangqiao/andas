package com.yuanfangqiao.juli.ws;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
public class AppSenderPool {

    private static final ConcurrentHashMap<String, AppSender> APP_SENDER_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    public static void put(String id, AppSender appSender){
        APP_SENDER_CONCURRENT_HASH_MAP.put(id, appSender);
    }

    public static AppSender get(String id){
        return APP_SENDER_CONCURRENT_HASH_MAP.get(id);
    }

    public static void remove(String id){
        APP_SENDER_CONCURRENT_HASH_MAP.remove(id);
    }

}
