-- V2__insert_task_types.sql
INSERT INTO tasktypes (id, name) VALUES (1, 'ONE_POSSIBLE_ANSWER') ON CONFLICT (id) DO NOTHING;
INSERT INTO tasktypes (id, name) VALUES (2, 'MULTIPLE') ON CONFLICT (id) DO NOTHING;