package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象；
 * @author andrew
 * @create 2021-10-22 10:35
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<User>();

    //向当前线程中存入user对象
    public void setUser(User user){
        users.set(user);
    }

    //从当前线程中取user对象
    public User getUser(){
        return users.get();
    }

    //清理当前线程中的数据
    public void clear(){
        users.remove();
    }

}
