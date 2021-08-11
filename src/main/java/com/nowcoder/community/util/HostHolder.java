package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 作为容器，持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {
    /*
    ThreadLocal可以让每个线程持有一个对象，线程之间相互隔离，保证了线程安全
    底层实现是一个Map，Thread作为key，对象作为value
    客户端向服务器发送请求时，一般一个请求一个线程，我们需要保证每个线程持有的user对其他线程不可见
     */
    private final ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    /*
    请求结束清理对象
     */
    public void clear() {
        users.remove();
    }
}
