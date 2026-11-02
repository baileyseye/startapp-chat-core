# KODA.md — Правила проекта Startapp Chat Core

Этот файл определяет соглашения и правила для разработки в проекте. Koda CLI следует им при генерации кода и предложений.

---

## 🛠 Стек технологий

- **Язык:** Java 21
- **Фреймворки:** Spring Boot 4.0.6, Vaadin 25.1.5
- **Коммуникация:** gRPC (Spring gRPC), WebSocket, REST
- **Базы данных:** PostgreSQL (prod), H2 (dev), Liquibase (миграции)
- **Мессенджинг:** Apache Kafka / Kafka Streams
- **Мониторинг:** Micrometer, Prometheus, Actuator
- **Безопасность:** Spring Security (OAuth2, базовая)
- **Сборка:** Gradle (Kotlin DSL)

---

## 📐 Кодстайл (на основе IntelliJ IDEA Project.xml)

### Общие правила Java

| Параметр | Значение                                                    |
|----------|-------------------------------------------------------------|
| **Отступ** | 2 пробела                                                   |
| **Продолжение строки** | 4 пробела                                                   |
| **Tab size** | 2 пробела (только пробелы, не табы)                         |
| **Макс. длина строки** | 130 символов                                                |
| **Обёртка параметров метода** | Если не влезает — каждый аргумент с новой строки (wrap = 5) |
| **Обёртка вызовов** | wrap = 1 (если не влезает — с новой строки)                 |
| **Бинарные операции** | Символ операции на следующей строке                         |
| **Тернарные операции** | Символы `?` и `:` на следующей строке                       |
| **Классы/методы** | Одна пустая строка после заголовка класса                   |
| **Пустые строки** | Не более одной подряд                                       |
| **Скобки** | Всегда требуются для `if`, `for`, `while`, `do-while`       |

### Импорты Java

- **Не использовать** `import on demand` (`*`), если не более 999 классов в пакете (фактически — всегда явные импорты)
- **Порядок импортов:**
  1. Статические импорты из проекта (module = true)
  2. Статические импорты из внешних библиотек
  3. Пустая строка
  4. Все остальные импорты (в алфавитном порядке)

### XML (Liquibase, конфиги)

- **Отступ:** 2 пробела
- **Выравнивание атрибутов:** отключено
- **Порядок атрибутов:**
  - `xmlns` (без префикса)
  - `xmlns:.*` (по имени)
  - `id`
  - `style`
  - Остальные по имени

### JSON / Proto

- **Отступ:** 2 пробела
- **Продолжение:** 2 пробела
- **Max line:** 80 символов для `.proto`

---

## 📁 Структура проекта

```
src/
├── main/
│   ├── java/startapp/com/core/
│   │   ├── StartappChatCoreApplication.java  # Точка входа
│   │   ├── security/                         # Конфиги безопасности
│   │   ├── views/                            # Vaadin UI视图
│   │   └── services/                         # Бизнес-логика (будет)
│   ├── resources/
│   │   ├── application.yaml                  # Конфиг приложения
│   │   ├── db/changelog/                     # Liquibase миграции
│   │   │   ├── database-changelog-master.xml
│   │   │   ├── ddl/                          # DDL (структура)
│   │   │   └── dml/                          # DML (данные)
│   │   ├── static/                           # Статика (CSS, JS)
│   │   └── templates/                        # Шаблоны (если нужно)
│   └── proto/                                # .proto файлы для gRPC
└── test/
    └── java/startapp/com/core/               # Тесты
```

---

## 🧪 Тестирование

- **Фреймворк:** JUnit 5
- **Пакет:** `src/test/java/startapp/com/core/...`
- **Сufficx:** `*Test.java`
- **Правила:**
  - Покрывать бизнес-логику (services, repositories)
  - Использовать `@SpringBootTest` для интеграционных тестов
  - Использовать тестовые профили (`@ActiveProfiles("test")`)

---

## 🚀 Сборка и запуск

| Команда | Описание |
|---------|----------|
| `./gradlew build` | Полная сборка + тесты |
| `./gradlew test` | Запуск тестов |
| `./gradlew bootJar` | Сборка JAR |
| `docker-compose up` | Запуск PostgreSQL |

> ⚠️ **КОМАНДА `bootRun` ЗАПРЕЩЕНА.** Koda CLI **НИКОГДА** не должен выполнять, упоминать или предлагать использовать `bootRun` в
> любом виде. Для запуска приложения используйте IntelliJ IDEA Run/Debug конфигурации.

---

## 📝 Соглашения по именованию

| Элемент | Правило | Пример |
|---------|---------|--------|
| **Пакеты** | lowercase, точка-разделитель | `startapp.com.core.views` |
| **Классы** | UpperCamelCase | `HomeView`, `LocalSecurityConfig` |
| **Методы/переменные** | lowerCamelCase | `createUserTable`, `securityFilterChain` |
| **Константы** | UPPER_SNAKE_CASE | `MAX_USERS_LIMIT` |
| **Файлы БД** | `YYYYMM-changelog-[ddl|dml].xml` | `202605-changelog-ddl.xml` |
| **Профили Spring** | `local`, `test`, `prod` | `application-local.yaml` |

| Паттерн                        | Правило                                                                                                                                                                                                   | Пример                                                                                                                                                                                |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Lombok**                     | Всегда использовать аннотации                                                                                                                                                                             | `@Data`, `@RequiredArgsConstructor`, `@Builder`                                                                                                                                       |
| **FieldDefaults**              | **Обязательно** использовать `@FieldDefaults(level = PRIVATE)` во всех DTO и сущностях для сокращения boilerplate-кода (не писать `private` перед каждым полем).                                          | `@Data @FieldDefaults(level = PRIVATE) public class UserDto { Long id; String name; }`                                                                                                |
| **MapStruct**                  | Использовать для маппинга DTO ↔ Entity. Не писать вручную `toDto`/`toEntity`. Создать интерфейс с `@Mapper`                                                                                               | `UserMapper.INSTANCE.toDto(user)`                                                                                                                                                     |
| **DTO для MapStruct**          | Все DTO, используемые в MapStruct, **обязаны иметь конструктор без аргументов** (явный или через `@NoArgsConstructor`). MapStruct использует его для создания экземпляров при маппинге. Пример: `UserDto` | `@Data @NoArgsConstructor @AllArgsConstructor public class UserDto { ... }`                                                                                                           |
| **Явные маппинги MapStruct**   | **ВСЕГДА** указывать явные `@Mapping` для каждого поля, даже если имена совпадают. Это гарантирует корректную работу маппера и предотвращает потерю данных.                                               | `@Mapping(target = "id", source = "id")<br>@Mapping(target = "name", source = "name")<br>@Mapping(target = "age", source = "age")`                                                    |
| **DI через конструктор**       | Использовать `@RequiredArgsConstructor` + `@Autowired` (или только `@RequiredArgsConstructor` для Spring 4+)                                                                                              | `@RequiredArgsConstructor private final UserService service;`                                                                                                                         |
| **Сервисы**                    | **Не создавать интерфейс**, если одна реализация. Использовать `@Service` класс напрямую                                                                                                                  | `@Service public class UserService { ... }`                                                                                                                                           |
| **Транзакции**                 | `@Transactional` **только на методах**, не на классе                                                                                                                                                      | `@Transactional public void save()`                                                                                                                                                   |
| **Репозитории**                | `JpaRepository<Entity, ID>`                                                                                                                                                                               | `UserRepository`                                                                                                                                                                      |
| **Имена методов репозиториев** | **Только camelCase** для методов Spring Data JPA. **Запрещено** использовать snake_case (`_`) в именах методов. Подчеркивания допустимы только для разделения полей объекта (например, `findByUserId`).   | `findByParticipantsId()`, `findByCreatedAtBetween()`                                                                                                                                  |
| **Сущности JPA**               | `@Entity`, `@Id`, `@GeneratedValue`, `@RequiredArgsConstructor`                                                                                                                                           | `UserEntity`                                                                                                                                                                          |
| **Контроллеры**                | `@RestController`, `@RequestMapping`                                                                                                                                                                      | `UserController`                                                                                                                                                                      |
| **Комментарии в DTO/Entity**   | **Запрещено** использовать `//` для комментариев к полям. **Обязательно** использовать Javadoc (`/** ... */`) для описания полей DTO и сущностей.                                                         | `/** Идентификатор чата */<br>@Id private Long id;`                                                                                                                                   |
| **Смысл комментариев в DTO**   | **Не просто повторять имя поля**, а раскрывать его **техническую суть, роль в системе или контекст использования**. Избегать очевидных формулировок.                                                      | **Плохо:** `/** Имя пользователя */`<br>**Хорошо:** `/** Отображаемое имя пользователя для UI */`<br>**Плохо:** `/** Время */`<br>**Хорошо:** `/** Время отправки сообщения в UTC */` |

---

## 🔒 Безопасность

- На профиле `local` — отключать CSRF и авторизацию (см. `LocalSecurityConfig`)
- Секреты **никогда** не коммитить (использовать `application-local.yaml` в `.gitignore`)
- Пароли по умолчанию: `admin/admin` (только для dev)

---

## 📝 Документация контроллеров (Javadoc для Postman)

Для каждого метода в `@RestController` **обязательно** добавлять Javadoc с примером запроса для Postman.

### Шаблон документации метода контроллера

```java
/**
 * HTTP_METHOD http://localhost:8080/полный/путь
 * <br><br>
 * Body:
 * <pre>{@code
 * {
 *   "поле1": "значение1",
 *   "поле2": 123
 * }
 * }</pre>
 */
```

### Правила:

1. **GET/DELETE** — только URL (Body не требуется)
2. **POST/PUT/PATCH** — URL + пример Body в формате JSON
3. Использовать `<pre>{@code ... }</pre>` для форматирования JSON
4. Не добавлять лишние описания (@param, @return) — только пример для Postman
5. Примеры должны быть актуальными для текущей бизнес-логики

### Примеры:

**GET метод:**

```java
/**
 * GET {@code http://localhost:8080/api/users/{id}}
 * <br>
 * Пример: {@code http://localhost:8080/api/users/1}
 */
@GetMapping("/{id}")
public ResponseEntity<UserDto> getUser(@PathVariable Long id) { ...}
```

**POST метод:**

```java
/**
 * POST {@code http://localhost:8080/api/users}
 * <br><br>
 * Body:
 * <pre>{@code
 * {
 *   "name": "Иван Иванов",
 *   "age": 25
 * }
 * }</pre>
 */
@PostMapping
public ResponseEntity<UserDto> createUser(@RequestBody UserDto dto) { ...}
```

**Важно:** Все URL в Javadoc должны быть обернуты в `{@code ...}`, чтобы предотвратить создание гиперссылок и избежать
предупреждений IntelliJ IDEA.

---

## 📚 Документация

- **README.md:** Описание проекта, запуск, архитектура
- **KODA.md:** Настоящий файл (правила для AI и разработчиков)
- **Javadoc:** Для публичных API (если нужно)

---

> ✅ **Koda CLI будет следовать этим правилам при генерации кода, рефакторинге и предложениях.**
