package com.example.jooqpredicate;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Map;

public class JooqPredicateBuilder<T> {

    private final Map<String, Field<?>> fieldMap;

    public JooqPredicateBuilder(Map<String, Field<?>> fieldMap) {
        this.fieldMap = fieldMap;
    }

    @SuppressWarnings("unchecked")
    public Condition build(Map<String, String> queryParams) {
        Condition condition = DSL.trueCondition();

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String rawKey = entry.getKey(); // example: name__like
            String value = entry.getValue();

            String[] parts = rawKey.split("__");
            String fieldName = parts[0];
            String op = parts.length > 1 ? parts[1] : "eq";

            Field<Object> field = (Field<Object>) fieldMap.get(fieldName);
            if (field == null || value == null) continue;

            Condition part = switch (op) {
                case "like" -> field.likeIgnoreCase("%" + value + "%");
                case "eq" -> field.eq(value);
                case "ne" -> field.ne(value);
                case "gt" -> field.gt(value);
                case "lt" -> field.lt(value);
                case "gte" -> field.ge(value);
                case "lte" -> field.le(value);
                default -> field.eq(value);
            };

            condition = condition.and(part);
        }

        return condition;
    }
}