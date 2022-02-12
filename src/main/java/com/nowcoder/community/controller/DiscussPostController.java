package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_POST;

/**
 * @author andrew
 * @create 2021-10-24 23:29
 */
@Controller
@RequestMapping(value = "/discuss")
public class DiscussPostController implements CommunityConstant{

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    //处理发布帖子请求
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {

        //如果当前未登录，则无权限发布帖子
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还未登录！");
        }

        //创建“帖子对象”
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());

        //调用Service层方法发布帖子
        discussPostService.addDiscussPost(discussPost);


        //触发发帖事件，要将新发布的帖子存入Elasticsearch中，将此事件存入消息队列中。
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setEntityId(discussPost.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setUserId(user.getId());
        eventProducer.fireEvent(event);

        //发布帖子，将该帖子存入要更新分数的帖子缓存中
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        //报错的情况，将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }


    //处理根据id查询帖子详情的请求
    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        //根据帖子id获取帖子对象，并放入共享域中
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //显示帖子时，要显示贴主的名字头像等，而不只显示贴主的id，所以要查询到贴主对象，并放入共享域中
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //将帖子的点赞数存入共享域
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        //将帖子的点赞状态存入共享域
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);


        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论

        //获取当前帖子的评论列表(实体对象)
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit()
        );
        //创建当前帖子的评论的展示(VO)列表(视图对象)
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //每条评论对应一个map对象，用于在前端页面显示
                Map<String, Object> commentVo = new HashMap<>();
                //将评论对象存入
                commentVo.put("comment", comment);
                //将评论的作者存入
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //将评论的点赞数存入
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //将评论的点赞状态存入
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                //将评论的“回复列表(不进行分页)”存入
                //除了不进行分页，其他操作等同于帖子下的显示评论操作
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE
                );
                //创建当前评论的回复的展示(VO)列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList){
                        //评论的每条回复对应一个map，用于在前端页面显示
                        Map<String, Object> replyVo = new HashMap<>();
                        //将回复对象存入
                        replyVo.put("reply", reply);
                        //将回复的作者存入
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //将回复的目标用户存入(User target，回复回复的回复特有，针对某一个用户的回复)
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //将回复的点赞数存入
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        //将回复的点赞状态存入
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        //将此条回复的VO对象存入VO列表中
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                //将评论的回复数量存入
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                //将此条评论的VO对象存入VO列表中
                commentVoList.add(commentVo);
            }
        }

        //将评论的VO列表存入共享域中
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

    // 置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        // 帖子状态发生变化，要在Elasticsearch中实时更新，触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 帖子状态发生变化，要在Elasticsearch中实时更新，触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //加精，会更改帖子分数，将操作的帖子放入更新分数的缓存中
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 帖子状态发生变化，要在Elasticsearch中实时更新，触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}
