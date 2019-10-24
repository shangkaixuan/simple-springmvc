package com.simple.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @author srh
 * @date 2019/10/23
 **/
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Quaifier {

    String value();

}
