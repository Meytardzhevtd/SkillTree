-- schema.sql
CREATE TABLE IF NOT EXISTS Users (
    id BIGSERIAL PRIMARY KEY NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'BASE_USER'
);

CREATE TABLE IF NOT EXISTS Courses (
    id BIGSERIAL PRIMARY KEY NOT NULL UNIQUE,
    --     id_user BIGINT NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS TakenCourses (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    id_course BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    progress REAL NOT NULL DEFAULT 0,

    CONSTRAINT uq_taken_course UNIQUE (id_course, id_user),

    CONSTRAINT fk_taken_courses_course
        FOREIGN KEY (id_course)
        REFERENCES Courses(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_taken_courses_user
        FOREIGN KEY (id_user)
        REFERENCES Users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Module (
    id BIGSERIAL PRIMARY KEY,
    id_course BIGINT NOT NULL,
    name TEXT NOT NULL,
    can_be_open BOOLEAN NOT NULL,

    CONSTRAINT fk_module_course
        FOREIGN KEY (id_course)
        REFERENCES Courses(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS TaskTypes (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Tasks (
    id BIGSERIAL PRIMARY KEY,
    id_type_task BIGINT NOT NULL,
    id_module BIGINT NOT NULL,
    content JSONB,

    CONSTRAINT fk_task_type
        FOREIGN KEY (id_type_task)
        REFERENCES TaskTypes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_task_module
        FOREIGN KEY (id_module)
        REFERENCES Module(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS ProgressModule (
    id BIGSERIAL PRIMARY KEY,
    id_module BIGINT NOT NULL,
    id_taken_course BIGINT NOT NULL,
    progress REAL NOT NULL DEFAULT 0,

    CONSTRAINT uq_progress UNIQUE (id_module, id_taken_course),

    CONSTRAINT fk_progress_module
        FOREIGN KEY (id_module)
        REFERENCES Module(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_progress_taken_course
        FOREIGN KEY (id_taken_course)
        REFERENCES TakenCourses(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS UserAnswers (
    id BIGSERIAL PRIMARY KEY,
    id_task BIGINT NOT NULL,
    id_progress_module BIGINT NOT NULL,
    answer JSONB NOT NULL,

    CONSTRAINT fk_answer_task
        FOREIGN KEY (id_task)
        REFERENCES Tasks(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_answer_progress
        FOREIGN KEY (id_progress_module)
        REFERENCES ProgressModule(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Dependencies (
    id BIGSERIAL PRIMARY KEY,
    id_module BIGINT NOT NULL,
    id_block_module BIGINT NOT NULL,

    CONSTRAINT uq_dependence UNIQUE (id_module, id_block_module),

    CONSTRAINT fk_dependence_module
        FOREIGN KEY (id_module)
        REFERENCES Module(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_dependence_block_module
        FOREIGN KEY (id_block_module)
        REFERENCES Module(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Roles (
    id BIGSERIAL PRIMARY KEY,
    id_course BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    course_role TEXT NOT NULL,

    CONSTRAINT uq_role UNIQUE (id_course, id_user),

    CONSTRAINT fk_role_user
        FOREIGN KEY (id_user)
        REFERENCES Users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_role_course
        FOREIGN KEY (id_course)
        REFERENCES Courses(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
