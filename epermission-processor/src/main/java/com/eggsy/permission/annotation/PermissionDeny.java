package com.eggsy.permission.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by eggsy on 16-12-9.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PermissionDeny
{
    int requestCode() default Integer.MIN_VALUE;

    String requestPermission();
}

