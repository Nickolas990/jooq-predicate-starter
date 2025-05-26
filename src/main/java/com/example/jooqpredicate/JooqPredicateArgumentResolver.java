package com.example.jooqpredicate;

import jakarta.servlet.http.HttpServletRequest;
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
        return parameter.hasParameterAnnotation(JooqPredicate.class) &&
               Condition.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        JooqPredicate annotation = parameter.getParameterAnnotation(JooqPredicate.class);
        Class<?> tableClass = annotation.table();

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

        Map<String, Field<?>> fieldMap = Arrays.stream(table.fields())
                .collect(Collectors.toMap(Field::getName, java.util.function.Function.identity()));

        Map<String, String[]> paramMap = ((HttpServletRequest) webRequest.getNativeRequest()).getParameterMap();
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            if (entry.getValue().length > 0) {
                values.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        Object dto = mapToDto(parameter.getParameterType(), values);

        @SuppressWarnings("unchecked")
        JooqPredicateBuilder<Object> builder = new JooqPredicateBuilder<>(fieldMap);
        return builder.build((Map<String, String>) dto);
    }

    private Object mapToDto(Class<?> dtoClass, Map<String, Object> values) {
        try {
            Object dto = dtoClass.getDeclaredConstructor().newInstance();
            for (java.lang.reflect.Field field : dtoClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = values.get(field.getName());
                if (value != null) {
                    field.set(dto, convertTo(value.toString(), field.getType()));
                }
            }
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to bind DTO", e);
        }
    }

    private Object convertTo(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == Integer.class || type == int.class) return Integer.valueOf(value);
        if (type == Long.class || type == long.class) return Long.valueOf(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.valueOf(value);
        return null;
    }
}