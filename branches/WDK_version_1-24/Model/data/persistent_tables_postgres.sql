DROP SEQUENCE IF EXISTS wdkengine.migration_pkseq;
DROP SEQUENCE IF EXISTS wdkengine.dataset_indices_pkseq;
DROP SEQUENCE IF EXISTS wdkengine.answers_pkseq;

DROP SEQUENCE IF EXISTS wdkuser.user_datasets2_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.steps_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.strategies_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.users_pkseq;

DROP TABLE IF EXISTS wdkuser.favorites;
DROP TABLE IF EXISTS wdkuser.user_baskets;
DROP TABLE IF EXISTS wdkuser.strategies;
DROP TABLE IF EXISTS wdkuser.step_params;
DROP TABLE IF EXISTS wdkuser.steps;
DROP TABLE IF EXISTS wdkuser.user_datasets;
DROP TABLE IF EXISTS wdkuser.preferences;
DROP TABLE IF EXISTS wdkuser.user_roles;
DROP TABLE IF EXISTS wdkuser.users;

DROP TABLE IF EXISTS wdkengine.answers;
DROP TABLE IF EXISTS wdkengine.clob_values;
DROP TABLE IF EXISTS wdkengine.dataset_values;
DROP TABLE IF EXISTS wdkengine.dataset_indices;



/* =========================================================================
   create sequences
   ========================================================================= */

CREATE SEQUENCE wdkengine.migration_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkengine.dataset_indices_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkengine.answers_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.users_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.migration_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.strategies_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.steps_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.user_datasets2_pkseq INCREMENT BY 1 START WITH 1;


/* =========================================================================
   tables in wdk engine schema
   ========================================================================= */


CREATE TABLE wdkengine.answers
(
  answer_id NUMERIC(12) NOT NULL,
  answer_checksum VARCHAR(40) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  project_version VARCHAR(50) NOT NULL,
  question_name VARCHAR(200) NOT NULL,
  query_checksum  VARCHAR(40) NOT NULL,
  old_query_checksum  VARCHAR(40),
  params TEXT,
  result_message TEXT,
  prev_answer_id NUMERIC(12),
  migration NUMERIC(12),
  CONSTRAINT "answers_pk" PRIMARY KEY (answer_id),
  CONSTRAINT "answers_uq1" UNIQUE (project_id, question_name, answer_checksum)
);

CREATE INDEX answers_idx01 ON wdkengine.answers (prev_answer_id);
CREATE INDEX answers_idx02 ON wdkengine.answers (old_query_checksum);


CREATE TABLE wdkengine.dataset_indices
(
  dataset_id NUMERIC(12) NOT NULL,
  dataset_checksum VARCHAR(40) NOT NULL,
  record_class VARCHAR(200) NOT NULL,
  summary VARCHAR(200) NOT NULL,
  dataset_size NUMERIC(12) NOT NULL,
  PREV_DATASET_ID NUMERIC(12),
  migration NUMERIC(12),
  CONSTRAINT "DATASET_INDICES_PK" PRIMARY KEY (dataset_id),
  CONSTRAINT "DATASET_CHECKSUM_UNIQUE" UNIQUE (dataset_checksum)
);

CREATE INDEX dataset_indices_idx01 ON wdkengine.dataset_indices (prev_dataset_id);


CREATE TABLE wdkengine.dataset_values
(
  dataset_id NUMERIC(12) NOT NULL,
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  migration NUMERIC(12),
  CONSTRAINT "DATASET_VALUES_PK" PRIMARY KEY (dataset_id, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "DATASET_VALUES_DATASET_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES wdkengine.dataset_indices (dataset_id)
);


CREATE TABLE wdkengine.clob_values
(
  clob_checksum VARCHAR(40) NOT NULL,
  clob_value TEXT NOT NULL,
  migration NUMERIC(12),
  CONSTRAINT "CLOB_VALUES_PK" PRIMARY KEY (clob_checksum)
);


/* =========================================================================
   tables in user schema
   ========================================================================= */
   
CREATE TABLE wdkuser.users
(
  user_id NUMERIC(12) NOT NULL,
  email VARCHAR(255) NOT NULL,
  passwd VARCHAR(50) NOT NULL,
  is_guest BOOLEAN NOT NULL,
  signature VARCHAR(40),
  register_time TIMESTAMP,
  last_active TIMESTAMP,
  last_name VARCHAR(50),
  first_name VARCHAR(50),
  middle_name VARCHAR(50),
  title VARCHAR(255),
  organization VARCHAR(255),
  department VARCHAR(255),
  address VARCHAR(500),
  city VARCHAR(255),
  state VARCHAR(255),
  zip_code VARCHAR(20),
  phone_number VARCHAR(50),
  country VARCHAR(255),
  PREV_USER_ID NUMERIC(12),
  migration NUMERIC(12),
  CONSTRAINT "USER_PK" PRIMARY KEY (user_id),
  CONSTRAINT "USER_EMAIL_UNIQUE" UNIQUE (email)
);

CREATE INDEX users_idx01 ON wdkuser.users (prev_user_id);


CREATE TABLE wdkuser.user_roles
(
  user_id NUMERIC(12) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  migration NUMERIC(12),
  CONSTRAINT "USER_ROLE_PK" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "USER_ROLE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id) 
);


CREATE TABLE wdkuser.preferences
(
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  preference_name VARCHAR(200) NOT NULL,
  preference_value VARCHAR(4000),
  migration NUMERIC(12),
  CONSTRAINT "PREFERENCES_PK" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "PREFERENCE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id) 
);


CREATE TABLE wdkuser.steps
(
  step_id NUMERIC(12) NOT NULL,
  display_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  answer_id NUMERIC(12) NOT NULL,
  left_child_id NUMERIC(12),
  right_child_id NUMERIC(12),
  create_time TIMESTAMP NOT NULL,
  last_run_time TIMESTAMP NOT NULL,
  estimate_size NUMERIC(12),
  answer_filter VARCHAR(100),
  custom_name VARCHAR(4000),
  is_deleted BOOLEAN,
  is_valid BOOLEAN,
  collapsed_name VARCHAR(200),
  is_collapsible BOOLEAN,
  display_params TEXT,
  prev_step_id NUMERIC(12),
  invalid_message VARCHAR(2000),
  assigned_weight NUMERIC(12),
  migration NUMERIC(12),
  CONSTRAINT "STEPS_PK" PRIMARY KEY (step_id),
  CONSTRAINT "STEPS_UNIQUE" UNIQUE (user_id, display_id),
  CONSTRAINT "STEPS_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id),
  CONSTRAINT "STEPS_ANSWER_ID_FK" FOREIGN KEY (answer_id)
      REFERENCES wdkengine.answers (answer_id)
);

CREATE INDEX wdkuser.steps_idx01 ON wdkuser.steps (answer_id, user_id, left_child_id);
CREATE INDEX wdkuser.steps_idx02 ON wdkuser.steps (user_id, answer_id, right_child_id);
CREATE INDEX wdkuser.steps_idx03 ON wdkuser.steps (user_id, display_id, last_run_time);
CREATE INDEX wdkuser.steps_idx04 ON wdkuser.steps (user_id, answer_id, is_deleted);
CREATE INDEX wdkuser.steps_idx05 ON wdkuser.steps (user_id, answer_id, display_id);


CREATE TABLE wdkuser.step_params
(
  step_id NUMERIC(12) NOT NULL,
  param_name VARCHAR(200) NOT NULL,
  param_value VARCHAR(4000),
  migration NUMERIC(12),
  CONSTRAINT "STEP_PARAMS_STEP_ID_FK" FOREIGN KEY (step_id)
      REFERENCES wdkuser.steps (step_id)
);

CREATE INDEX step_params_idx02 ON wdkuser.step_params (step_id, param_name);


CREATE TABLE wdkuser.strategies
(
     strategy_id NUMERIC(12) NOT NULL,
     display_id NUMERIC(12) NOT NULL,
     user_id NUMERIC(12) NOT NULL,
     root_step_id NUMERIC(12) NOT NULL,
     project_id varchar(50) NOT NULL,
     is_saved BOOLEAN NOT NULL,
     create_time TIMESTAMP DEFAULT NOW(),
     last_view_time TIMESTAMP DEFAULT NOW(),
     last_modify_time TIMESTAMP DEFAULT NOW(),
     description varchar(4000),
     signature varchar(40),
     name varchar(200) NOT NULL,
     saved_name varchar(200),
     is_deleted BOOLEAN,
     prev_strategy_id NUMERIC(12),
     migration NUMERIC(12),
     CONSTRAINT "STRATEGIES_PK" PRIMARY KEY (strategy_id),
     CONSTRAINT "STRATEGIES_UNIQUE" UNIQUE (user_id, display_id, project_id),
     CONSTRAINT "STRATEGIES_STEP_FK" FOREIGN KEY (user_id, root_step_id)
         REFERENCES wdkuser.steps (user_id, display_id),
     CONSTRAINT "STRATEGIES_USER_ID_FK" FOREIGN KEY (user_id)
         REFERENCES wdkuser.users (user_id)
);


CREATE INDEX wdkuser.strategies_idx01 ON wdkuser.strategies (signature, project_id);
CREATE INDEX wdkuser.strategies_idx02 ON wdkuser.strategies (project_id, user_id, display_id, is_deleted);
CREATE INDEX wdkuser.strategies_idx03 ON wdkuser.strategies (user_id, project_id, root_step_id, is_deleted, is_saved);
CREATE INDEX wdkuser.strategies_idx04 ON wdkuser.strategies (project_id, user_id, is_deleted, is_saved, name);


CREATE TABLE wdkuser.user_datasets2
(
  user_dataset_id NUMERIC(12) NOT NULL,
  dataset_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  upload_file VARCHAR(2000),
  prev_user_dataset_id NUMERIC(12),
  migration NUMERIC(12),
  CONSTRAINT "USER_DATASET_PK" PRIMARY KEY (user_dataset_id),
  CONSTRAINT "USER_DATASET_UQ1" UNIQUE (dataset_id, user_id),
  CONSTRAINT "USER_DATASETS_DS_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES wdkengine.dataset_indices (dataset_id),
  CONSTRAINT "USER_DATASETS_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);


CREATE TABLE wdkuser.user_baskets
(
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  record_class VARCHAR(100) NOT NULL,
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  CONSTRAINT "USER_BASKETS_PK" PRIMARY KEY (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "USER_BASKETS_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);


CREATE TABLE wdkuser.favorites
(
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  record_class VARCHAR(100) NOT NULL,
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  record_note VARCHAR(200),
  record_group VARCHAR(50),
  CONSTRAINT "FAVORITES_PK" PRIMARY KEY (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "FAVORITES_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);

CREATE INDEX wdkuser.favorites_idx02 ON wdkuser.favorites (record_group, user_id, project_id);
