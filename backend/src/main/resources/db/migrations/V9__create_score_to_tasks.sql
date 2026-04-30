ALTER TABLE tasks ADD COLUMN score INT NOT NULL DEFAULT 1;

CREATE TABLE user_task_scores (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE(user_id, task_id)
);

CREATE INDEX idx_user_task_scores_user_id ON user_task_scores(user_id);
CREATE INDEX idx_user_task_scores_task_id ON user_task_scores(task_id);