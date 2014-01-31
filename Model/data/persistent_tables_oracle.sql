/*
DROP SEQUENCE wdkuser.categories_pkseq;
DROP SEQUENCE wdkuser.datasets_pkseq;
DROP SEQUENCE wdkuser.dataset_values_pkseq;
DROP SEQUENCE wdkuser.favorites_pkseq;
DROP SEQUENCE wdkuser.user_baskets_pkseq;
DROP SEQUENCE wdkuser.migration_pkseq;
DROP SEQUENCE wdkuser.steps_pkseq;
DROP SEQUENCE wdkuser.strategies_pkseq;
DROP SEQUENCE wdkuser.users_pkseq;

DROP TABLE wdkuser.categories;
DROP TABLE wdkuser.favorites;
DROP TABLE wdkuser.user_baskets;
DROP TABLE wdkuser.strategies;
DROP TABLE wdkuser.steps;
DROP TABLE wdkuser.dataset_values;
DROP TABLE wdkuser.datasets;
DROP TABLE wdkuser.preferences;
DROP TABLE wdkuser.user_roles;
DROP TABLE wdkuser.users;
*/


/* =========================================================================
   create sequences
   ========================================================================= */
CREATE SEQUENCE wdkuser.users_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.migration_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.strategies_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.steps_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.datasets_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.dataset_values_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.user_baskets_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.favorites_pkseq INCREMENT BY 1 START WITH 1;


CREATE SEQUENCE wdkuser.categories_pkseq INCREMENT BY 1 START WITH 1;



/* =========================================================================
   tables in user schema
   ========================================================================= */
   
CREATE TABLE wdkuser.users
(
  user_id NUMBER(12) NOT NULL,
  email VARCHAR(255) NOT NULL,
  passwd VARCHAR(50) NOT NULL,
  is_guest NUMBER(1) NOT NULL,
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
  PREV_USER_ID NUMBER(12),
  migration_id NUMBER(12),
  CONSTRAINT "users_pk" PRIMARY KEY (user_id),
  CONSTRAINT "users_uc01" UNIQUE (email),
  CONSTRAINT "users_uc02" UNIQUE (signature)
);


CREATE INDEX wdkuser.users_idx01 ON wdkuser.users (is_guest);
CREATE INDEX wdkuser.users_idx02 ON wdkuser.users (prev_user_id);


CREATE TABLE wdkuser.user_roles
(
  user_id NUMBER(12) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  migration_id NUMBER(12),
  CONSTRAINT "user_roles_pk" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "user_roles_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id) 
);


CREATE TABLE wdkuser.preferences
(
  user_id NUMBER(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  preference_name VARCHAR(200) NOT NULL,
  preference_value VARCHAR(4000),
  migration_id NUMBER(12),
  CONSTRAINT "preferences_pk" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "preferences_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id) 
);


CREATE TABLE wdkuser.steps
(
  step_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  left_child_id NUMBER(12),
  right_child_id NUMBER(12),
  create_time TIMESTAMP NOT NULL,
  last_run_time TIMESTAMP NOT NULL,
  estimate_size NUMBER(12),
  answer_filter VARCHAR(100),
  custom_name VARCHAR(4000),
  is_deleted NUMBER(1),
  is_valid NUMBER(1),
  collapsed_name varchar(200),
  is_collapsible NUMBER(1),
  assigned_weight NUMBER(12),
  project_id VARCHAR(50) NOT NULL,
  project_version VARCHAR(50) NOT NULL,
  question_name VARCHAR(200) NOT NULL,
  strategy_id NUMBER(12),
  display_params CLOB,
  result_message CLOB,
  prev_step_id NUMBER(12),
  migration_id NUMBER(12),
  CONSTRAINT "steps_pk" PRIMARY KEY (step_id),
  CONSTRAINT "steps_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);

CREATE INDEX wdkuser.steps_idx01 ON wdkuser.steps (left_child_id, right_child_id, user_id);
CREATE INDEX wdkuser.steps_idx02 ON wdkuser.steps (project_id, question_name, user_id);
CREATE INDEX wdkuser.steps_idx03 ON wdkuser.steps (is_deleted, user_id, project_id);
CREATE INDEX wdkuser.steps_idx04 ON wdkuser.steps (is_valid, project_id, user_id);
CREATE INDEX wdkuser.steps_idx05 ON wdkuser.steps (last_run_time, user_id, project_id);
CREATE INDEX wdkuser.steps_idx06 ON wdkuser.steps (strategy_id, user_id, project_id);


CREATE TABLE wdkuser.strategies
(
     strategy_id NUMBER(12) NOT NULL,
     user_id NUMBER(12) NOT NULL,
     root_step_id NUMBER(12) NOT NULL,
     project_id varchar(50) NOT NULL,
     version varchar(100),
     is_saved NUMBER(1) NOT NULL,
     create_time TIMESTAMP DEFAULT SYSDATE,
     last_view_time TIMESTAMP DEFAULT SYSDATE,
     last_modify_time TIMESTAMP DEFAULT SYSDATE,
     description varchar(4000),
     signature varchar(40),
     name varchar(200) NOT NULL,
     saved_name varchar(200),
     is_deleted NUMBER(1),
     is_public NUMBER(1),
     prev_strategy_id NUMBER(12),
     migration_id NUMBER(12),
     CONSTRAINT "strategies_pk" PRIMARY KEY (strategy_id),
     CONSTRAINT "strategies_fk01" FOREIGN KEY (root_step_id)
         REFERENCES wdkuser.steps (step_id),
     CONSTRAINT "strategies_fk02" FOREIGN KEY (user_id)
         REFERENCES wdkuser.users (user_id)
);

CREATE INDEX wdkuser.strategies_idx01 ON wdkuser.strategies (signature, project_id);
CREATE INDEX wdkuser.strategies_idx02 ON wdkuser.strategies (user_id, project_id, is_deleted, is_saved);
CREATE INDEX wdkuser.strategies_idx03 ON wdkuser.strategies (root_step_id, project_id, user_id, is_saved, is_deleted);
CREATE INDEX wdkuser.strategies_idx04 ON wdkuser.strategies (is_deleted, is_saved, name, project_id, user_id);
CREATE INDEX wdkuser.strategies_idx05 ON wdkuser.strategies (project_id, is_public, is_saved, is_deleted);


CREATE TABLE wdkuser.datasets (
  dataset_id NUMBER(12) NOT NULL,
  user_id NUMBER(12),
  dataset_name VARCHAR(100) NOT NULL,
  dataset_size NUMBER(12) NOT NULL,
  content_checksum VARCHAR(40) NOT NULL,
  created_time TIMESTAMP NOT NULL,
  upload_file VARCHAR(2000),
  parser VARCHAR(50) NOT NULL,
  category_id NUMBER(12),
  content CLOB,
  prev_dataset_id NUMBER(12),
  migration_id NUMBER(12),
  CONSTRAINT "datasets_pk" PRIMARY KEY (dataset_id),
  CONSTRAINT "datasets_uq01" UNIQUE (user_id, content_checksum),
  CONSTRAINT "datasets_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);


CREATE TABLE wdkuser.dataset_values
(
  dataset_value_id NUMBER(12) NOT NULL,
  dataset_id NUMBER(12) NOT NULL,
  data1 VARCHAR(1999) NOT NULL,
  data2 VARCHAR(1999),
  data3 VARCHAR(1999),
  data4 VARCHAR(1999),
  data5 VARCHAR(1999),
  prev_dataset_value_id NUMBER(12),
  migration_id NUMBER(12),
  CONSTRAINT "dataset_values_pk" PRIMARY KEY (dataset_value_id),
  CONSTRAINT "dataset_values_fk01" FOREIGN KEY (dataset_id)
      REFERENCES wdkuser.datasets (dataset_id)
);

CREATE INDEX wdkuser.dataset_values_idx01 ON wdkuser.dataset_values (dataset_id, data1);


CREATE TABLE wdkuser.user_baskets
(
  basket_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  basket_name VARCHAR(100),
  project_id VARCHAR(50) NOT NULL,
  record_class VARCHAR(100) NOT NULL,
  is_default NUMBER(1),
  category_id NUMBER(12),
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  prev_basket_id NUMBER(12),
  migration_id NUMBER(12),
  CONSTRAINT "user_baskets_pk" PRIMARY KEY (basket_id),
  CONSTRAINT "user_baskets_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "user_baskets_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);

CREATE INDEX wdkuser.user_baskets_idx01 ON wdkuser.user_baskets (project_id, record_class);


CREATE TABLE wdkuser.favorites
(
  favorite_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  record_class VARCHAR(100) NOT NULL,
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  record_note VARCHAR(200),
  record_group VARCHAR(50),
  prev_favorite_id NUMBER(12),
  migration_id NUMBER(12),
  CONSTRAINT "favorites_pk" PRIMARY KEY (favorite_id),
  CONSTRAINT "favorites_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "favorites_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);

CREATE INDEX wdkuser.favorites_idx01 ON wdkuser.favorites (record_class, project_id);


CREATE TABLE wdkuser.categories
(
  category_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  parent_id NUMBER(12),
  category_type VARCHAR(50) NOT NULL,
  category_name VARCHAR(100) NOT NULL,
  description VARCHAR(200),
  prev_category_id NUMBER(12),
  migration_id NUMBER(12),
  CONSTRAINT "categories_pk" PRIMARY KEY (category_id),
  CONSTRAINT "categories_uq01" UNIQUE (user_id, category_type, parent_id, category_name),
  CONSTRAINT "categories_fk01" FOREIGN KEY (user_id)
      REFERENCES wdkuser.users (user_id)
);