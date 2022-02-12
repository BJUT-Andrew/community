package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author andrew
 * @create 2021-10-13 20:35
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){

        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();

        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());


    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png/test@Param");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "Hello, @Param");
        System.out.println(rows);
    }

    @Test
    public void testSelectDiscussPosts(){

        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10, 0);
        for(DiscussPost discussPost : discussPosts){
            System.out.println(discussPost);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);

    }

    @Test
    public void testInsertLoginTicket(){

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000*60*10));

        loginTicketMapper.insertLoginTicket(loginTicket);

    }

    @Test
    public void testSelectByTicket(){

        LoginTicket abcLoginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(abcLoginTicket);
        loginTicketMapper.updateStatus("abc", 1);
        abcLoginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(abcLoginTicket);

    }

    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);

    }

    @Test
    public void test(){
        int[][] martix = new int[][]{{1,2,3},{4,5,6},{7,8,9}};
        System.out.println(spiralOrder(martix));
    }




    public List<Integer> spiralOrder(int[][] matrix) {

        List<Integer> res = new ArrayList<>();
        if(matrix == null || matrix.length == 0 || matrix[0].length == 0)
            return res;

        int m = matrix.length;
        int n = matrix[0].length;

        int left = 0, right = n - 1, top = 0, bottom = m - 1;

        while(left <= right && top <= bottom){

            //遍历此层最顶行
            for(int i = left; i <= right; i++)
                res.add(matrix[top][i]);
            //遍历此层最右列
            for(int j = top + 1; j <= bottom; j++)
                res.add(matrix[j][right]);
            //如果此层是一个圈（则遍历最底行和最左列）
            if(left < right && top < bottom){
                for(int i = right - 1; i >= left; i--)
                    res.add(matrix[bottom][i]);
                for(int j = bottom + 1; j > top; j--)
                    res.add(matrix[j][left]);
            }

            //进入下一层
            left++; right--;    top++; bottom--;
        }

        return res;
    }





}
