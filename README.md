<div align="center">

# 🌲 SkillTree

### Платформа для онлайн-обучения

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

**SkillTree** — веб-платформа для создания и прохождения онлайн-курсов.  
Каждый желающий может опубликовать свой курс — строй дерево навыков, урок за уроком.

[Запуск](#-запуск) · [Структура проекта](#-структура-проекта) · [Команда](#-команда)

</div>

---

## 📋 О проекте

SkillTree позволяет преподавателям создавать структурированные курсы с модулями, уроками и заданиями, а студентам — записываться на курсы, отслеживать прогресс и оставлять комментарии. Проект реализован как полноценное веб-приложение с бэкендом на Java и фронтендом на React.

**Ключевые возможности:**
- Создание курсов с модулями, уроками и блоками контента
- Система заданий с приёмом ответов от студентов
- Запись на курс и отслеживание прогресса
- Комментарии к урокам
- JWT-аутентификация и ролевая модель доступа

> Проект находится в активной разработке в рамках семестрового проекта.

---

## 🛠 Технологический стек

| Слой | Технологии |
|---|---|
| **Бэкенд** | Java 21, Spring Boot 3.x, Spring Data JPA / Hibernate |
| **База данных** | PostgreSQL 15 |
| **Миграции** | Flyway |
| **Сборка** | Maven |
| **Фронтенд** | React 19, TypeScript |
| **Контейнеризация** | Docker, Docker Compose |
| **Тестирование** | JUnit 5, Mockito |

---

## 🚀 Запуск

### Требования

- Java 21+
- Maven 3.9+
- Node.js 20+ и npm
- Docker и Docker Compose

### Запуск через Docker (рекомендуется)

```bash
git clone https://github.com/your-org/SkillTree.git
cd SkillTree
```

**Терминал 1 — бэкенд + база данных:**
```bash
cd backend
docker-compose up --build
```

**Терминал 2 — фронтенд:**
```bash
cd frontend
npm install
npm run dev
```

| Сервис | Адрес |
|---|---|
| Фронтенд | http://localhost:5173 |
| API бэкенда | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

### Переменные окружения

Создайте файл `backend/.env` на основе примера:

```env
DB_URL=jdbc:postgresql://localhost:5432/skilltree
DB_USERNAME=postgres
DB_PASSWORD=ваш_пароль
JWT_SECRET=ваш_секретный_ключ
JWT_EXPIRATION=86400000
```

---

## 🗂 Структура проекта

```
SkillTree/
├── backend/
│   └── src/
│       ├── main/java/com/skilltree/
│       │   ├── controller/        # REST-контроллеры
│       │   ├── Service/           # Бизнес-логика
│       │   ├── repository/        # Spring Data JPA репозитории
│       │   ├── model/             # JPA-сущности
│       │   ├── dto/               # DTO запросов и ответов
│       │   │   ├── courses/
│       │   │   ├── lessons/
│       │   │   ├── module/
│       │   │   ├── tasks/
│       │   │   ├── comments/
│       │   │   ├── content/
│       │   │   └── takeCourse/
│       │   ├── mapper/            # Маперы Entity ↔ DTO
│       │   ├── config/            # Security, CORS, бины
│       │   └── exception/         # Глобальная обработка ошибок
│       └── resources/db/migrations/  # SQL-миграции Flyway
│
└── frontend/
    └── src/
        ├── pages/                 # Страницы (уровень роутов)
        ├── components/            # Переиспользуемые компоненты
        ├── layouts/               # Обёртки макетов страниц
        ├── services/              # API-вызовы через Axios
        └── App.tsx
```

---

## 🧪 Запуск тестов

```bash
cd backend
mvn test
```

Тесты находятся в `src/test/java/com/skilltree/` и покрывают слои сервисов и контроллеров.

---

## 👥 Команда

| Имя | GitHub |
|---|---|
| Матвей Баженов | [@MatveyBazhenov](https://github.com/MatveyBazhenov) | 
| Тимофей Мейтарджев | [@Meytardzhevtd](https://github.com/Meytardzhevtd) |
| Андрей Ким | [@arkaoi](https://github.com/arkaoi) | 

---

<div align="center">
  <sub>Семестровый проект · Высшая школа экономики · Java · 2025</sub>
</div>