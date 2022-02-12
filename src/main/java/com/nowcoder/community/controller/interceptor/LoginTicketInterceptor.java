package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Security;
import java.util.Date;

/**
 * 创建拦截器类；
 * @author andrew
 * @create 2021-10-22 10:17
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor{

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //查询用户信息，每次请求之前都需要，所以放在preHandle中
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //从Cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null){
            //查询登录凭证对象
            LoginTicket loginTicket = userService.findLoginTicket(ticket);

            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户对象
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有当前登录用户对象（需要使用ThreadLocal实现线程隔离，只在此浏览器对应的线程中存储）
                hostHolder.setUser(user);

                //构建用户认证的结果（权限），存入SecurityContext中告知SpringSecurity，以便Security进行授权
                Authentication Authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(Authentication));

            }
        }

        return true;
    }

    //在显示模板页面前，将查询到的当前登录用户存在共享域中，以便前端模板调用
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

        User user = hostHolder.getUser();

        if (user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    //请求结束之后，将线程中的数据进行清理
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
