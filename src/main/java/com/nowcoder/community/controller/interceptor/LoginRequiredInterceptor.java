package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author andrew
 * @create 2021-10-23 19:39
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor{

    @Autowired
    private HostHolder hostHolder;

    //在方法执行前，对其进行拦截，所以要重写preHandle方法；
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (handler instanceof HandlerMethod){
            //如果此拦截器拦截的对象handler是方法，则获取其方法对象
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            //获取方法对象的@LoginRequired类型的注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);

            //★如果@LoginRequired注解存在，则需要登录才能执行★
            if (loginRequired != null && hostHolder.getUser() == null){
                //需要登录且未登录，则重定向回登录页面 + 拦截请求；
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
