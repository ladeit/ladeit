package com.ladeit.api.annotation;

import java.lang.annotation.*;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname SlackLogin
 * @Date 2020/3/25 13:35
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SlackLogin {
}
