package com.example;

import java.lang.annotation.*;


@Target({ ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomAnnotation {

}
