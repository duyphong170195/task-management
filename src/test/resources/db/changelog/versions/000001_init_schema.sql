-- liquibase formatted sql
-- changeset phongnd13:000001_init_schema.sql
-- validCheckSum ANY

CREATE TABLE "user" (
                        "id" uuid PRIMARY KEY NOT NULL,
                        "username" varchar(50) NOT NULL,
                        "full_name" varchar(50) NOT NULL,
                        "created_at" timestamp NOT NULL,
                        "updated_at" timestamp NOT NULL
);

CREATE TABLE "tasks" (
                         "id" uuid PRIMARY KEY NOT NULL,
                         "name" varchar(120) NOT NULL,
                         "created_at" timestamp NOT NULL,
                         "updated_at" timestamp NOT NULL,
                         "status" varchar(20) NOT NULL,
                         "user_id" uuid,
                         "description" TEXT,
                         "task_type" varchar(20) NOT NULL
);

CREATE TABLE "bug" (
                       "task_id" uuid PRIMARY KEY NOT NULL,
                       "severity" varchar(50) NOT NULL,
                       "steps_to_reproduce" TEXT NOT NULL
);

CREATE TABLE "feature" (
                           "task_id" uuid PRIMARY KEY NOT NULL,
                           "business_value" integer NOT NULL,
                           "deadline" timestamp NOT NULL
);



CREATE INDEX idx_tasks_user_id ON "tasks" ("user_id");

CREATE INDEX idx_tasks_status ON "tasks" ("status");

ALTER TABLE "tasks" ADD CONSTRAINT fk_tasks_user_user_id FOREIGN KEY ("user_id") REFERENCES "user" ("id");

ALTER TABLE "feature" ADD CONSTRAINT fk_feature_tasks_task_id FOREIGN KEY ("task_id") REFERENCES "tasks" ("id");

ALTER TABLE "bug" ADD CONSTRAINT fk_bug_tasks_task_id FOREIGN KEY ("task_id") REFERENCES "tasks" ("id");

-- rollback drop table user_management.feature;
-- rollback drop table user_management.bug;
-- rollback drop table user_management.tasks;
-- rollback drop table user_management.user;

