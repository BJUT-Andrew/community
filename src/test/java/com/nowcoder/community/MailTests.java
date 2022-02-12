package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author andrew
 * @create 2021-10-15 12:06
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    //注入模板引擎，用于在@Test方法中调用Thymeleaf模板；
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testHtmlMail(){
        //向共享域中传入数据，在前端HTML页面中获取；
        Context context = new Context();
        context.setVariable("username", "lss");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("15603104286@163.com", "TestHTML", content);

    }

    @Test
    public void testTextMail(){
        mailClient.sendMail("15603104286@163.com", "Test", "WelCome");
    }



}
