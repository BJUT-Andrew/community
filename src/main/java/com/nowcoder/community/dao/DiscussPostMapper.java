package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author andrew
 * @create 2021-10-14 10:13
 */
@Mapper
@Repository
public interface DiscussPostMapper {

    //查询帖子内容，返回一个List集合。
    //userId：用于查询某用户发布的所有帖子，有此参数则带上此条件，无则不带，动态sql；
    //offset：用于MySQL分页，表示每页的起始行索引；
    //limit：用于MySQL分页，表示此页的行数目；
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset,
                                         @Param("limit") int limit, @Param("orderMode") int orderMode);

    //查询帖子的数目，若传入userId，则查询此用户发布的帖子数目；
    int selectDiscussPostRows(@Param("userId") int userId);

    //增加帖子
    int insertDiscussPost(DiscussPost discussPost);

    //根据帖子id查询帖子（对象）详情
    DiscussPost selectDiscussPostById(int id);

    //更新帖子的评论数目
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    //置顶、取消置顶
    int updateType(@Param("id") int id, @Param("type") int type);

    //加精、删除（拉黑）
    int updateStatus(@Param("id") int id, @Param("status") int status);

    //更新帖子分数
    int updateScore(@Param("id") int id,@Param("score") double score);

}
