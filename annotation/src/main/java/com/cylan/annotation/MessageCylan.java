package com.cylan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Lupy
 * @description 消息注解
 * @since 2019/3/1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MessageCylan {
    int messageId() default -1;
}
