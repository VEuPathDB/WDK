/*==============================================================================
 * This SQL script will create the userlogins5 schema and all required tables
 * and sequences needed for a properly functioning WDK.
 *============================================================================*/

DROP SCHEMA IF EXISTS userlogins5 CASCADE;
CREATE SCHEMA IF NOT EXISTS userlogins5;
GRANT USAGE ON SCHEMA userlogins5 TO COMM_WDK_W;

/*==============================================================================
 * create tables
 *============================================================================*/

CREATE TABLE userlogins5.config
(
  config_name   VARCHAR(100) NOT NULL,
  config_value  VARCHAR(255),
  migration_id  NUMERIC(12),
  CONSTRAINT "config_pk" PRIMARY KEY (config_name)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.config TO COMM_WDK_W;

-- special metadata insert to declare WDK schema version
INSERT INTO userlogins5.config(config_name, config_value) VALUES('wdk.user.schema.version', '5');

--==============================================================================

CREATE TABLE userlogins5.users
(
  user_id       NUMERIC(12) NOT NULL,
  is_guest      BOOLEAN NOT NULL,
  first_access  TIMESTAMP,
  CONSTRAINT "users_pk" PRIMARY KEY (user_id)
);

CREATE INDEX users_idx01 ON userlogins5.users (is_guest);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.users TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.user_roles
(
  user_id       NUMERIC(12) NOT NULL,
  user_role     VARCHAR(50) NOT NULL,
  migration_id  NUMERIC(12),
  CONSTRAINT "user_roles_pk" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "user_roles_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.user_roles TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.preferences
(
  user_id           NUMERIC(12) NOT NULL,
  project_id        VARCHAR(50) NOT NULL,
  preference_name   VARCHAR(200) NOT NULL,
  preference_value  VARCHAR(4000),
  migration_id      NUMERIC(12),
  CONSTRAINT "preferences_pk" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "preferences_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.preferences TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.user_baskets (
  basket_id       NUMERIC(12) NOT NULL,
  user_id         NUMERIC(12) NOT NULL,
  basket_name     VARCHAR(100),
  project_id      VARCHAR(50) NOT NULL,
  record_class    VARCHAR(100) NOT NULL,
  is_default      BOOLEAN,
  category_id     NUMERIC(12),
  pk_column_1     VARCHAR(1999) NOT NULL,
  pk_column_2     VARCHAR(1999),
  pk_column_3     VARCHAR(1999),
  prev_basket_id  NUMERIC(12),
  migration_id    NUMERIC(12),
  CONSTRAINT "user_baskets_pk" PRIMARY KEY (basket_id),
  CONSTRAINT "user_baskets_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "user_baskets_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

CREATE INDEX user_baskets_idx01 ON userlogins5.user_baskets (project_id, record_class);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.user_baskets TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.favorites (
  favorite_id       NUMERIC(12) NOT NULL,
  user_id           NUMERIC(12) NOT NULL,
  project_id        VARCHAR(50) NOT NULL,
  record_class      VARCHAR(100) NOT NULL,
  pk_column_1       VARCHAR(1999) NOT NULL,
  pk_column_2       VARCHAR(1999),
  pk_column_3       VARCHAR(1999),
  record_note       VARCHAR(200),
  record_group      VARCHAR(50),
  prev_favorite_id  NUMERIC(12),
  migration_id      NUMERIC(12),
  is_deleted        BOOLEAN,
  CONSTRAINT "favorites_pk" PRIMARY KEY (favorite_id),
  CONSTRAINT "favorites_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "favorites_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

CREATE INDEX favorites_idx01 ON userlogins5.favorites (record_class, project_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.favorites TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.datasets (
  dataset_id        NUMERIC(12) NOT NULL,
  user_id           NUMERIC(12),
  dataset_name      VARCHAR(100) NOT NULL,
  dataset_size      NUMERIC(12) NOT NULL,
  content_checksum  VARCHAR(40) NOT NULL,
  created_time      TIMESTAMP NOT NULL,
  upload_file       VARCHAR(2000),
  parser            VARCHAR(50) NOT NULL,
  category_id       NUMERIC(12),
  content           TEXT,
  prev_dataset_id   NUMERIC(12),
  migration_id      NUMERIC(12),
  CONSTRAINT "datasets_pk" PRIMARY KEY (dataset_id),
  CONSTRAINT "datasets_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.datasets TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.dataset_values (
  dataset_value_id       NUMERIC(12) NOT NULL,
  dataset_id             NUMERIC(12) NOT NULL,
  data1                  VARCHAR(1999) NOT NULL,
  data2                  VARCHAR(1999),
  data3                  VARCHAR(1999),
  data4                  VARCHAR(1999),
  data5                  VARCHAR(1999),
  data6                  VARCHAR(1999),
  data7                  VARCHAR(1999),
  data8                  VARCHAR(1999),
  data9                  VARCHAR(1999),
  data10                 VARCHAR(1999),
  data11                 VARCHAR(1999),
  data12                 VARCHAR(1999),
  data13                 VARCHAR(1999),
  data14                 VARCHAR(1999),
  data15                 VARCHAR(1999),
  data16                 VARCHAR(1999),
  data17                 VARCHAR(1999),
  data18                 VARCHAR(1999),
  data19                 VARCHAR(1999),
  data20                 VARCHAR(1999),
  prev_dataset_value_id  NUMERIC(12),
  migration_id           NUMERIC(12),
  CONSTRAINT "dataset_values_pk" PRIMARY KEY (dataset_value_id),
  CONSTRAINT "dataset_values_fk01" FOREIGN KEY (dataset_id)
      REFERENCES userlogins5.datasets (dataset_id)
);

CREATE INDEX dataset_values_idx01 ON userlogins5.dataset_values (dataset_id, data1);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.dataset_values TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.steps (
  step_id            NUMERIC(12) NOT NULL,
  user_id            NUMERIC(12) NOT NULL,
  left_child_id      NUMERIC(12),
  right_child_id     NUMERIC(12),
  create_time        TIMESTAMP NOT NULL,
  last_run_time      TIMESTAMP NOT NULL,
  estimate_size      NUMERIC(12),
  answer_filter      VARCHAR(100),
  custom_name        VARCHAR(4000),
  is_deleted         BOOLEAN,
  is_valid           BOOLEAN,
  collapsed_name     VARCHAR(200),
  is_collapsible     BOOLEAN,
  assigned_weight    NUMERIC(12),
  project_id         VARCHAR(50) NOT NULL,
  project_version    VARCHAR(50) NOT NULL,
  question_name      VARCHAR(200) NOT NULL,
  strategy_id        NUMERIC(12),
  display_params     TEXT,
  result_message     TEXT,
  prev_step_id       NUMERIC(12),
  migration_id       NUMERIC(12),
  display_prefs      TEXT DEFAULT '{}',
  branch_is_expanded BOOLEAN DEFAULT FALSE NOT NULL,
  branch_name        VARCHAR(200),
  CONSTRAINT "steps_pk" PRIMARY KEY (step_id),
  CONSTRAINT "steps_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

CREATE INDEX steps_idx01 ON userlogins5.steps (left_child_id, right_child_id, user_id);
CREATE INDEX steps_idx02 ON userlogins5.steps (project_id, question_name, user_id);
CREATE INDEX steps_idx03 ON userlogins5.steps (is_deleted, user_id, project_id);
CREATE INDEX steps_idx04 ON userlogins5.steps (is_valid, project_id, user_id);
CREATE INDEX steps_idx05 ON userlogins5.steps (last_run_time, user_id, project_id);
CREATE INDEX steps_idx06 ON userlogins5.steps (strategy_id, user_id, project_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.steps TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.strategies (
  strategy_id       NUMERIC(12) NOT NULL,
  user_id           NUMERIC(12) NOT NULL,
  root_step_id      NUMERIC(12) NOT NULL,
  project_id        VARCHAR(50) NOT NULL,
  version           VARCHAR(100),
  is_saved          BOOLEAN NOT NULL,
  create_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_view_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_modify_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  description       VARCHAR(4000),
  signature         VARCHAR(40),
  name              VARCHAR(200) NOT NULL,
  saved_name        VARCHAR(200),
  is_deleted        BOOLEAN,
  is_public         BOOLEAN,
  prev_strategy_id  NUMERIC(12),
  migration_id      NUMERIC(12),
  CONSTRAINT "strategies_pk" PRIMARY KEY (strategy_id),
  CONSTRAINT "strategies_fk01" FOREIGN KEY (root_step_id)
      REFERENCES userlogins5.steps (step_id),
  CONSTRAINT "strategies_fk02" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

CREATE INDEX strategies_idx01 ON userlogins5.strategies (signature, project_id);
CREATE INDEX strategies_idx02 ON userlogins5.strategies (user_id, project_id, is_deleted, is_saved);
CREATE INDEX strategies_idx03 ON userlogins5.strategies (root_step_id, project_id, user_id, is_saved, is_deleted);
CREATE INDEX strategies_idx04 ON userlogins5.strategies (is_deleted, is_saved, name, project_id, user_id);
CREATE INDEX strategies_idx05 ON userlogins5.strategies (project_id, is_public, is_saved, is_deleted);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.strategies TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.step_analysis (
  analysis_id          NUMERIC(12) NOT NULL,
  step_id              NUMERIC(12) NOT NULL,
  display_name         VARCHAR(1024),
  user_notes           VARCHAR(4000),
  is_new               BOOLEAN,
  has_params           BOOLEAN,
  invalid_step_reason  VARCHAR(1024),
  context_hash         VARCHAR(96),
  context              TEXT,
  properties           TEXT,
  CONSTRAINT "step_analysis_pk" PRIMARY KEY (analysis_id),
  CONSTRAINT "step_analysis_fk01" FOREIGN KEY (step_id)
      REFERENCES userlogins5.steps (step_id)
);

CREATE INDEX step_analysis_idx01 ON userlogins5.step_analysis (step_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.step_analysis TO COMM_WDK_W;

--==============================================================================
-- create sequences -- not necessary if using foreign data wrappers
-- as sequences will never be used; foreign schema will require local versions
--==============================================================================

CREATE SEQUENCE userlogins5.user_baskets_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON userlogins5.user_baskets_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.favorites_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON userlogins5.favorites_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.datasets_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON userlogins5.datasets_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.dataset_values_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON userlogins5.dataset_values_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.strategies_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON userlogins5.strategies_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.steps_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON userlogins5.steps_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.step_analysis_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON userlogins5.step_analysis_pkseq TO COMM_WDK_W;
