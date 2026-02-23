# SkillTree

Проект разделен на две части:
- `backend` — Spring Boot API (порт `8080`)
- `frontend` — React + TypeScript (Vite, порт `5173`)

## Как запускать локально

1. Запустить backend:

```bash
cd backend
./mvnw spring-boot:run
```

2. Запустить frontend (в отдельном терминале):

```bash
cd frontend
npm install
npm run dev
```

Frontend настроен через Vite proxy и отправляет запросы на backend по пути `/api`.

## Основные API для MVP личного кабинета

- `GET /api/hello` — проверка связи frontend ↔ backend
- `POST /api/auth/register` — регистрация
- `GET /api/profile/me?email=...` — получить профиль пользователя

## Docker

Файл `docker-compose.yml` находится в папке `backend`.