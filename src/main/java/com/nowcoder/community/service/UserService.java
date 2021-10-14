package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author andrew
 * @create 2021-10-14 11:07
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    //首页展示界面，帖子的发布者应该显示用户名而不是userId，所以要使用userId查询到该用户，再查询用户名；
    public User findUserById(int id){
        return userMapper.selectById(id);
    }

}
