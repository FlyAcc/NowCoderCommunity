package com.nowcoder.community.config;


import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/resources/**"); // 忽略静态资源访问，提高性能
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll() // 其余任意用户均允许
                .and().csrf().disable(); // 不启用csrf

        // 权限不足处理
        http.exceptionHandling()
                // 未登录
                .authenticationEntryPoint((request, response, e) -> {
                    String xRequestedWith = request.getHeader("x-requested-with");
                    // 异步请求
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(CommunityUtil.getJsonString(403, "你还没有登陆！"));
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login");
                    }
                })
                // 权限不足
                .accessDeniedHandler((request, response, e) -> {
                    String xRequestedWith = request.getHeader("x-requested-with");
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(CommunityUtil.getJsonString(403, "你没有访问该功能的权限！"));
                    } else {
                        response.sendRedirect(request.getContextPath() + "/denied");
                    }
                });

        /*
        Spring底层默认会拦截/logout请求，进行退出处理
        需要覆盖其默认逻辑，才能使用原先的logout逻辑
        这里使用一个假的路径？这种处理方法好吗？
         */
        http.logout().logoutUrl("/securitylogout");
    }
}
