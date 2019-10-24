package com.simple.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @author srh
 * @date 2019/10/23
 **/
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    String value();

}
