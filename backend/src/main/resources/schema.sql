-- schema.sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'BASE_USER',
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    id_user BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS taken_courses (
    id BIGSERIAL PRIMARY KEY,
    id_course BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    id_user BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    progress REAL NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS module (
    id BIGSERIAL PRIMARY KEY,
    id_course BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    dependences INT
);

CREATE TABLE IF NOT EXISTS task_types (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    id_type_task BIGINT NOT NULL REFERENCES task_types(id),
    id_module BIGINT NOT NULL REFERENCES module(id) ON DELETE CASCADE,
    content JSONB
);

CREATE TABLE IF NOT EXISTS progress_module (
    id BIGSERIAL PRIMARY KEY,
    id_module BIGINT NOT NULL REFERENCES module(id) ON DELETE CASCADE,
    id_taken_course BIGINT NOT NULL REFERENCES taken_courses(id) ON DELETE CASCADE,
    progress REAL NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_answers (
    id BIGSERIAL PRIMARY KEY,
    id_task BIGINT NOT NULL REFERENCES tasks(id),
    id_progress_module BIGINT NOT NULL REFERENCES progress_module(id) ON DELETE CASCADE,
    answer JSONB NOT NULL
);