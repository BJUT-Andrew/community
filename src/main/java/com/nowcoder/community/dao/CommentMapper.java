package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author andrew
 * @create 2021-10-27 11:21
 */
@Mapper
@Repository
public interface CommentMapper {

    //查询所有评论内容的方法（需要分页）
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    //查询评论数目的方法
    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    //增加评论
    int insertComment(Comment comment);

    //根据id查询评论
    Comment selectCommentById(int id);
}
