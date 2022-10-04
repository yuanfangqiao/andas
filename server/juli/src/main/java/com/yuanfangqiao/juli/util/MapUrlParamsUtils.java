package com.yuanfangqiao.juli.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cheng.zhang
 * @version 1.0
 * @date 2021/12/10 15:08
 *
 * 简单的鉴权，只能适用于私有部署项目
 *
 */
public class MapUrlParamsUtils {
    /**
     * 将url参数转换成map
     *
     * @param param aa=11&bb=22&cc=33
     * @return
     */
    public static Map<String, String> urlToMap(String param) {
        Map<String, String> map = new HashMap<>(0);
        if (null == param || "".equals(param)) {
            return map;
        }
        String[] params = param.split("&");
        for (String s : params) {
            String[] p = s.split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            }
            if (p.length == 1) {
                map.put(p[0], "");
            }
        }
        return map;
    }

    /**
     * 将map转换成url
     *
     * @param map
     * @return
     */
    public static String mapToUrl(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if(!"".equals(entry.getKey())){
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                sb.append("&");
            }
        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = s.substring(0,s.length()-1);
        }
        return s;
    }

    public static void main(String[] args){
        String url1 = "aa=11&bb=22&cc=33";
        Map<String, String> map1 = urlToMap(url1);
        System.out.println(map1);

        String url2 = "aa=&bb=&cc=33";
        Map<String, String> map2 = urlToMap(url2);
        System.out.println(map2);

        System.out.println(mapToUrl(map1));

        System.out.println(mapToUrl(map2));

    }

}
