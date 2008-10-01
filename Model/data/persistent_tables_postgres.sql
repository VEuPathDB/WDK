
DROP SEQUENCE wdkstorage.dataset_indices_pkseq;
DROP SEQUENCE wdkstorage.answer_pkseq;
DROP SEQUENCE userlogins3.users_pkseq;

DROP TABLE userlogins3.user_datasets;
DROP TABLE userlogins3.histories;
DROP TABLE userlogins3.preferences;
DROP TABLE userlogins3.user_roles;
DROP TABLE userlogins3.users;

DROP TABLE wdkstorage.answer;
DROP TABLE wdkstorage.clob_values;
DROP TABLE wdkstorage.dataset_values;
DROP TABLE wdkstorage.dataset_indices;



/* =========================================================================
   create sequences
   ========================================================================= */


CREATE SEQUENCE wdkstorage.dataset_indices_pkseq INCREMENT BY 1 START WITH 1;

GRANT select on wdkstorage.dataset_indices_pkseq to GUS_W;
GRANT select on wdkstorage.dataset_indices_pkseq to GUS_R;


CREATE SEQUENCE wdkstorage.answwer_pkseq INCREMENT BY 1 START WITH 1;

GRANT select on wdkstorage.answwer_pkseq to GUS_W;
GRANT select on wdkstorage.answwer_pkseq to GUS_R;


CREATE SEQUENCE userlogins3.users_pkseq INCREMENT BY 1 START WITH 1;

GRANT select on userlogins3.users_pkseq to GUS_W;
GRANT select on userlogins3.users_pkseq to GUS_R;



/* =========================================================================
   tables in wdk engine schema
   ========================================================================= */


CREATE TABLE wdkstorage.answer
(
  answer_id NUMERIC(12) NOT NULL,
  answer_checksum varchar(40) NOT NULL,
  project_id varchar(50) NOT NULL,
  project_version varchar(50) NOT NULL,
  question_name varchar(200) NOT NULL,
  query_checksum  varchar(40) NOT NULL,
  params TEXT,
  result_message TEXT,
  CONSTRAINT "answer_pk" PRIMARY KEY (answer_id),
  CONSTRAINT "answer_uq1" UNIQUE (project_id, answer_checksum)
);

GRANT insert, update, delete on wdkstorage.answer to GUS_W;
GRANT select on wdkstorage.answer to GUS_R;

GRANT insert, update, delete on wdkstorage.sorting_attributes to GUS_W;
GRANT select on wdkstorage.sorting_attributes to GUS_R;



CREATE TABLE wdkstorage.dataset_indices
(
  dataset_id NUMERIC(12) NOT NULL,
  dataset_checksum VARCHAR(40) NOT NULL,
  summary varchar(200) NOT NULL,
  dataset_size NUMERIC(12) NOT NULL,
  PREV_DATASET_ID NUMERIC(12),
  CONSTRAINT "DATASET_INDICES_PK" PRIMARY KEY (dataset_id),
  CONSTRAINT "DATASET_CHECKSUM_UNIQUE" UNIQUE (dataset_checksum)
);

GRANT insert, update, delete on wdkstorage.dataset_indices to GUS_W;
GRANT select on wdkstorage.dataset_indices to GUS_R;


CREATE TABLE wdkstorage.dataset_values
(
  dataset_id NUMERIC(12) NOT NULL,
  dataset_value varchar(1999) NOT NULL,
  CONSTRAINT "DATASET_VALUES_DATASET_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES wdkstorage.dataset_indices (dataset_id)
);

CREATE INDEX wdkstorage.dataset_values_idx01 ON wdkstorage.dataset_values (dataset_id);

GRANT insert, update, delete on wdkstorage.dataset_values to GUS_W;
GRANT select on wdkstorage.dataset_values to GUS_R;


CREATE TABLE wdkstorage.clob_values
(
  clob_checksum VARCHAR(40) NOT NULL,
  clob_value TEXT NOT NULL,
  CONSTRAINT "CLOB_VALUES_PK" PRIMARY KEY (clob_checksum)
);

GRANT insert, update, delete on wdkstorage.clob_values to GUS_W;
GRANT select on wdkstorage.clob_values to GUS_R;


/* =========================================================================
   tables in user schema
   ========================================================================= */
   
CREATE TABLE userlogins3.users
(
  user_id NUMERIC(12) NOT NULL,
  email varchar(255) NOT NULL,
  passwd varchar(50) NOT NULL,
  is_guest NUMERIC(1) NOT NULL,
  signature varchar(40),
  register_time timestamp,
  last_active timestamp,
  last_name varchar(50),
  first_name varchar(50),
  middle_name varchar(50),
  title varchar(255),
  organization varchar(255),
  department varchar(255),
  address varchar(500),
  city varchar(255),
  state varchar(255),
  zip_code varchar(20),
  phone_NUMERIC varchar(50),
  country varchar(255),
  PREV_USER_ID NUMERIC(12),
  CONSTRAINT "USER_PK" PRIMARY KEY (user_id),
  CONSTRAINT "USER_EMAIL_UNIQUE" UNIQUE (email)
);

GRANT insert, update, delete on userlogins3.users to GUS_W;
GRANT select on userlogins3.users to GUS_R;


CREATE TABLE userlogins3.user_roles
(
  user_id NUMERIC(12) NOT NULL,
  user_role varchar(50) NOT NULL,
  CONSTRAINT "USER_ROLE_PK" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "USER_ROLE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins3.users (user_id) 
);

GRANT insert, update, delete on userlogins3.user_roles to GUS_W;
GRANT select on userlogins3.user_roles to GUS_R;


CREATE TABLE userlogins3.preferences
(
  user_id NUMERIC(12) NOT NULL,
  project_id varchar(50) NOT NULL,
  preference_name varchar(200) NOT NULL,
  preference_value varchar(4000),
  CONSTRAINT "PREFERENCES_PK" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "PREFERENCE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins3.users (user_id) 
);

GRANT insert, update, delete on userlogins3.preferences to GUS_W;
GRANT select on userlogins3.preferences to GUS_R;


CREATE TABLE userlogins3.histories
(
  history_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  answer_id NUMERIC(12) NOT NULL,
  create_time timestamp NOT NULL,
  last_run_time timestamp NOT NULL,
  estimate_size NUMERIC(12),
  answer_filter varchar(100),
  custom_name varchar(4000),
  is_boolean NUMERIC(1),
  is_deleted NUMERIC(1),
  display_params TEXT,
  CONSTRAINT "HISTORIES_PK" PRIMARY KEY (user_id, history_id),
  CONSTRAINT "HISTORY_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins3.users (user_id)
  CONSTRAINT "HISTORY_ANSWER_ID_FK" FOREIGN KEY (answer_id)
      REFERENCES wdkstorage.answer (answer_id)
);

GRANT insert, update, delete on userlogins3.histories to GUS_W;
GRANT select on userlogins3.histories to GUS_R;


CREATE TABLE userlogins3.user_datasets
(
  dataset_id NUMERIC(12) NOT NULL,
  user_id NUMERIC(12) NOT NULL,
  create_time timestamp NOT NULL,
  upload_file varchar(2000),
  CONSTRAINT "USER_DATASET_PK" PRIMARY KEY (dataset_id, user_id),
  CONSTRAINT "USER_DATASETS_DS_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES userlogins3.dataset_indices (dataset_id),
  CONSTRAINT "USER_DATASETS_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins3.users (user_id)
);

GRANT insert, update, delete on userlogins3.user_datasets to GUS_W;
GRANT select on userlogins3.user_datasets to GUS_R;

