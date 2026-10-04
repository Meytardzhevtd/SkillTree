#!/usr/bin/env bash
set -euo pipefail

BASE="http://localhost:8080"
EMAIL="load@test.com"
USERNAME="loaduser"
PASSWORD="Passw0rd!"

echo "=== Регистрация пользователя ==="
curl -s -X POST "$BASE/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  || true

echo
echo "=== Логин, получение токена ==="
LOGIN_RESPONSE=$(curl -s -X POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
USER_ID=$(echo "$LOGIN_RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

if [ -z "$TOKEN" ]; then
  echo "Не удалось получить токен. Ответ:"
  echo "$LOGIN_RESPONSE"
  exit 1
fi

echo "TOKEN получен (первые 30 символов): ${TOKEN:0:30}..."
echo "USER_ID: $USER_ID"

AUTH="Authorization: Bearer $TOKEN"
JSON="Content-Type: application/json"

echo
echo "=== Создание курса ==="
COURSE_RESPONSE=$(curl -s -X POST "$BASE/api/course" \
  -H "$AUTH" -H "$JSON" \
  -d '{"name":"Load Test Course","description":"for load testing"}')
COURSE_ID=$(echo "$COURSE_RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
echo "COURSE_ID: $COURSE_ID"

echo
echo "=== Создание модуля ==="
MODULE_RESPONSE=$(curl -s -X POST "$BASE/api/module" \
  -H "$AUTH" -H "$JSON" \
  -d "{\"courseId\":$COURSE_ID,\"name\":\"Module 1\",\"can_be_open\":true}")
MODULE_ID=$(echo "$MODULE_RESPONSE" | sed -n 's/.*"moduleId":\([0-9]*\).*/\1/p')
echo "MODULE_ID: $MODULE_ID"

echo
echo "=== Создание задачи ==="
TASK_RESPONSE=$(curl -s -X POST "$BASE/api/tasks" \
  -H "$AUTH" -H "$JSON" \
  -d "{\"taskTypeId\":1,\"moduleId\":$MODULE_ID,\"score\":10,\"content\":{\"type\":\"ONE_POSSIBLE_ANSWER\",\"question\":\"2+2?\",\"options\":[\"3\",\"4\",\"5\"],\"indexCorrectAnswer\":1}}")
TASK_ID=$(echo "$TASK_RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
echo "TASK_ID: $TASK_ID"

echo
echo "=== Запись на курс ==="
curl -s -X POST "$BASE/api/take/course" \
  -H "$AUTH" -H "$JSON" \
  -d "{\"courseId\":$COURSE_ID,\"userId\":$USER_ID}" > /dev/null
echo "OK"

echo
echo "=== Достаём progressModuleId из БД ==="
PROGRESS_MODULE_ID=$(docker exec skilltree-db psql -U postgres -d skilltree -t -A \
  -c "SELECT id FROM progressmodule WHERE id_module = $MODULE_ID LIMIT 1;")
echo "PROGRESS_MODULE_ID: $PROGRESS_MODULE_ID"

echo
echo "=========================================="
echo "Итоговые данные для нагрузочного теста:"
echo "TOKEN=$TOKEN"
echo "USER_ID=$USER_ID"
echo "COURSE_ID=$COURSE_ID"
echo "MODULE_ID=$MODULE_ID"
echo "TASK_ID=$TASK_ID"
echo "PROGRESS_MODULE_ID=$PROGRESS_MODULE_ID"
echo "=========================================="

cat > .loadtest.env <<EOF
TOKEN=$TOKEN
USER_ID=$USER_ID
COURSE_ID=$COURSE_ID
MODULE_ID=$MODULE_ID
TASK_ID=$TASK_ID
PROGRESS_MODULE_ID=$PROGRESS_MODULE_ID
EOF

echo
echo "Данные сохранены в .loadtest.env"