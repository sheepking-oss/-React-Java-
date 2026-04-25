package com.campus.secondhand.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    String value() default "";

    long expireSeconds() default 60;

    String action() default "";
}
