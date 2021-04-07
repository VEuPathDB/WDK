/*==============================================================================
 * This SQL script will create the userlogins5 schema and all required tables
 * and sequences needed for a properly functioning WDK.
 *============================================================================*/

CREATE USER userlogins5
--IDENTIFIED BY DCB802868A9EF9F9 -- deprecated
  QUOTA UNLIMITED ON users
  DEFAULT TABLESPACE users
  TEMPORARY TABLESPACE temp;

GRANT CREATE VIEW TO userlogins5;
GRANT CREATE MATERIALIZED VIEW TO userlogins5;
GRANT CREATE TABLE TO userlogins5;
GRANT CREATE SYNONYM TO userlogins5;
GRANT CREATE SESSION TO userlogins5;
GRANT CREATE ANY INDEX TO userlogins5;
GRANT CREATE TRIGGER TO userlogins5;
GRANT CREATE ANY TRIGGER TO userlogins5;

/*==============================================================================
 * create tables
 *============================================================================*/

CREATE TABLE userlogins5.config
(
  config_name   VARCHAR2(100) NOT NULL,
  config_value  VARCHAR2(255),
  migration_id  NUMBER(12),
  CONSTRAINT "config_pk" PRIMARY KEY (config_name)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.config TO COMM_WDK_W;

-- special metadata insert to declare WDK schema version
INSERT INTO userlogins5.config(config_name, config_value) VALUES('wdk.user.schema.version', '5');

--==============================================================================

CREATE TABLE userlogins5.users
(
  user_id       NUMBER(12) NOT NULL,
  is_guest      NUMBER(1) NOT NULL,
  first_access  TIMESTAMP,
  CONSTRAINT "users_pk" PRIMARY KEY (user_id)
);

CREATE INDEX userlogins5.users_idx01 ON userlogins5.users (is_guest);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.users TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.user_roles
(
  user_id       NUMBER(12) NOT NULL,
  user_role     VARCHAR2(50) NOT NULL,
  migration_id  NUMBER(12),
  CONSTRAINT "user_roles_pk" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "user_roles_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.user_roles TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.preferences
(
  user_id           NUMBER(12) NOT NULL,
  project_id        VARCHAR2(50) NOT NULL,
  preference_name   VARCHAR2(4000) NOT NULL,
  preference_value  CLOB,
  CONSTRAINT "preferences_pk" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "preferences_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.preferences TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.user_baskets (
  basket_id       NUMBER(12) NOT NULL,
  user_id         NUMBER(12) NOT NULL,
  basket_name     VARCHAR2(100),
  project_id      VARCHAR2(50) NOT NULL,
  record_class    VARCHAR2(100) NOT NULL,
  is_default      NUMBER(1),
  category_id     NUMBER(12),
  pk_column_1     VARCHAR2(1999) NOT NULL,
  pk_column_2     VARCHAR2(1999),
  pk_column_3     VARCHAR2(1999),
  prev_basket_id  NUMBER(12),
  migration_id    NUMBER(12),
  CONSTRAINT "user_baskets_pk" PRIMARY KEY (basket_id),
  CONSTRAINT "user_baskets_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "user_baskets_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

CREATE INDEX userlogins5.user_baskets_idx01 ON userlogins5.user_baskets (project_id, record_class);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.user_baskets TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.favorites (
  favorite_id       NUMBER(12) NOT NULL,
  user_id           NUMBER(12) NOT NULL,
  project_id        VARCHAR2(50) NOT NULL,
  record_class      VARCHAR2(100) NOT NULL,
  pk_column_1       VARCHAR2(1999) NOT NULL,
  pk_column_2       VARCHAR2(1999),
  pk_column_3       VARCHAR2(1999),
  record_note       VARCHAR2(200),
  record_group      VARCHAR2(50),
  prev_favorite_id  NUMBER(12),
  migration_id      NUMBER(12),
  is_deleted        NUMBER(1),
  CONSTRAINT "favorites_pk" PRIMARY KEY (favorite_id),
  CONSTRAINT "favorites_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "favorites_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

CREATE INDEX userlogins5.favorites_idx01 ON userlogins5.favorites (record_class, project_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.favorites TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.categories (
  category_id       NUMBER(12) NOT NULL,
  user_id           NUMBER(12) NOT NULL,
  parent_id         NUMBER(12),
  category_type     VARCHAR2(50) NOT NULL,
  category_name     VARCHAR2(100) NOT NULL,
  description       VARCHAR2(200),
  prev_category_id  NUMBER(12),
  migration_id      NUMBER(12),
  CONSTRAINT "categories_pk" PRIMARY KEY (category_id),
  CONSTRAINT "categories_uq01" UNIQUE (user_id, category_type, parent_id, category_name),
  CONSTRAINT "categories_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.categories TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.datasets (
  dataset_id        NUMBER(12) NOT NULL,
  user_id           NUMBER(12),
  dataset_name      VARCHAR2(100) NOT NULL,
  dataset_size      NUMBER(12) NOT NULL,
  content_checksum  VARCHAR2(40) NOT NULL,
  created_time      TIMESTAMP NOT NULL,
  upload_file       VARCHAR2(2000),
  parser            VARCHAR2(50) NOT NULL,
  category_id       NUMBER(12),
  content           CLOB,
  prev_dataset_id   NUMBER(12),
  migration_id      NUMBER(12),
  CONSTRAINT "datasets_pk" PRIMARY KEY (dataset_id),
  CONSTRAINT "datasets_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.datasets TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.dataset_values (
  dataset_value_id       NUMBER(12) NOT NULL,
  dataset_id             NUMBER(12) NOT NULL,
  data1                  VARCHAR2(1999) NOT NULL,
  data2                  VARCHAR2(1999),
  data3                  VARCHAR2(1999),
  data4                  VARCHAR2(1999),
  data5                  VARCHAR2(1999),
  data6                  VARCHAR2(1999),
  data7                  VARCHAR2(1999),
  data8                  VARCHAR2(1999),
  data9                  VARCHAR2(1999),
  data10                 VARCHAR2(1999),
  data11                 VARCHAR2(1999),
  data12                 VARCHAR2(1999),
  data13                 VARCHAR2(1999),
  data14                 VARCHAR2(1999),
  data15                 VARCHAR2(1999),
  data16                 VARCHAR2(1999),
  data17                 VARCHAR2(1999),
  data18                 VARCHAR2(1999),
  data19                 VARCHAR2(1999),
  data20                 VARCHAR2(1999),
  prev_dataset_value_id  NUMBER(12),
  migration_id           NUMBER(12),
  CONSTRAINT "dataset_values_pk" PRIMARY KEY (dataset_value_id),
  CONSTRAINT "dataset_values_fk01" FOREIGN KEY (dataset_id)
      REFERENCES userlogins5.datasets (dataset_id)
);

CREATE INDEX userlogins5.dataset_values_idx01 ON userlogins5.dataset_values (dataset_id, data1);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.dataset_values TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.steps (
  step_id            NUMBER(12) NOT NULL,
  user_id            NUMBER(12) NOT NULL,
  left_child_id      NUMBER(12),
  right_child_id     NUMBER(12),
  create_time        TIMESTAMP NOT NULL,
  last_run_time      TIMESTAMP NOT NULL,
  estimate_size      NUMBER(12),
  answer_filter      VARCHAR2(100),
  custom_name        VARCHAR2(4000),
  is_deleted         NUMBER(1),
  is_valid           NUMBER(1),
  collapsed_name     VARCHAR2(200),
  is_collapsible     NUMBER(1),
  assigned_weight    NUMBER(12),
  project_id         VARCHAR2(50) NOT NULL,
  project_version    VARCHAR2(50) NOT NULL,
  question_name      VARCHAR2(200) NOT NULL,
  strategy_id        NUMBER(12),
  display_params     CLOB,
  result_message     CLOB,
  prev_step_id       NUMBER(12),
  migration_id       NUMBER(12),
  display_prefs      CLOB DEFAULT '{}',
  branch_is_expanded NUMBER(1) DEFAULT 0 NOT NULL,
  branch_name        VARCHAR2(200),
  CONSTRAINT "steps_pk" PRIMARY KEY (step_id),
  CONSTRAINT "steps_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

CREATE INDEX userlogins5.steps_idx01 ON userlogins5.steps (left_child_id, right_child_id, user_id);
CREATE INDEX userlogins5.steps_idx02 ON userlogins5.steps (project_id, question_name, user_id);
CREATE INDEX userlogins5.steps_idx03 ON userlogins5.steps (is_deleted, user_id, project_id);
CREATE INDEX userlogins5.steps_idx04 ON userlogins5.steps (is_valid, project_id, user_id);
CREATE INDEX userlogins5.steps_idx05 ON userlogins5.steps (last_run_time, user_id, project_id);
CREATE INDEX userlogins5.steps_idx06 ON userlogins5.steps (strategy_id, user_id, project_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.steps TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.strategies (
  strategy_id       NUMBER(12) NOT NULL,
  user_id           NUMBER(12) NOT NULL,
  root_step_id      NUMBER(12) NOT NULL,
  project_id        VARCHAR2(50) NOT NULL,
  version           VARCHAR2(100),
  is_saved          NUMBER(1) NOT NULL,
  create_time       TIMESTAMP DEFAULT SYSDATE,
  last_view_time    TIMESTAMP DEFAULT SYSDATE,
  last_modify_time  TIMESTAMP DEFAULT SYSDATE,
  description       VARCHAR2(4000),
  signature         VARCHAR2(40),
  name              VARCHAR2(200) NOT NULL,
  saved_name        VARCHAR2(200),
  is_deleted        NUMBER(1),
  is_public         NUMBER(1),
  prev_strategy_id  NUMBER(12),
  migration_id      NUMBER(12),
  CONSTRAINT "strategies_pk" PRIMARY KEY (strategy_id),
  CONSTRAINT "strategies_fk01" FOREIGN KEY (root_step_id)
      REFERENCES userlogins5.steps (step_id),
  CONSTRAINT "strategies_fk02" FOREIGN KEY (user_id)
      REFERENCES userlogins5.users (user_id)
);

CREATE INDEX userlogins5.strategies_idx01 ON userlogins5.strategies (signature, project_id);
CREATE INDEX userlogins5.strategies_idx02 ON userlogins5.strategies (user_id, project_id, is_deleted, is_saved);
CREATE INDEX userlogins5.strategies_idx03 ON userlogins5.strategies (root_step_id, project_id, user_id, is_saved, is_deleted);
CREATE INDEX userlogins5.strategies_idx04 ON userlogins5.strategies (is_deleted, is_saved, name, project_id, user_id);
CREATE INDEX userlogins5.strategies_idx05 ON userlogins5.strategies (project_id, is_public, is_saved, is_deleted);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.strategies TO COMM_WDK_W;

--==============================================================================

CREATE TABLE userlogins5.step_analysis (
  analysis_id          NUMBER(12) NOT NULL,
  step_id              NUMBER(12) NOT NULL,
  display_name         VARCHAR2(1024),
  user_notes           VARCHAR2(4000),
  is_new               NUMBER(1),
  has_params           NUMBER(1),
  invalid_step_reason  VARCHAR2(1024),
  context_hash         VARCHAR2(96),
  context              CLOB,
  properties           CLOB,
  CONSTRAINT "step_analysis_pk" PRIMARY KEY (analysis_id),
  CONSTRAINT "step_analysis_fk01" FOREIGN KEY (step_id)
      REFERENCES userlogins5.steps (step_id)
);

CREATE INDEX userlogins5.step_analysis_idx01 ON userlogins5.step_analysis (step_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON userlogins5.step_analysis TO COMM_WDK_W;

--==============================================================================
-- create sequences
-- ApiCommN for 100000000, ApiCommS for 100000003
--==============================================================================

CREATE SEQUENCE userlogins5.user_baskets_pkseq INCREMENT BY 10 START WITH 100000000;
GRANT SELECT ON userlogins5.user_baskets_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.favorites_pkseq INCREMENT BY 10 START WITH 100000000;
GRANT SELECT ON userlogins5.favorites_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.categories_pkseq INCREMENT BY 10 START WITH 100000000;
GRANT SELECT ON userlogins5.categories_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.datasets_pkseq INCREMENT BY 10 START WITH 100000000;
GRANT SELECT ON userlogins5.datasets_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.dataset_values_pkseq INCREMENT BY 10 START WITH 100000000;
GRANT SELECT ON userlogins5.dataset_values_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.strategies_pkseq INCREMENT BY 10 START WITH 100000000;
GRANT SELECT ON userlogins5.strategies_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.steps_pkseq INCREMENT BY 10 START WITH 100000000;
GRANT SELECT ON userlogins5.steps_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.step_analysis_pkseq INCREMENT BY 10 START WITH 100000000;
GRANT SELECT ON userlogins5.step_analysis_pkseq TO COMM_WDK_W;
