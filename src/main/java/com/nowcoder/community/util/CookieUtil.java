package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author andrew
 * @create 2021-10-22 10:20
 */
public class CookieUtil {

    //根据name，从请求的Cookie中获取Cookie的值
    public static String getValue(HttpServletRequest request, String name) {

        if (request == null || name == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null){
            for (Cookie cookie : cookies){
                if (cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
