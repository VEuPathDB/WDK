/*==============================================================================
 * This SQL script will create the wdkuserdata schema and all required tables
 * and sequences needed for a properly functioning WDK.
 *============================================================================*/

DROP SCHEMA IF EXISTS wdkuserdata CASCADE;
CREATE SCHEMA IF NOT EXISTS wdkuserdata;
GRANT USAGE ON SCHEMA wdkuserdata TO COMM_WDK_W;

/*==============================================================================
 * create tables
 *============================================================================*/

CREATE TABLE wdkuserdata.users
(
  user_id       BIGINT NOT NULL,
  is_guest      BOOLEAN NOT NULL,
  first_access  TIMESTAMP,
  CONSTRAINT "users_pk" PRIMARY KEY (user_id)
);

CREATE INDEX users_idx01 ON wdkuserdata.users (is_guest);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.users TO COMM_WDK_W;

--==============================================================================

CREATE TABLE wdkuserdata.preferences
(
  user_id           BIGINT NOT NULL,
  project_id        VARCHAR(50) NOT NULL,
  preference_name   VARCHAR(4000) NOT NULL,
  preference_value  TEXT,
  CONSTRAINT "preferences_pk" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "preferences_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuserdata.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.preferences TO COMM_WDK_W;

--==============================================================================

CREATE TABLE wdkuserdata.user_baskets (
  basket_id       BIGINT NOT NULL,
  user_id         BIGINT NOT NULL,
  basket_name     VARCHAR(100),
  project_id      VARCHAR(50) NOT NULL,
  record_class    VARCHAR(100) NOT NULL,
  pk_column_1     VARCHAR(1999) NOT NULL,
  pk_column_2     VARCHAR(1999),
  pk_column_3     VARCHAR(1999),
  CONSTRAINT "user_baskets_pk" PRIMARY KEY (basket_id),
  CONSTRAINT "user_baskets_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "user_baskets_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuserdata.users (user_id)
);

CREATE INDEX user_baskets_idx01 ON wdkuserdata.user_baskets (project_id, record_class);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.user_baskets TO COMM_WDK_W;

--==============================================================================

CREATE TABLE wdkuserdata.favorites (
  favorite_id       BIGINT NOT NULL,
  user_id           BIGINT NOT NULL,
  project_id        VARCHAR(50) NOT NULL,
  record_class      VARCHAR(100) NOT NULL,
  pk_column_1       VARCHAR(1999) NOT NULL,
  pk_column_2       VARCHAR(1999),
  pk_column_3       VARCHAR(1999),
  record_note       VARCHAR(200),
  record_group      VARCHAR(50),
  is_deleted        BOOLEAN,
  CONSTRAINT "favorites_pk" PRIMARY KEY (favorite_id),
  CONSTRAINT "favorites_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "favorites_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuserdata.users (user_id)
);

CREATE INDEX favorites_idx01 ON wdkuserdata.favorites (record_class, project_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.favorites TO COMM_WDK_W;

--==============================================================================

CREATE TABLE wdkuserdata.datasets (
  dataset_id        BIGINT NOT NULL,
  user_id           BIGINT NOT NULL,
  dataset_name      VARCHAR(100) NOT NULL,
  dataset_size      INTEGER NOT NULL,
  content_checksum  VARCHAR(40) NOT NULL,
  created_time      TIMESTAMP NOT NULL,
  upload_file       VARCHAR(2000),
  parser            VARCHAR(50) NOT NULL,
  content           TEXT,
  CONSTRAINT "datasets_pk" PRIMARY KEY (dataset_id),
  CONSTRAINT "datasets_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuserdata.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.datasets TO COMM_WDK_W;

--==============================================================================

CREATE TABLE wdkuserdata.dataset_values (
  dataset_value_id       BIGINT NOT NULL,
  dataset_id             BIGINT NOT NULL,
  dataset_value_order    BIGINT NOT NULL,
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
  CONSTRAINT "dataset_values_pk" PRIMARY KEY (dataset_value_id),
  CONSTRAINT "dataset_values_fk01" FOREIGN KEY (dataset_id)
      REFERENCES wdkuserdata.datasets (dataset_id)
);

CREATE INDEX dataset_values_idx01 ON wdkuserdata.dataset_values (dataset_id, data1);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.dataset_values TO COMM_WDK_W;

--==============================================================================

CREATE TABLE wdkuserdata.steps (
  step_id            BIGINT NOT NULL,
  user_id            BIGINT NOT NULL,
  left_child_id      BIGINT,
  right_child_id     BIGINT,
  create_time        TIMESTAMP NOT NULL,
  last_run_time      TIMESTAMP NOT NULL,
  estimate_size      INTEGER,
  custom_name        VARCHAR(4000),
  is_deleted         BOOLEAN,
  is_valid           BOOLEAN,
  collapsed_name     VARCHAR(200),
  is_collapsible     BOOLEAN,
  assigned_weight    NUMERIC(12),
  project_id         VARCHAR(50) NOT NULL,
  project_version    VARCHAR(50) NOT NULL,
  question_name      VARCHAR(200) NOT NULL,
  strategy_id        BIGINT,
  display_params     TEXT,
  display_prefs      TEXT DEFAULT '{}',
  branch_is_expanded BOOLEAN DEFAULT FALSE NOT NULL,
  branch_name        VARCHAR(200),
  CONSTRAINT "steps_pk" PRIMARY KEY (step_id),
  CONSTRAINT "steps_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuserdata.users (user_id)
);

CREATE INDEX steps_idx01 ON wdkuserdata.steps (left_child_id, right_child_id, user_id);
CREATE INDEX steps_idx02 ON wdkuserdata.steps (project_id, question_name, user_id);
CREATE INDEX steps_idx03 ON wdkuserdata.steps (is_deleted, user_id, project_id);
CREATE INDEX steps_idx04 ON wdkuserdata.steps (is_valid, project_id, user_id);
CREATE INDEX steps_idx05 ON wdkuserdata.steps (last_run_time, user_id, project_id);
CREATE INDEX steps_idx06 ON wdkuserdata.steps (strategy_id, user_id, project_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.steps TO COMM_WDK_W;

--==============================================================================

CREATE TABLE wdkuserdata.strategies (
  strategy_id       BIGINT NOT NULL,
  user_id           BIGINT NOT NULL,
  root_step_id      BIGINT NOT NULL,
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
  CONSTRAINT "strategies_pk" PRIMARY KEY (strategy_id),
  CONSTRAINT "strategies_uq01" UNIQUE (signature),
  CONSTRAINT "strategies_fk01" FOREIGN KEY (root_step_id)
      REFERENCES wdkuserdata.steps (step_id),
  CONSTRAINT "strategies_fk02" FOREIGN KEY (user_id)
      REFERENCES wdkuserdata.users (user_id)
);

CREATE INDEX strategies_idx01 ON wdkuserdata.strategies (signature, project_id);
CREATE INDEX strategies_idx02 ON wdkuserdata.strategies (user_id, project_id, is_deleted, is_saved);
CREATE INDEX strategies_idx03 ON wdkuserdata.strategies (root_step_id, project_id, user_id, is_saved, is_deleted);
CREATE INDEX strategies_idx04 ON wdkuserdata.strategies (is_deleted, is_saved, name, project_id, user_id);
CREATE INDEX strategies_idx05 ON wdkuserdata.strategies (project_id, is_public, is_saved, is_deleted);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.strategies TO COMM_WDK_W;

--==============================================================================

CREATE TABLE wdkuserdata.step_analysis (
  analysis_id          BIGINT NOT NULL,
  step_id              BIGINT NOT NULL,
  display_name         VARCHAR(1024),
  user_notes           VARCHAR(4000),
  revision_status      INTEGER,
  has_params           BOOLEAN,
  invalid_step_reason  VARCHAR(1024),
  context_hash         VARCHAR(96),
  context              TEXT,
  properties           TEXT,
  CONSTRAINT "step_analysis_pk" PRIMARY KEY (analysis_id),
  CONSTRAINT "step_analysis_fk01" FOREIGN KEY (step_id)
      REFERENCES wdkuserdata.steps (step_id)
);

CREATE INDEX step_analysis_idx01 ON wdkuserdata.step_analysis (step_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON wdkuserdata.step_analysis TO COMM_WDK_W;

--==============================================================================
-- create sequences -- not necessary if using foreign data wrappers
-- as sequences will never be used; foreign schema will require local versions
--==============================================================================

CREATE SEQUENCE wdkuserdata.user_baskets_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON wdkuserdata.user_baskets_pkseq TO COMM_WDK_W;

CREATE SEQUENCE wdkuserdata.favorites_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON wdkuserdata.favorites_pkseq TO COMM_WDK_W;

CREATE SEQUENCE wdkuserdata.datasets_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON wdkuserdata.datasets_pkseq TO COMM_WDK_W;

CREATE SEQUENCE wdkuserdata.dataset_values_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON wdkuserdata.dataset_values_pkseq TO COMM_WDK_W;

CREATE SEQUENCE wdkuserdata.strategies_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON wdkuserdata.strategies_pkseq TO COMM_WDK_W;

CREATE SEQUENCE wdkuserdata.steps_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON wdkuserdata.steps_pkseq TO COMM_WDK_W;

CREATE SEQUENCE wdkuserdata.step_analysis_pkseq INCREMENT BY 10; -- START WITH 100000000;
GRANT SELECT ON wdkuserdata.step_analysis_pkseq TO COMM_WDK_W;
