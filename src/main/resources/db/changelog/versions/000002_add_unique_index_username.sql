-- liquibase formatted sql
-- changeset phongnd13:000002_add_unique_index_username.sql
-- validCheckSum ANY

CREATE UNIQUE INDEX ui_user_username ON "user" ("username");

-- rollback drop index ui_user_username;
