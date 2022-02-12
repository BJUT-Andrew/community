package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author andrew
 * @create 2021-10-24 17:17
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class FilterTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testFilter(){

        String text = "★赌★博★、★抽★烟★、★吸★毒★、★嫖★娼★、开票";
        System.out.println(sensitiveFilter.filter(text));

    }

}
