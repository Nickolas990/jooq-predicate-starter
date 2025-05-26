package com.example.jooqpredicate;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JooqPredicateArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JooqPredicate.class)
                && Condition.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        JooqPredicate annotation = parameter.getParameterAnnotation(JooqPredicate.class);
        Class<?> tableClass = annotation.table();

        // Получаем статическое поле типа Table<?> (например, Users.USERS)
        Table<?> table = Arrays.stream(tableClass.getFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()) && Table.class.isAssignableFrom(f.getType()))
                .findFirst()
                .map(f -> {
                    try {
                        return (Table<?>) f.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cannot access table field", e);
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("Table class must expose a static Table field"));

        // Получаем карту fieldName -> Field
        Map<String, Field<?>> fieldMap = Arrays.stream(table.fields())
                .collect(Collectors.toMap(Field::getName, java.util.function.Function.identity()));

        // Собираем query-параметры вида key -> value
        Map<String, String> queryParams = new HashMap<>();
        webRequest.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                queryParams.put(key, values[0]);
            }
        });

        JooqPredicateBuilder<Object> builder = new JooqPredicateBuilder<>(fieldMap);

        return builder.build(queryParams);
    }
}