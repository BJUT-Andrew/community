package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import com.sun.mail.imap.protocol.ID;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.*;

/**
 * @author andrew
 * @create 2021-10-20 18:35
 */
@Mapper
@Component
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired}) "
    })
    //自动生成主键id
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket = #{ticket} "
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "update login_ticket set status = #{status} where ticket = #{ticket}"
    })
    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);

}
