package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * @author andrew
 * @create 2021-10-15 14:14
 */
public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID() {
        //生成随机字符串，并去除串中的"-"；
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //MD5对所有用户密码进行加密：并加入salt，进一步提高用户密码安全性；
    public static String md5(String key) {

        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    //服务器向浏览器可能返回编码int code、提示信息String msg、业务信息HashMap map等数据
    //需要将这些数据转换为JSON字符串格式，响应到浏览器上；
    public static String getJSONString(int code, String msg, Map<String, Object> map) {

        JSONObject json = new JSONObject();

        //将code、msg、map添加到json对象中；
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put("key", map.get(key));
            }
        }

        return json.toJSONString();
    }

    //数据可能不全，需要重载
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

}
