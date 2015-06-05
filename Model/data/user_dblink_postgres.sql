-- create database links to user database in app database
-- "<dbuser>" and "<dbpassword>" must be replaced by the actual values before this is run

CREATE EXTENSION postgres_fdw;

CREATE SERVER foreign_server
        FOREIGN DATA WRAPPER postgres_fdw
        OPTIONS (host 'localhost', port '5432', dbname 'niagads_user_dev');

CREATE USER MAPPING FOR <dbuser>
        SERVER foreign_server
        OPTIONS (user '<dbuser>', password '<dbpassword>');

create schema wdkuser;

CREATE FOREIGN TABLE wdkuser.config
(
  config_name VARCHAR(100) NOT NULL,
  config_value VARCHAR(255),
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'config');
;

CREATE FOREIGN TABLE wdkuser.users
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
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'users');

CREATE FOREIGN TABLE wdkuser.user_roles
(
  user_id NUMERIC(12) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'user_roles');


CREATE FOREIGN TABLE wdkuser.preferences
(
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  preference_name VARCHAR(200) NOT NULL,
  preference_value VARCHAR(4000),
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'preferences');


CREATE FOREIGN TABLE wdkuser.steps
(
  step_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
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
  assigned_weight NUMERIC(12),
  project_id VARCHAR(50) NOT NULL,
  project_version VARCHAR(50) NOT NULL,
  question_name VARCHAR(200) NOT NULL,
  strategy_id NUMERIC(12),
  display_params TEXT,
  result_message TEXT,
  prev_step_id NUMERIC(12),
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'steps');

CREATE FOREIGN TABLE wdkuser.strategies
(
     strategy_id NUMERIC(12) NOT NULL,
     user_id NUMERIC(12) NOT NULL,
     root_step_id NUMERIC(12) NOT NULL,
     project_id varchar(50) NOT NULL,
     version varchar(100),
     is_saved BOOLEAN NOT NULL,
     create_time TIMESTAMP DEFAULT NOW(),
     last_view_time TIMESTAMP DEFAULT NOW(),
     last_modify_time TIMESTAMP DEFAULT NOW(),
     description varchar(4000),
     signature varchar(40),
     name varchar(200) NOT NULL,
     saved_name varchar(200),
     is_deleted BOOLEAN,
     is_public BOOLEAN,
     prev_strategy_id NUMERIC(12),
     migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'strategies');

CREATE FOREIGN TABLE wdkuser.datasets (
  dataset_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12),
  dataset_name VARCHAR(100) NOT NULL,
  dataset_size NUMERIC(12) NOT NULL,
  content_checksum VARCHAR(40) NOT NULL,
  created_time TIMESTAMP NOT NULL,
  upload_file VARCHAR(2000),
  parser VARCHAR(50) NOT NULL,
  category_id NUMERIC(12),
  content TEXT,
  prev_dataset_id NUMERIC(12),
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'datasets');

CREATE FOREIGN TABLE wdkuser.dataset_values
(
  dataset_value_id NUMERIC(12) NOT NULL,
  dataset_id NUMERIC(12) NOT NULL,
  data1 VARCHAR(1999) NOT NULL,
  data2 VARCHAR(1999),
  data3 VARCHAR(1999),
  data4 VARCHAR(1999),
  data5 VARCHAR(1999),
  data6 VARCHAR(1999),
  data7 VARCHAR(1999),
  data8 VARCHAR(1999),
  data9 VARCHAR(1999),
  data10 VARCHAR(1999),
  data11 VARCHAR(1999),
  data12 VARCHAR(1999),
  data13 VARCHAR(1999),
  data14 VARCHAR(1999),
  data15 VARCHAR(1999),
  data16 VARCHAR(1999),
  data17 VARCHAR(1999),
  data18 VARCHAR(1999),
  data19 VARCHAR(1999),
  data20 VARCHAR(1999),
  prev_dataset_value_id NUMERIC(12),
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'dataset_values');

CREATE FOREIGN TABLE wdkuser.user_baskets
(
  basket_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  basket_name VARCHAR(100),
  project_id VARCHAR(50) NOT NULL,
  record_class VARCHAR(100) NOT NULL,
  is_default NUMERIC(1),
  category_id NUMERIC(12),
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  prev_basket_id NUMERIC(12),
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'user_baskets');

CREATE FOREIGN TABLE wdkuser.favorites
(
  favorite_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  record_class VARCHAR(100) NOT NULL,
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  record_note VARCHAR(200),
  record_group VARCHAR(50),
  prev_favorite_id NUMERIC(12),
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'favorites');

CREATE FOREIGN TABLE wdkuser.categories
(
  category_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  parent_id NUMERIC(12),
  category_type VARCHAR(50) NOT NULL,
  category_name VARCHAR(100) NOT NULL,
  description VARCHAR(200),
  prev_category_id NUMERIC(12),
  migration_id NUMERIC(12)
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'categories');


CREATE FOREIGN TABLE wdkuser.step_analysis
(
  analysis_id          NUMERIC(12) NOT NULL,
  step_id              NUMERIC(12) NOT NULL,
  display_name         VARCHAR(1024),
  is_new               NUMERIC(1),
  has_params           NUMERIC(1),
  invalid_step_reason  VARCHAR(1024),
  context_hash         VARCHAR(96),
  context              TEXT
)
SERVER foreign_server
        OPTIONS (schema_name 'wdkuser', table_name 'step_analysis');
