package com.yuanfangqiao.juli.ws;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
@Slf4j
public class DeviceSenderPool {

    private static final ConcurrentHashMap<String, DeviceSender> DEVICE_SENDER_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    public static void put(String id, DeviceSender appSender){
        try {
            DEVICE_SENDER_CONCURRENT_HASH_MAP.put(id, appSender);
        }catch (Exception e){
            log.error("DeviceSenderPool exception", e);
        }
    }

    public static DeviceSender get(String id){
        return DEVICE_SENDER_CONCURRENT_HASH_MAP.get(id);
    }

    public static void remove(String id){
        DEVICE_SENDER_CONCURRENT_HASH_MAP.remove(id);
    }

}
