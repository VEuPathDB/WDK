
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

GRANT select ON wdkstorage.dataset_indices_pkseq TO GUS_W;
GRANT select ON wdkstorage.dataset_indices_pkseq TO GUS_R;


CREATE SEQUENCE wdkstorage.answer_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON wdkstorage.answer_pkseq TO GUS_W;
GRANT select ON wdkstorage.answer_pkseq TO GUS_R;


CREATE SEQUENCE userlogins3.users_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON userlogins3.users_pkseq TO GUS_W;
GRANT select ON userlogins3.users_pkseq TO GUS_R;



/* =========================================================================
   tables in wdk engine schema
   ========================================================================= */


CREATE TABLE wdkstorage.answer
(
  answer_id NUMBER(12) NOT NULL,
  answer_checksum VARCHAR(40) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  project_version VARCHAR(50) NOT NULL,
  question_name VARCHAR(200) NOT NULL,
  query_checksum  VARCHAR(40) NOT NULL,
  params CLOB,
  result_message CLOB,
  CONSTRAINT "answer_pk" PRIMARY KEY (answer_id),
  CONSTRAINT "answer_uq1" UNIQUE (project_id, answer_checksum)
);

GRANT insert, update, delete ON wdkstorage.answer TO GUS_W;
GRANT select ON wdkstorage.answer TO GUS_R;
GRANT references ON wdkstorage.answer TO userlogins3;


CREATE TABLE wdkstorage.dataset_indices
(
  dataset_id NUMBER(12) NOT NULL,
  dataset_checksum VARCHAR(40) NOT NULL,
  summary VARCHAR(200) NOT NULL,
  dataset_size NUMBER(12) NOT NULL,
  PREV_DATASET_ID NUMBER(12),
  CONSTRAINT "DATASET_INDICES_PK" PRIMARY KEY (dataset_id),
  CONSTRAINT "DATASET_CHECKSUM_UNIQUE" UNIQUE (dataset_checksum)
);

GRANT insert, update, delete ON wdkstorage.dataset_indices TO GUS_W;
GRANT select ON wdkstorage.dataset_indices TO GUS_R;
GRANT references ON wdkstorage.dataset_indices TO userlogins3;


CREATE TABLE wdkstorage.dataset_values
(
  dataset_id NUMBER(12) NOT NULL,
  dataset_value VARCHAR(1999) NOT NULL,
  CONSTRAINT "DATASET_VALUES_DATASET_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES wdkstorage.dataset_indices (dataset_id)
);

CREATE INDEX wdkstorage.dataset_values_idx01 ON wdkstorage.dataset_values (dataset_id);

GRANT insert, update, delete ON wdkstorage.dataset_values TO GUS_W;
GRANT select ON wdkstorage.dataset_values TO GUS_R;


CREATE TABLE wdkstorage.clob_values
(
  clob_checksum VARCHAR(40) NOT NULL,
  clob_value CLOB NOT NULL,
  CONSTRAINT "CLOB_VALUES_PK" PRIMARY KEY (clob_checksum)
);

GRANT insert, update, delete ON wdkstorage.clob_values TO GUS_W;
GRANT select ON wdkstorage.clob_values TO GUS_R;


/* =========================================================================
   tables in user schema
   ========================================================================= */
   
CREATE TABLE userlogins3.users
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
  CONSTRAINT "USER_PK" PRIMARY KEY (user_id),
  CONSTRAINT "USER_EMAIL_UNIQUE" UNIQUE (email)
);

GRANT insert, update, delete ON userlogins3.users TO GUS_W;
GRANT select ON userlogins3.users TO GUS_R;


CREATE TABLE userlogins3.user_roles
(
  user_id NUMBER(12) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  CONSTRAINT "USER_ROLE_PK" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "USER_ROLE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins3.users (user_id) 
);

GRANT insert, update, delete ON userlogins3.user_roles TO GUS_W;
GRANT select ON userlogins3.user_roles TO GUS_R;


CREATE TABLE userlogins3.preferences
(
  user_id NUMBER(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  preference_name VARCHAR(200) NOT NULL,
  preference_value VARCHAR(4000),
  CONSTRAINT "PREFERENCES_PK" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "PREFERENCE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins3.users (user_id) 
);

GRANT insert, update, delete ON userlogins3.preferences TO GUS_W;
GRANT select ON userlogins3.preferences TO GUS_R;


CREATE TABLE userlogins3.histories
(
  history_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  answer_id NUMBER(12) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  last_run_time TIMESTAMP NOT NULL,
  estimate_size NUMBER(12),
  answer_filter VARCHAR(100),
  custom_name VARCHAR(4000),
  is_boolean NUMBER(1),
  is_deleted NUMBER(1),
  display_params CLOB,
  CONSTRAINT "HISTORIES_PK" PRIMARY KEY (user_id, history_id),
  CONSTRAINT "HISTORY_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins3.users (user_id),
  CONSTRAINT "HISTORY_ANSWER_ID_FK" FOREIGN KEY (answer_id)
      REFERENCES wdkstorage.answer (answer_id)
);

GRANT insert, update, delete ON userlogins3.histories TO GUS_W;
GRANT select ON userlogins3.histories TO GUS_R;


CREATE TABLE userlogins3.user_datasets
(
  dataset_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  upload_file VARCHAR(2000),
  CONSTRAINT "USER_DATASET_PK" PRIMARY KEY (dataset_id, user_id),
  CONSTRAINT "USER_DATASETS_DS_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES wdkstorage.dataset_indices (dataset_id),
  CONSTRAINT "USER_DATASETS_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins3.users (user_id)
);

GRANT insert, update, delete ON userlogins3.user_datasets TO GUS_W;
GRANT select ON userlogins3.user_datasets TO GUS_R;
