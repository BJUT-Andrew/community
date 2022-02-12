package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author andrew
 * @create 2021-10-27 16:12
 */
@Controller
@RequestMapping(value = "/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    //处理提交评论表单的请求
    //接收帖子id，确保点击提交后，重定向回添加完评论的目标帖子详情页
    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {

        //前端传入评论对象comment的entityType\entityId\targetId\content\，其他需要后端赋值
        comment.setCreateTime(new Date());
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);

        //调用Service层方法，添加评论
        commentService.addComment(comment);

        //触发评论事件，构造事件，调用生产者将事件添加到消息队列中
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setEntityType(comment.getEntityType())
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);


        //如果该评论是对帖子的评论，则触发发帖事件，要将新发布了评论的帖子“更新”到Elasticsearch中，将此事件存入消息队列中
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setEntityId(discussPostId)
                    .setEntityType(ENTITY_TYPE_POST)
                    .setUserId(comment.getUserId());
            eventProducer.fireEvent(event);

            //如果是对帖子的评论，会更改帖子分数，将操作的帖子放入更新分数的缓存中
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        //重定向至，根据id查询帖子详情的请求
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
