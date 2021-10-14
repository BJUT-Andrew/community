package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

/**
 * @author andrew
 * @create 2021-10-13 18:57
 */

//@Mapper注解：
//mybatis中的注解，自动扫描数据持久层的映射文件及DAO接口的关系；
//让此注解标识的DAO接口和Mapper.xml文件进行绑定；
@Mapper
@Repository
public interface UserMapper {

    User selectById(@Param("id") int id);

    User selectByName(@Param("username") String username);

    User selectByEmail(@Param("email") String email);

    int insertUser(User user);

    int updateStatus(@Param("id") int id, @Param("status") int status);

    int updateHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    int updatePassword(@Param("id") int id, @Param("password") String password);

}
