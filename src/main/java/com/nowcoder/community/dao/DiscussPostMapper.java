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
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    //查询帖子的数目，若传入userId，则查询此用户发布的帖子数目；
    int selectDiscussPostRows(@Param("userId") int userId);


}
