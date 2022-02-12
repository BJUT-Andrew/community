package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author andrew
 * @create 2021-10-27 19:21
 */
@Mapper
@Repository
public interface MessageMapper {

    //查询当前用户的会话（私信）列表（分页），针对每个会话只返回最新的一条私信
    List<Message> selectConversations(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的所有私信列表（分页）
    List<Message> selectLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    //查询某个会话包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    //不带conversationId则查询该用户所有的，带则查询此会话的
    int selectLetterUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

    //发送私信
    int insertMessage(Message message);

    //修改私信的状态，点开一个会话，将会话内所有私信都改为已读
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

    //查询某个主题下最新的通知
    Message selectLastestNotice(@Param("userId") int userId, @Param("topic") String topic);

    //查询某个主题所包含的通知数量
    int selectNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    //查询某个主题中未读的通知数量
    int selectNoticeUnreadCount(@Param("userId") int userId, @Param("topic") String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(@Param("userId") int userId, @Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);

}
