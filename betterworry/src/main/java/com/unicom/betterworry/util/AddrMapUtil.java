package com.unicom.betterworry.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公共键值映射工具类
 */
public class AddrMapUtil {
    private static final Map<String, Object> ADDR_MAP = new ConcurrentHashMap();

    public static Object get(String key) {
        return ADDR_MAP.get(key);
    }

    public static Object get(String prefix,String key) {
        return ADDR_MAP.get(prefix+key);
    }

    public static void put(String key, Object value) {
        ADDR_MAP.put(key, value);
    }

    public static void put(String prefix,String key, Object value) {
        ADDR_MAP.put(prefix+key, value);
    }
    public static void putIfAbsent(String key, Object value) {
        ADDR_MAP.putIfAbsent(key, value);
    }
    public static void putIfAbsent(String prefix,String key, Object value) {
        ADDR_MAP.putIfAbsent(prefix+key, value);
    }


    public static boolean containsKey(String key) {
        return ADDR_MAP.containsKey(key);
    }

    public static void remove(String key) {
        ADDR_MAP.remove(key);
    }

    public static boolean containsKey(String prefix,String key) {
        return ADDR_MAP.containsKey(prefix+key);
    }

    public static void remove(String prefix,String key) {
        ADDR_MAP.remove(prefix+key);
    }


}
