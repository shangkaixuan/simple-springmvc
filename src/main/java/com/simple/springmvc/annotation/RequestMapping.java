package com.simple.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @author srh
 * @date 2019/10/23
 **/
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value();

}
