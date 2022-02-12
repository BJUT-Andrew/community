package com.nowcoder.community.controller;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andrew
 * @create 2021-10-14 11:17
 */
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    //处理访问首页的请求的控制器方法；
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {

        //将page类的对象加入形参，控制器方法需要对其某些属性值进行注入，同时也需要使用其某些属性值；

        //在方法调用前，前端控制器DispatcherServlet会自动实例化形参model、page，并将page注入model中，
        //所以，在Thymeleaf中可直接获取page对象，而无需再手动添加到共享域中。
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        //只存放帖子的list集合；
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);

        //存放帖子以及发布此帖子的用户对象；
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        //如果list集合中有帖子，则将其中的帖子对象以及其发布用户对象，保存到Map中，添加到新list中；
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);

                //将帖子的点赞数存入map中
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }

        //将存有帖子对象+发布者对象的List集合，共享到域中，方便前端页面调用；
        model.addAttribute("discussPosts", discussPosts);

        //排序方式传给前端模板
        model.addAttribute(orderMode);

        return "index";
    }

    //处理出现异常的请求，跳转到500.html页面
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    //没有权限，则跳转到404页面
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

}
