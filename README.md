# JOOQ Predicate Starter

Универсальная библиотека для автоматического построения `org.jooq.Condition` из DTO, 
аналогичная `@QuerydslPredicate`, но для JOOQ и Spring Web MVC.

## Особенности

- Аннотация `@JooqPredicate(table = YourTable.class)` для параметра контроллера.
- Автоматическое связывание query-параметров с DTO.
- Построение `Condition` на лету.
- Поддержка операторов прямо в query string: `name__like=alex`, `age__gte=18`
- Поддержка `Pageable` (`page`, `size`, `sort`)

## Установка

Скопируй проект в свою структуру или подключи как зависимость локально.

## Использование

### 1. DTO

```java
public record UserDto(String name, Integer age, Boolean active) {}
```

### 2. Контроллер

```java
@GetMapping("/users")
public Flux<User> getUsers(@JooqPredicate(table = Users.class) Condition condition, Pageable pageable) {
    return userRepository.findBy(condition, pageable);
}
```

### 3. Запросы

```http
GET /users?name__like=alex&age__gte=18&page=0&size=10&sort=age,desc
```

### Поддерживаемые операторы

| Суффикс     | Описание         |
|-------------|------------------|
| `__eq`      | Равно (по умолчанию) |
| `__ne`      | Не равно         |
| `__like`    | LIKE %значение%  |
| `__gt`      | Больше           |
| `__lt`      | Меньше           |
| `__gte`     | Больше или равно |
| `__lte`     | Меньше или равно |

## Интеграция с Pageable

Spring автоматически распознаёт `Pageable` как аргумент контроллера.
Ты можешь использовать параметры `page`, `size`, `sort`.

Пример:
```
GET /users?active=true&page=1&size=5&sort=name,asc
```

## План развития

- [x] Поддержка `Pageable`
- [ ] Поддержка `IN`, `NOT IN`
- [ ] Аннотации для DTO (`@Like`, `@Gte`, `@Ignore`)
- [ ] Автоконфигурация через `@EnableJooqPredicateSupport`

## Лицензия

MIT