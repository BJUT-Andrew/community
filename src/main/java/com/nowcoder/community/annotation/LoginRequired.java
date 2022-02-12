package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注此方法是否需要在登录状态下才能执行
 * @author andrew
 * @create 2021-10-23 19:34
 */

//表示此注解用于标注方法
@Target(ElementType.METHOD)
//表示此注解在运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {


}
