/*
DROP SEQUENCE IF EXISTS wdkengine.migration_pkseq;
DROP SEQUENCE IF EXISTS wdkengine.dataset_indices_pkseq;

DROP SEQUENCE IF EXISTS wdkuser.migration_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.favorites_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.user_baskets_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.user_datasets2_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.step_params_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.steps_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.strategies_pkseq;
DROP SEQUENCE IF EXISTS wdkuser.users_pkseq;

DROP TABLE IF EXISTS wdkuser.favorites;
DROP TABLE IF EXISTS wdkuser.user_baskets;
DROP TABLE IF EXISTS wdkuser.strategies;
DROP TABLE IF EXISTS wdkuser.step_params;
DROP TABLE IF EXISTS wdkuser.steps;
DROP TABLE IF EXISTS wdkuser.user_datasets2;
DROP TABLE IF EXISTS wdkuser.preferences;
DROP TABLE IF EXISTS wdkuser.user_roles;
DROP TABLE IF EXISTS wdkuser.users;

DROP TABLE IF EXISTS wdkengine.clob_values;
DROP TABLE IF EXISTS wdkengine.dataset_values;
DROP TABLE IF EXISTS wdkengine.dataset_indices;
*/


/* =========================================================================
   create schemas ("schemata"?)
   ========================================================================= */

-- CREATE SCHEMA IF NOT EXISTS wdkuser;
CREATE SCHEMA wdkuser;

-- CREATE SCHEMA IF NOT EXISTS wdkengine;
CREATE SCHEMA wdkengine;


/* =========================================================================
   create sequences
   ========================================================================= */

CREATE SEQUENCE wdkengine.migration_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkengine.dataset_indices_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.users_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.migration_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.strategies_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.steps_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.step_params_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.user_datasets2_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.user_baskets_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.favorites_pkseq INCREMENT BY 1 START WITH 1;



/* =========================================================================
   tables in wdk engine schema
   ========================================================================= */

CREATE TABLE wdkengine.dataset_indices
(
  dataset_id NUMERIC(12) NOT NULL,
  dataset_checksum VARCHAR(40) NOT NULL,
  record_class VARCHAR(200) NOT NULL,
  summary VARCHAR(200) NOT NULL,
  dataset_size NUMERIC(12) NOT NULL,
  PREV_DATASET_ID NUMERIC(12),
  migration NUMERIC(12),
  CONSTRAINT "dataset_indices_pk" PRIMARY KEY (dataset_id),
  CONSTRAINT "dataset_indices_uq01" UNIQUE (dataset_checksum)
);

CREATE INDEX dataset_indices_idx01 ON wdkengine.dataset_indices (prev_dataset_id);


/* cannot create PK on composite columns, since there might be null values */
CREATE TABLE wdkengine.dataset_values
(
  dataset_id NUMERIC(12) NOT NULL,
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  migration NUMERIC(12),
  CONSTRAINT "dataset_values_uq01" UNIQUE (dataset_id, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "dataset_values_fk01" FOREIGN KEY (dataset_id)
      REFERENCES wdkengine.dataset_indices (dataset_id)
);


CREATE TABLE wdkengine.clob_values
(
  clob_checksum VARCHAR(40) NOT NULL,
  clob_value TEXT NOT NULL,
  migration NUMERIC(12),
  CONSTRAINT "clob_values_pk" PRIMARY KEY (clob_checksum)
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
  CONSTRAINT "users_pk" PRIMARY KEY (user_id),
  CONSTRAINT "users_uq01" UNIQUE (email)
);

CREATE INDEX users_idx01 ON wdkuser.users (prev_user_id);


CREATE TABLE wdkuser.user_roles
(
  user_id NUMERIC(12) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  migration NUMERIC(12),
  CONSTRAINT "user_roles_pk" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "user_roles_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id) 
);


CREATE TABLE wdkuser.preferences
(
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  preference_name VARCHAR(200) NOT NULL,
  preference_value VARCHAR(4000),
  migration NUMERIC(12),
  CONSTRAINT "preferences_pk" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "preferences_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id) 
);


CREATE TABLE wdkuser.steps
(
  step_id NUMERIC(12) NOT NULL,
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
  CONSTRAINT "steps_pk" PRIMARY KEY (step_id),
  CONSTRAINT "steps_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);

CREATE INDEX steps_idx01 ON wdkuser.steps (user_id, left_child_id, right_child_id);
CREATE INDEX steps_idx02 ON wdkuser.steps (project_id, question_name, user_id);
CREATE INDEX steps_idx03 ON wdkuser.steps (is_deleted, user_id, project_id);
CREATE INDEX steps_idx04 ON wdkuser.steps (is_valid, user_id, project_id);
CREATE INDEX steps_idx05 ON wdkuser.steps (last_run_time, user_id, project_id);


/* 
   cannot create foreign key constraint on step_id, since step_params table is
   used in a different context than steps table.
*/
CREATE TABLE wdkuser.step_params
(
  step_param_id NUMERIC(12) NOT NULL,
  step_id NUMERIC(12) NOT NULL,
  param_name VARCHAR(200) NOT NULL,
  param_value VARCHAR(4000),
  migration NUMERIC(12),
  CONSTRAINT "step_params_pk" PRIMARY KEY (step_param_id)
);

CREATE INDEX step_params_idx02 ON wdkuser.step_params (step_id, param_name);


CREATE TABLE wdkuser.strategies
(
     strategy_id NUMERIC(12) NOT NULL,
     user_id NUMERIC(12) NOT NULL,
     root_step_id NUMERIC(12) NOT NULL,
     project_id varchar(50) NOT NULL,
     is_saved BOOLEAN NOT NULL,
     create_time TIMESTAMP DEFAULT NOW(),
     last_view_time TIMESTAMP DEFAULT NOW(),
     last_modify_time TIMESTAMP DEFAULT NOW(),
     description varchar(4000),
     signature varchar(40),
     version varchar(100),
     name varchar(200) NOT NULL,
     saved_name varchar(200),
     is_deleted BOOLEAN,
     prev_strategy_id NUMERIC(12),
     migration NUMERIC(12),
     CONSTRAINT "strategies_pk" PRIMARY KEY (strategy_id),
     CONSTRAINT "strategies_fk01" FOREIGN KEY (root_step_id)
         REFERENCES wdkuser.steps (step_id),
     CONSTRAINT "strategies_fk02" FOREIGN KEY (user_id)
         REFERENCES wdkuser.users (user_id)
);


CREATE INDEX strategies_idx01 ON wdkuser.strategies (signature, project_id);
CREATE INDEX strategies_idx02 ON wdkuser.strategies (user_id, project_id, is_deleted, is_saved);
CREATE INDEX strategies_idx03 ON wdkuser.strategies (project_id, root_step_id, user_id, is_saved, is_deleted);
CREATE INDEX strategies_idx04 ON wdkuser.strategies (is_deleted, is_saved, name, project_id, user_id);


CREATE TABLE wdkuser.user_datasets2
(
  user_dataset_id NUMERIC(12) NOT NULL,
  dataset_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  upload_file VARCHAR(2000),
  prev_user_dataset_id NUMERIC(12),
  migration NUMERIC(12),
  CONSTRAINT "user_datasets2_pk" PRIMARY KEY (user_dataset_id),
  CONSTRAINT "user_datasets2_uq01" UNIQUE (dataset_id, user_id),
  CONSTRAINT "user_datasets2_fk01" FOREIGN KEY (dataset_id)
      REFERENCES wdkengine.dataset_indices (dataset_id),
  CONSTRAINT "user_datasets2_fk02" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);

/* cannot create composite primary key, since the columns might contain null values */
CREATE TABLE wdkuser.user_baskets
(
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  record_class VARCHAR(100) NOT NULL,
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  CONSTRAINT "user_baskets_uq01" UNIQUE (project_id, record_class, pk_column_1, pk_column_2, pk_column_3, user_id),
  CONSTRAINT "user_baskets_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);

CREATE INDEX user_baskets_idx01 ON wdkuser.user_baskets (user_id);


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
  CONSTRAINT "favorites_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "favorites_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);

CREATE INDEX favorites_idx01 ON wdkuser.favorites (record_group, user_id, project_id);
