# SkillTree

Минимальный шаблон Spring Boot (Java 21) с запуском через Docker одной командой.

## Что внутри

- Spring Boot 3 (`spring-boot-starter-web`)
- Простой REST-контроллер:
	- `GET /` → `SkillTree API is running`
	- `GET /api/hello` → `Hello from Spring Boot!`
- Multi-stage `Dockerfile`
- `docker-compose.yml` для быстрого старта

## Запуск одной командой

```bash
docker compose up --build
```

После запуска приложение доступно на `http://localhost:8080`.

## Остановка

```bash
docker compose down
```