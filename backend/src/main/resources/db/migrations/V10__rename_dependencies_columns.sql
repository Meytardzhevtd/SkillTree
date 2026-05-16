ALTER TABLE Dependencies RENAME COLUMN id_module TO id_main_module;
ALTER TABLE Dependencies RENAME COLUMN id_block_module TO id_blocked_module;
ALTER TABLE Dependencies RENAME CONSTRAINT fk_dependence_module TO fk_dependence_main_module;
ALTER TABLE Dependencies RENAME CONSTRAINT fk_dependence_block_module TO fk_dependence_blocked_module;