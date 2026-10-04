# SkillTree Backend

REST API платформы для создания и прохождения образовательных курсов. Курс состоит из модулей, связанных графом зависимостей: следующий модуль открывается после освоения предыдущих.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791)
![License](https://img.shields.io/badge/status-active-lightgrey)

---

## Содержание

- [Возможности](#возможности)
- [Стек](#стек)
- [Быстрый старт](#быстрый-старт)
- [Конфигурация](#конфигурация)
- [Доменная модель](#доменная-модель)
- [API](#api)
- [Импорт курса](#импорт-курса)
- [Тестирование](#тестирование)
- [Структура проекта](#структура-проекта)
- [Разработка](#разработка)

---

## Возможности

- Регистрация и аутентификация по JWT
- Курсы, модули, уроки и задания с ролевым доступом внутри курса (`admin` / `student`)
- Граф зависимостей между модулями с защитой от циклов
- Автоматический расчёт прогресса по модулю и курсу
- Задания двух типов: один вариант ответа, несколько вариантов
- Баллы пользователя за решённые задания
- Комментарии к заданиям
- Загрузка аватаров в MinIO
- Полнотекстовый нечёткий поиск курсов (`pg_trgm`)
- Импорт курса из JSON
- Swagger UI

## Стек

| Область        | Технологии                                  |
|----------------|---------------------------------------------|
| Язык / runtime | Java 21 (Temurin)                           |
| Фреймворк      | Spring Boot 3.5, Spring Web, Spring Security|
| Данные         | Spring Data JPA, PostgreSQL 15, Flyway      |
| Хранилище      | MinIO                                       |
| Аутентификация | JWT (jjwt 0.12)                             |
| Маппинг        | MapStruct, Lombok                           |
| Документация   | springdoc-openapi                           |
| Тесты          | JUnit 5, Mockito, Testcontainers            |
| Сборка         | Maven, formatter-maven-plugin               |

## Быстрый старт

Требуется Docker и Docker Compose.

```bash
docker compose up --build
```

| Сервис        | Адрес                                   |
|---------------|-----------------------------------------|
| API           | http://localhost:8080                   |
| Swagger UI    | http://localhost:8080/swagger-ui.html   |
| PostgreSQL    | `localhost:5432`                        |
| MinIO API     | http://localhost:9000                   |
| MinIO Console | http://localhost:9001                   |

Миграции Flyway применяются автоматически при старте. Бакет `skilltree-images` создаётся сервисом `minio-init`.

### Локальный запуск без Docker

Нужны JDK 21+, Maven, запущенные PostgreSQL и MinIO.

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/skilltree
export MINIO_ENDPOINT=http://localhost:9000
mvn spring-boot:run
```

## Конфигурация

Параметры задаются в `application.properties` и переопределяются переменными окружения.

| Параметр                      | Переменная                     | По умолчанию                           |
|-------------------------------|--------------------------------|----------------------------------------|
| `spring.datasource.url`       | `SPRING_DATASOURCE_URL`        | `jdbc:postgresql://db:5432/skilltree`  |
| `spring.datasource.username`  | `SPRING_DATASOURCE_USERNAME`   | `postgres`                             |
| `spring.datasource.password`  | `SPRING_DATASOURCE_PASSWORD`   | см. `application.properties`           |
| `jwt.secret`                  | `JWT_SECRET`                   | см. `application.properties`           |
| `jwt.expiration-ms`           | `JWT_EXPIRATION_MS`            | `3600000` (1 час)                      |
| `minio.endpoint`              | `MINIO_ENDPOINT`               | `http://minio:9000`                    |
| `minio.access-key`            | `MINIO_ACCESS_KEY`             | `minioadmin`                           |
| `minio.secret-key`            | `MINIO_SECRET_KEY`             | см. `application.properties`           |
| `minio.bucket-name`           | `MINIO_BUCKET_NAME`            | `skilltree-images`                     |

> **Важно.** Значения по умолчанию предназначены только для разработки. Перед развёртыванием замените пароль БД, `jwt.secret` и ключи MinIO, не храните их в репозитории.

CORS разрешён для `http://localhost:5173` (frontend).

## Доменная модель

```
Users ──< Roles >── Courses ──< Module ──< Task ──< UserAnswers
  │                    │          │  │                   │
  │                    │          │  └──< Lesson         │
  └──< TakenCourses >──┘          │                      │
            │                     └─ Dependencies        │
            └──< ProgressModule >─────────────────────────┘
```

- **Course** содержит модули. Создатель получает роль `admin`, записавшийся студент — `student`.
- **Module** — тема курса. Хранит позицию на графе (`positionX`, `positionY`) и признак `can_be_open`.
- **Dependencies** — ориентированное ребро `mainModule → blockedModule`. Добавление и изменение зависимости проверяется на циклы.
- **Task** — задание с контентом в JSONB и весом (`score`, по умолчанию 10).
- **TakenCourses / ProgressModule** — прогресс студента по курсу и модулям, в процентах от суммарного веса заданий.
- **UserAnswers** — история всех попыток. Баллы начисляются один раз за задание.

### Правила

- Модуль доступен студенту, если прогресс по всем блокирующим модулям достиг **97%**.
- Редактирование курса, модулей и заданий доступно только `admin` курса.
- При добавлении, удалении задания или изменении веса прогресс пересчитывается.

### Типы заданий

| ID | Тип                   | Контент                                         | Формат ответа    |
|----|-----------------------|-------------------------------------------------|------------------|
| 1  | `ONE_POSSIBLE_ANSWER` | `question`, `options`, `indexCorrectAnswer`     | индекс (`int`)   |
| 2  | `MULTIPLE`            | `question`, `options`, `correctAnswers`         | массив индексов  |

## API

Базовый путь: `/api`. Все эндпоинты, кроме `/api/auth/**` и Swagger, требуют заголовок:

```
Authorization: Bearer <token>
```

Интерактивная документация: `/swagger-ui.html`.

### Auth и профиль

| Метод | Путь                  | Описание                          |
|-------|-----------------------|-----------------------------------|
| POST  | `/auth/register`      | Регистрация                       |
| POST  | `/auth/login`         | Вход, возвращает JWT              |
| GET   | `/auth/me`            | ID текущего пользователя          |
| GET   | `/profile/me`         | Профиль и сумма баллов            |
| PUT   | `/profile/me`         | Смена имени пользователя          |
| POST  | `/avatar/upload`      | Загрузка аватара (multipart)      |
| GET   | `/avatar/me`          | Ссылка на текущий аватар          |

### Курсы

| Метод  | Путь                    | Описание                                  |
|--------|-------------------------|-------------------------------------------|
| POST   | `/course`               | Создать курс                              |
| GET    | `/course/{id}`          | Курс с модулями                           |
| GET    | `/course/all?search=`   | Список курсов, опциональный поиск         |
| GET    | `/course/my/{role}`     | Мои курсы по роли                         |
| GET    | `/course/{id}/my-role`  | Моя роль в курсе                          |
| PUT    | `/course/update/{id}`   | Обновить курс                             |
| DELETE | `/course/delete/{id}`   | Удалить курс (только `admin`)             |
| POST   | `/take/course`          | Записаться на курс                        |
| GET    | `/take/course/my`       | Пройденные курсы с прогрессом             |
| GET    | `/user/courses`         | Курсы пользователя                        |
| GET    | `/user/my-courses`      | Курсы, созданные пользователем            |

### Модули и зависимости

| Метод  | Путь                                       | Описание                             |
|--------|--------------------------------------------|--------------------------------------|
| POST   | `/module`                                  | Создать модуль                       |
| GET    | `/module/{id}`                             | Модуль со списком заданий            |
| GET    | `/module/courses/{id}`                     | Модули курса                         |
| PUT    | `/module/update/{id}`                      | Обновить модуль                      |
| POST   | `/module/position`                         | Сохранить позицию на графе           |
| POST   | `/module/{id}/start?takenCourseId=`        | Начать модуль                        |
| DELETE | `/module/{id}`                             | Удалить модуль                       |
| POST   | `/dependencies/{mainId}/{blockedId}`       | Создать зависимость                  |
| PUT    | `/dependencies/update/{id}`                | Изменить зависимость                 |
| DELETE | `/dependencies/{id}`                       | Удалить зависимость                  |
| GET    | `/dependencies/graph/{courseId}`           | Граф курса (конструктор)             |
| GET    | `/dependencies/graph/takenCourse/{id}`     | Граф студента с признаком `isOpen`   |

### Уроки, задания, комментарии, баллы

| Метод  | Путь                              | Описание                              |
|--------|-----------------------------------|---------------------------------------|
| POST   | `/lessons`                        | Создать урок                          |
| GET    | `/lessons/{id}`                   | Урок                                  |
| GET    | `/lessons/module/{moduleId}`      | Уроки модуля                          |
| PUT    | `/lessons/{id}`                   | Обновить урок                         |
| DELETE | `/lessons/{id}`                   | Удалить урок                          |
| POST   | `/tasks`                          | Создать задание                       |
| GET    | `/tasks/{id}`                     | Задание                               |
| GET    | `/tasks?moduleId=&progressModuleId=` | Задания модуля со статусом решения |
| PUT    | `/tasks/{id}`                     | Обновить задание                      |
| DELETE | `/tasks/{id}`                     | Удалить задание                       |
| POST   | `/tasks/{id}/submit`              | Отправить ответ                       |
| POST   | `/comments`                       | Добавить комментарий                  |
| GET    | `/comments/task/{taskId}`         | Комментарии к заданию                 |
| GET    | `/scores/{userId}`                | Сумма баллов пользователя             |
| PUT    | `/scores/change/{taskId}/{score}` | Изменить вес задания                  |

### Пример: отправка ответа

```http
POST /api/tasks/42/submit
Content-Type: application/json
Authorization: Bearer <token>

{
  "progressModuleId": 7,
  "answer": 2
}
```

```json
{
  "correct": true,
  "alreadySolved": false,
  "message": "Верно!",
  "moduleProgress": 33.3,
  "tasks": [{ "taskId": 42, "isCompleted": true }]
}
```

## Импорт курса

`POST /api/import-files/json-course` (multipart, поле `file`). Возвращает `{ "courseId": <id> }`.

```json
{
  "name": "Java Basics",
  "description": "Введение в Java",
  "modules": [
    {
      "name": "Типы данных",
      "canBeOpen": true,
      "lessons": [
        { "title": "Примитивы", "content": "..." }
      ],
      "tasks": [
        {
          "taskTypeId": 1,
          "score": 10,
          "question": "Размер int?",
          "options": ["8", "16", "32", "64"],
          "correctIndex": 2
        },
        {
          "taskTypeId": 2,
          "question": "Какие типы примитивные?",
          "options": ["int", "String", "double"],
          "correctAnswers": [0, 2]
        }
      ]
    }
  ]
}
```

## Тестирование

```bash
mvn test
```

| Набор                       | Тип         | Требования        |
|-----------------------------|-------------|-------------------|
| `DependenciesTestUnit`      | unit        | —                 |
| `DependencyTestIntegration` | integration | запущенный Docker |

Интеграционные тесты поднимают PostgreSQL через Testcontainers и проверяют производительность построения графа зависимостей.

## Структура проекта

```
src/main/java/com/skilltree
├── config/        Security, JWT-фильтр, MinIO, Jackson
├── controller/    REST-контроллеры
├── Service/       Бизнес-логика
├── repository/    Spring Data репозитории
├── model/         JPA-сущности
├── dto/           Запросы и ответы
├── mapper/        MapStruct
└── exception/     Исключения и обработчик ошибок

src/main/resources
├── application.properties
└── db/migrations/ Flyway-миграции (V1…V12)
```

## Разработка

- Схема БД управляется только Flyway (`ddl-auto=none`). Изменения оформляются новой миграцией `V{N}__description.sql`.
- Код форматируется автоматически при сборке (`formatter-maven-plugin`, конфигурация в `eclipse-formatter.xml`, ширина строки 100).
- Для сбора контекста репозитория в один файл используйте `./collect_repo.sh [output]`.

## Известные ограничения

- В `pom.xml` указан `java.version=17`, образ собирается на JDK 21: приведите значения к одному.
- Эндпоинты `/api/dependencies/**`, `/api/lessons/**`, `/api/scores/**` проверяют только факт аутентификации, но не роль в курсе.
- Валидация `RegisterRequest` отключена в `AuthController` (`@Valid` не указан).
- URL файлов MinIO формируется с хостом `localhost:9000`.