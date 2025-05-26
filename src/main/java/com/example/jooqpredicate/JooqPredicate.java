package com.example.jooqpredicate;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JooqPredicate {
    Class<?> table();
}