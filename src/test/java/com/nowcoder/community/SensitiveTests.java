package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 测试类和主类同个配置
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    void testSensitiveFilter() {
        String text = "你们好赌博、吸毒、嫖娼、开票可能行赌博";
        text = sensitiveFilter.filter(text);
        assertEquals("你们好**、**、**、**可能行**", text);

        text = "赌⭐博、吸⭐毒、嫖⭐娼、开⭐→票你们好";
        text = sensitiveFilter.filter(text);
        assertEquals("***、***、***、****你们好", text);
    }
}
