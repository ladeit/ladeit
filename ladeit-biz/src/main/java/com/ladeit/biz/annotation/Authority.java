package com.ladeit.biz.annotation;

import java.lang.annotation.*;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname Authority
 * @Date 2020/2/7 9:59
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authority {
    //权限验证类型 服务组group，服务service，集群cluster,命名空间env
    String type() default "";
    //权限级别RWX
    String level() default "";
}
