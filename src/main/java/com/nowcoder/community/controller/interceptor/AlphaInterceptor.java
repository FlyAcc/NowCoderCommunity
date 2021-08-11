package com.nowcoder.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AlphaInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaInterceptor.class);

    /*
    拦截点在handler（一般是controller）执行之前，在HandleMapping之后（HandleMapping选择一个合适的handler）。
    多个handler（包含interceptor）在一条execution chain中，如果返回true，则dispatchServlet继续处理chain，否则终止
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.debug("preHandler: " + handler);
        return true;
    }

    /*
    拦截点在handler（一般是controller）执行之后
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LOGGER.debug("post handle: " + handler);
    }

    /*
    在请求处理完成后之后执行，即渲染完view（这里是TemplateEngine）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LOGGER.debug("after completion: " + handler);
    }
}
