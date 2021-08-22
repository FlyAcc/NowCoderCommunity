package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

    public static void main(String[] args) {
        // 自动创建了Spring容器，自动扫描bean，自动装配
        // @SpringBootApplication:@SpringBootConfiguration, @EnableAutoConfiguration, @ComponentScan
        // 该类有SpringBootConfiguration注解，相当于一个配置类，Bean扫描时会扫描配置类所在的包（以及子包）
        // 被扫描的bean类需要注解@Controller,  @Repository, @Service，@Component,（前三个注解都包含了@Component）
        SpringApplication.run(CommunityApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // 解决netty启动冲突的问题（es和redis冲突）
        // 见Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

}
