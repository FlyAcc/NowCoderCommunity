package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 */
@Target(ElementType.METHOD) // 该注解能够应用的对象
@Retention(RetentionPolicy.RUNTIME) // 该注解能够保留多长时间
public @interface LoginRequired {

}
