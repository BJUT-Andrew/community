package com.nowcoder.community.controller;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andrew
 * @create 2021-10-14 11:17
 */
@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    //处理访问首页的请求的控制器方法；
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){

        //将page类的对象加入形参，控制器方法需要对其某些属性值进行注入，同时也需要使用其某些属性值；

        //在方法调用前，前端控制器DispatcherServlet会自动实例化形参model、page，并将page注入model中，
        //所以，在Thymeleaf中可直接获取page对象，而无需再手动添加到共享域中。
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        //只存放帖子的list集合；
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());

        //存放帖子以及发布此帖子的用户对象；
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        //如果list集合中有帖子，则将其中的帖子对象以及其发布用户对象，保存到Map中，添加到新list中；
        if(list != null){
            for (DiscussPost discussPost : list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);

                discussPosts.add(map);
            }
        }

        //将存有帖子对象+发布者对象的List集合，共享到域中，方便前端页面调用；
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }

}
