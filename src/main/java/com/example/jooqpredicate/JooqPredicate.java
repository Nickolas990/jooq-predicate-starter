package com.example.jooqpredicate;

import org.jooq.Table;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JooqPredicate {
    Class<?> table();
}