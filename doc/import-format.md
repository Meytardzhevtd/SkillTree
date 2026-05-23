# Импорт курса из JSON

## Обзор

Функция импорта позволяет администраторам создавать полноценные курсы с модулями, уроками и задачами, загружая JSON-файл в один клик.


## Доступ к функции

1. Войдите в систему как пользователь с ролью `admin`
2. Перейдите на страницу **"Мои курсы"**
3. Активируйте вкладку **"Мои курсы (созданные)"**
4. Нажмите кнопку **"📁 Импорт курса из JSON"**
5. Выберите JSON-файл, соответствующий описанной ниже схеме

После успешного импорта новый курс появится в списке ваших курсов.

---

## Формат JSON

### Корневая структура

| Поле | Тип | Обязательность | Описание |
|------|-----|----------------|----------|
| `name` | string | **Да** | Название курса |
| `description` | string | Нет | Описание курса (поддерживается Markdown) |
| `modules` | array | **Да** | Массив модулей курса |

### Структура модуля

| Поле | Тип | Обязательность | Описание |
|------|-----|----------------|----------|
| `name` | string | **Да** | Название модуля |
| `canBeOpen` | boolean | Нет (по умолчанию `false`) | Доступен ли модуль сразу после записи на курс |
| `lessons` | array | Нет | Массив уроков в модуле |
| `tasks` | array | Нет | Массив задач в модуле |

### Структура урока

| Поле | Тип | Обязательность | Описание |
|------|-----|----------------|----------|
| `title` | string | **Да** | Название урока |
| `content` | string | **Да** | Содержание урока (поддерживается Markdown) |

### Структура задачи

| Поле | Тип | Обязательность | Описание |
|------|-----|----------------|----------|
| `taskTypeId` | integer | **Да** | `1` — один правильный ответ, `2` — множественный выбор |
| `question` | string | **Да** | Текст вопроса |
| `options` | array | **Да** | Массив вариантов ответа (минимум 2) |
| `correctIndex` | integer | Да (для типа 1) | Индекс правильного ответа (начиная с 0) |
| `correctAnswers` | array | Да (для типа 2) | Массив индексов правильных ответов |
| `score` | integer | Нет (по умолчанию 10) | Количество баллов за верное решение |

---

## Пример

Ниже представлен пример курса по Spring Boot с двумя модулями, четырьмя уроками и четырьмя задачами.

```json
{
  "name": "Spring Boot: от новичка до профессионала",
  "description": "Практический курс по разработке веб-приложений на Spring Boot с нуля.",
  "modules": [
    {
      "name": "Введение в Spring Boot",
      "canBeOpen": true,
      "lessons": [
        {
          "title": "Что такое Spring Boot?",
          "content": "# Введение\n\nSpring Boot — это фреймворк для упрощения разработки Java-приложений.\n\n## Ключевые особенности\n\n- Автоконфигурация\n- Встроенный сервер (Tomcat, Jetty)\n- Минимальные настройки\n- Готов к production"
        },
        {
          "title": "Создание первого приложения",
          "content": "## Использование Spring Initializr\n\n1. Перейдите на [start.spring.io](https://start.spring.io)\n2. Выберите:\n   - Project: Maven\n   - Language: Java\n   - Spring Boot: 3.2.x\n3. Добавьте зависимости:\n   - Spring Web\n   - Spring Data JPA\n   - PostgreSQL Driver\n\n## Код контроллера\n\n```java\n@RestController\npublic class DemoController {\n    @GetMapping(\"/hello\")\n    public String hello() {\n        return \"Hello, Spring Boot!\";\n    }\n}\n```"
        }
      ],
      "tasks": [
        {
          "taskTypeId": 1,
          "question": "Какой встроенный веб-сервер используется в Spring Boot по умолчанию?",
          "options": ["Jetty", "Tomcat", "Undertow"],
          "correctIndex": 1,
          "score": 10
        },
        {
          "taskTypeId": 1,
          "question": "Какая аннотация делает класс REST-контроллером?",
          "options": ["@Controller", "@RestController", "@Service"],
          "correctIndex": 1,
          "score": 10
        }
      ]
    },
    {
      "name": "Spring Data JPA",
      "canBeOpen": false,
      "lessons": [
        {
          "title": "Работа с базами данных",
          "content": "# Spring Data JPA\n\nSpring Data JPA — это абстракция над JPA/Hibernate.\n\n## Основные аннотации\n\n| Аннотация | Назначение |\n|-----------|------------|\n| `@Entity` | Объявляет класс сущностью |\n| `@Id` | Указывает первичный ключ |\n| `@GeneratedValue` | Настраивает автогенерацию ID |\n| `@Column` | Настраивает маппинг колонки |"
        },
        {
          "title": "Репозитории",
          "content": "## JpaRepository\n\n```java\n@Repository\npublic interface UserRepository extends JpaRepository<User, Long> {\n    Optional<User> findByEmail(String email);\n    \n    @Query(\"SELECT u FROM User u WHERE u.email LIKE %:domain\")\n    List<User> findByEmailDomain(@Param(\"domain\") String domain);\n}\n```\n\nМетоды репозитория:\n- `save()` — сохранение или обновление\n- `findById()` — поиск по ID\n- `findAll()` — получение всех записей\n- `deleteById()` — удаление по ID\n- `existsById()` — проверка существования"
        }
      ],
      "tasks": [
        {
          "taskTypeId": 2,
          "question": "Какие аннотации используются для маппинга JPA сущностей?",
          "options": ["@Entity", "@Table", "@RestController", "@Column"],
          "correctAnswers": [0, 1, 3],
          "score": 20
        },
        {
          "taskTypeId": 1,
          "question": "Какой интерфейс расширяет репозиторий для получения базовых CRUD-операций?",
          "options": ["CrudRepository", "JpaRepository", "Оба варианта"],
          "correctIndex": 2,
          "score": 15
        }
      ]
    }
  ]
}