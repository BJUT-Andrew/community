package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author andrew
 * @create 2021-10-15 13:48
 */
@Controller
public class LoginController implements CommunityConstant{

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    //GET方式的/register请求，表示跳转到注册页面；
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(value = "/login")
    public String getLoginPage(){
        return "/site/login";
    }

    //POST方式的/register请求，表示利用表单进行提交注册数据；
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){

        Map<String, Object> map = userService.register(user);
        //注册成功（未激活），提醒用户激活，跳转到首页；
        if (map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else { //未注册成功，在当前注册页面显示注册失败信息；
            model.addAttribute("usernameMessage", map.get("usernameMessage"));
            model.addAttribute("passwordMessage", map.get("passwordMessage"));
            model.addAttribute("emailMessage", map.get("emailMessage"));
            return "/site/register";
        }
    }


    //处理点击激活链接后的激活请求；
    @RequestMapping(value = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){

        int result = userService.activation(userId, code);

        if (result == CommunityConstant.ACTIVATION_SUCCESS){
            model.addAttribute("msg", "您的账号已经激活成功,可以正常使用了!");
            model.addAttribute("target", "/login");
        }else if (result == CommunityConstant.ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        }else{
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        //都跳转到operate-result页面，不同结果显示不同msg且之后跳转的target不同；
        return "/site/operate-result";
    }


    //生成验证码图片，并在浏览器上显示；
    @RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
    //需要利用响应，将生成的图片显示在浏览器上；
    //后续点击登录发送登录请求时，需要验证用户输入的验证码与生成的验证码是否相同，所以要将生成的验证码信息保存到Session中（Cookie不安全）；
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){

        //生成验证码文本和图片；
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码保存到Session中，用于后续验证；
        //session.setAttribute("kaptcha", text);

        //将验证码存储在Redis中，重构代码：
        //生成用于标识此验证码对应的用户的随机字符串，存入Cookie中响应给浏览器端
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入Reids中，设置超时时间
        String redisKey = RedisKeyUtil.getKaptchKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        //将生成的验证码图片在浏览器上显示；
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }


    //处理登录（POST）请求
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*, HttpSession session*/, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){

        //已将验证码存入Redis中，重构代码：
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        //检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        //检查账号、密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }

    }

    //处理退出登录请求
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    //利用@CookieValue("")注解，获取Cookie中存储的值；
    public String logout(@CookieValue("ticket") String ticket){

        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";

    }



}
