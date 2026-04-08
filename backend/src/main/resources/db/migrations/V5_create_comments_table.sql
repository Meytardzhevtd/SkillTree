CREATE TABLE IF NOT EXISTS comments_task (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_edited BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_comment_task
        FOREIGN KEY (task_id)
        REFERENCES Tasks(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_comment_author
        FOREIGN KEY (author_id)
        REFERENCES Users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX idx_comments_task_id ON comments(task_id);

CREATE INDEX idx_comments_created_at ON comments(created_at);