DROP SCHEMA IF EXISTS userlogins CASCADE;


CREATE SCHEMA userlogins AUTHORIZATION postgres;


CREATE SEQUENCE userlogins.users_pkseq INCREMENT BY 1 START WITH 1;

CREATE SEQUENCE userlogins.dataset_indices_pkseq INCREMENT BY 1 START WITH 1;


CREATE TABLE userlogins.summary_attributes
(
  summary_checksum VARCHAR(40) NOT NULL,
  attributes VARCHAR(4000) NOT NULL,
  CONSTRAINT "SUMMARY_ATTRIBUTES_PK" PRIMARY KEY (summary_checksum)
);


CREATE TABLE userlogins.sorting_attributes
(
  sorting_checksum VARCHAR(40) NOT NULL,
  attributes VARCHAR(4000) NOT NULL,
  CONSTRAINT "SORTING_ATTRIBUTES_PK" PRIMARY KEY (sorting_checksum)
);


CREATE TABLE userlogins.users
(
  user_id NUMERIC(12) NOT NULL,
  email VARCHAR(255) NOT NULL,
  passwd VARCHAR(50) NOT NULL,
  is_guest NUMERIC(1) NOT NULL,
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
  CONSTRAINT "USER_PK" PRIMARY KEY (user_id),
  CONSTRAINT "USER_EMAIL_UNIQUE" UNIQUE (email)
);


CREATE TABLE userlogins.user_roles
(
  user_id NUMERIC(12) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  CONSTRAINT "USER_ROLE_PK" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "USER_ROLE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id) 
);


CREATE TABLE userlogins.preferences
(
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  preference_name VARCHAR(200) NOT NULL,
  preference_value VARCHAR(4000),
  CONSTRAINT "PREFERENCES_PK" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "PREFERENCE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id) 
);


CREATE TABLE userlogins.histories
(
  history_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  question_name VARCHAR(255) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  last_run_time TIMESTAMP NOT NULL,
  custom_name VARCHAR(4000),
  estimate_size NUMERIC(12),
  query_signature VARCHAR(40),
  query_instance_checksum VARCHAR(40),
  is_boolean NUMERIC(1),
  is_deleted NUMERIC(1),
  params TEXT,
  CONSTRAINT "HISTORIES_PK" PRIMARY KEY (user_id, history_id, project_id),
  CONSTRAINT "HISTORY_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id)
);


CREATE TABLE userlogins.dataset_indices
(
  dataset_id NUMERIC(12) NOT NULL,
  dataset_checksum VARCHAR(40) NOT NULL,
  summary VARCHAR(200) NOT NULL,
  dataset_size NUMERIC(12) NOT NULL,
  CONSTRAINT "DATASET_INDICES_PK" PRIMARY KEY (dataset_id),
  CONSTRAINT "DATASET_CHECKSUM_UNIQUE" UNIQUE (dataset_checksum)
);


CREATE TABLE userlogins.dataset_values
(
  dataset_id NUMERIC(12) NOT NULL,
  dataset_value VARCHAR(4000) NOT NULL,
  CONSTRAINT "DATASET_VALUES_DATASET_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES userlogins.dataset_indices (dataset_id)
);

CREATE INDEX DATASET_VALUES_ID_INDEX ON userlogins.dataset_values (dataset_id);


CREATE TABLE userlogins.user_datasets
(
  dataset_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  upload_file VARCHAR(2000),
  CONSTRAINT "USER_DATASET_PK" PRIMARY KEY (dataset_id, user_id),
  CONSTRAINT "USER_DATASETS_DS_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES userlogins.dataset_indices (dataset_id),
  CONSTRAINT "USER_DATASETS_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id)
);


CREATE TABLE userlogins.clob_values
(
  clob_checksum VARCHAR(40) NOT NULL,
  clob_value TEXT NOT NULL,
  CONSTRAINT "CLOB_VALUES_PK" PRIMARY KEY (clob_checksum)
);
