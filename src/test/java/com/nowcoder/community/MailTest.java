package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine engine;

    @Test
    void testTextMail() {
        mailClient.sendMail("xxxx@qq.com", "Test Spring Mail", "Hello World!");
    }

    @Test
    void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "monday");
        String content = engine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("xxxx@qq.com", "Test HTML Content", content);
    }
}
