package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.jws.Oneway;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author andrew
 * @create 2021-10-14 11:07
 */
@Service
public class UserService implements CommunityConstant{

    @Autowired
    private RedisTemplate redisTemplate;

    //@Autowired
    //private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Value(value = "${community.path.domain}")
    private String domain;

    @Value(value = "${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    //首页展示界面，帖子的发布者应该显示用户名而不是userId，所以要使用userId查询到该用户，再查询用户名；
    public User findUserById(int id){
        //return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
    }

    //注册功能：
    // 如果出现问题，则保存出现的问题，将其显示在注册页面上告知用户；
    // 如果没有问题，则进行注册...
    public Map<String, Object> register(User user){

        Map<String, Object> map = new HashMap<>();

        //空值处理：user对象、账号名、密码、邮箱；
        if (user == null){
            throw new IllegalArgumentException("注册对象参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMessage", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMessage", "密码不能为空！");
            return map;
        }if (StringUtils.isBlank(user.getUsername())){
            map.put("emailMessage", "邮箱不能为空！");
            return map;
        }

        //验证该账号是否已存在；
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMessage", "该账号已存在");
            return map;
        }

        //验证注册邮箱是否已存在；
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMessage", "该邮箱已被注册！");
            return map;
        }

        //输入信息没有问题，可以进行注册，为该用户生成一些初始信息：密码salt、头像、创建时间等...
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        //注册成功，设状态码status为0，表示未激活；
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //服务端发送激活邮件
        Context context = new Context();
        String email = user.getEmail();
        context.setVariable("email", email);
        //激活链接示例：http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //以/mail/activation为模板，将context中的内容整合，利用模板引擎生成一个HTML的邮件内容
        String content = templateEngine.process("/mail/activation", context);
        //发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        //表单提交数据没有问题，代表正常注册，返回一个空map；
        return map;
    }

    //激活功能
    public int activation(int userId, String code){

        User user = userMapper.selectById(userId);

        if (user.getStatus() == 1){
            //1.当前用户的状态已经为激活，则重复激活；
            return CommunityConstant.ACTIVATION_REPEAT;
        }else   if (code.equals(user.getActivationCode())){
            //2.传入激活码一致，则激活成功，更改状态码；
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return CommunityConstant.ACTIVATION_SUCCESS;
        }else {
            //3.传入激活码不一致，激活失败；
            return CommunityConstant.ACTIVATION_FAILURE;
        }
    }


    //登录功能：和注册功能类似，要返回一个map，记录失败or成功信息；
    public Map<String, Object> login(String username, String password, long expiredSeconds){

        Map<String, Object> map = new HashMap<>();

        //处理空值；
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号是否已注册；
        User u = userMapper.selectByName(username);
        if (u == null){
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        //验证该账号是否已激活；
        if (u.getStatus() == 0){
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        //验证密码是否正确；
        password = CommunityUtil.md5(password + u.getSalt());
        if (!password.equals(u.getPassword())){
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //登录成功，生成登录凭证，并存储在数据库中；
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(u.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        //loginTicketMapper.insertLoginTicket(loginTicket);

        //将登录凭证存储在Redis中，重构代码：
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        //将登录凭证以JSONString的格式存储在Redis中
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    //退出功能，调用dao层方法修改用户登录凭证的状态
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    //根据凭证查询登录凭证对象
    public LoginTicket findLoginTicket(String ticket){
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(RedisKeyUtil.getTicketKey(ticket));
        return loginTicket;
    }

    //调用dao层，修改用户头像的web访问路径
    public int updateHeader(int userId, String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    //调用dao层，修改用户密码
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword){
        Map<String, Object> map = new HashMap<>();

        //处理空值
        if (StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg", "原密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }

        //验证原密码是否正确
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!oldPassword.equals(user.getPassword())){
            map.put("oldPasswordMsg", "原密码输入有误！");
            return map;
        }

        //原密码输入正确，更新密码（验证新密码与确认密码是否相等，由前端js执行）
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        clearCache(userId);

        return map;
    }

    //根据用户名查找用户对象
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //将用户信息缓存在redis中
    //1.查询用户信息时，先试图从缓存中获取
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2.缓存中无目标数据，从数据库中查询，并将此数据添加到缓存中
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    //3.数据进行修改时，将数据从缓存中删除
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    //根据用户id，获取其type，获取其身份（权限）；
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
