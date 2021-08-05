package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 测试类和主类同个配置
class CommunityApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 测试通过容器获取bean
     */
    @Test
    public void testApplicationContext() {
        System.out.println(applicationContext);
        // 面向接口，可以方便更改实现
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class); // 获取高优先级的bean
        System.out.println(alphaDao.select());
        alphaDao = applicationContext.getBean("alphaHibernate", AlphaDao.class); // 根据名字获取bean
        System.out.println(alphaDao.select());
    }
}
