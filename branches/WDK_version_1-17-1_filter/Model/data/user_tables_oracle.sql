CREATE USER userlogins
IDENTIFIED BY loginpwd
QUOTA UNLIMITED ON users
QUOTA UNLIMITED ON gus
DEFAULT TABLESPACE gus
TEMPORARY TABLESPACE temp;

ALTER USER userlogins ACCOUNT LOCK;

GRANT SCHEMA_OWNER TO userlogins;
GRANT GUS_R TO userlogins;
GRANT GUS_W TO userlogins;
GRANT CREATE VIEW TO userlogins;



DROP SEQUENCE userlogins.users_pkseq;
DROP SEQUENCE userlogins.dataset_indices_pkseq;

DROP TABLE userlogins.clob_values;
DROP TABLE userlogins.user_datasets;
DROP TABLE userlogins.dataset_values;
DROP TABLE userlogins.dataset_indices;
DROP TABLE userlogins.histories;
DROP TABLE userlogins.preferences;
DROP TABLE userlogins.user_roles;
DROP TABLE userlogins.users;
DROP TABLE userlogins.sorting_attributes;
DROP TABLE userlogins.summary_attributes;
DROP TABLE userlogins.user_strategy_tree;
DROP TABLE userlogins.user_strategies;
DROP TABLE userlogins.user_answer_tree;
DROP TABLE userlogins.user_answers;
DROP TABLE userlogins.answers;



CREATE SEQUENCE userlogins.users_pkseq INCREMENT BY 1 START WITH 1;

GRANT select on userlogins.users_pkseq to GUS_W;
GRANT select on userlogins.users_pkseq to GUS_R;


CREATE SEQUENCE userlogins.dataset_indices_pkseq INCREMENT BY 1 START WITH 1;

GRANT select on userlogins.dataset_indices_pkseq to GUS_W;
GRANT select on userlogins.dataset_indices_pkseq to GUS_R;



CREATE TABLE userlogins.summary_attributes
(
  summary_checksum VARCHAR(40) NOT NULL,
  attributes VARCHAR(4000) NOT NULL,
  CONSTRAINT "SUMMARY_ATTRIBUTES_PK" PRIMARY KEY (summary_checksum)
);

GRANT insert, update, delete on userlogins.summary_attributes to GUS_W;
GRANT select on userlogins.summary_attributes to GUS_R;


CREATE TABLE userlogins.sorting_attributes
(
  sorting_checksum VARCHAR(40) NOT NULL,
  attributes VARCHAR(4000) NOT NULL,
  CONSTRAINT "SORTING_ATTRIBUTES_PK" PRIMARY KEY (sorting_checksum)
);

GRANT insert, update, delete on userlogins.sorting_attributes to GUS_W;
GRANT select on userlogins.sorting_attributes to GUS_R;


CREATE TABLE userlogins.users
(
  user_id NUMBER(12) NOT NULL,
  email varchar(255) NOT NULL,
  passwd varchar(50) NOT NULL,
  is_guest NUMBER(1) NOT NULL,
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
  phone_number varchar(50),
  country varchar(255),
  PREV_USER_ID number(12),
  CONSTRAINT "USER_PK" PRIMARY KEY (user_id),
  CONSTRAINT "USER_EMAIL_UNIQUE" UNIQUE (email)
);

GRANT insert, update, delete on userlogins.users to GUS_W;
GRANT select on userlogins.users to GUS_R;


CREATE TABLE userlogins.user_roles
(
  user_id NUMBER(12) NOT NULL,
  user_role varchar(50) NOT NULL,
  CONSTRAINT "USER_ROLE_PK" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "USER_ROLE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id) 
);

GRANT insert, update, delete on userlogins.user_roles to GUS_W;
GRANT select on userlogins.user_roles to GUS_R;


CREATE TABLE userlogins.preferences
(
  user_id NUMBER(12) NOT NULL,
  project_id varchar(50) NOT NULL,
  preference_name varchar(200) NOT NULL,
  preference_value varchar(4000),
  CONSTRAINT "PREFERENCES_PK" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "PREFERENCE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id) 
);

GRANT insert, update, delete on userlogins.preferences to GUS_W;
GRANT select on userlogins.preferences to GUS_R;


CREATE TABLE userlogins.histories
(
  history_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  project_id varchar(50) NOT NULL,
  question_name varchar(255) NOT NULL,
  create_time timestamp NOT NULL,
  last_run_time timestamp NOT NULL,
  custom_name varchar(4000),
  estimate_size NUMBER(12),
  query_signature VARCHAR(40),
  query_instance_checksum VARCHAR(40),
  is_boolean NUMBER(1),
  is_deleted NUMBER(1),
  params CLOB,
  CONSTRAINT "HISTORIES_PK" PRIMARY KEY (user_id, history_id, project_id),
  CONSTRAINT "HISTORY_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id)
);

GRANT insert, update, delete on userlogins.histories to GUS_W;
GRANT select on userlogins.histories to GUS_R;


CREATE TABLE userlogins.dataset_indices
(
  dataset_id NUMBER(12) NOT NULL,
  dataset_checksum VARCHAR(40) NOT NULL,
  summary varchar(200) NOT NULL,
  dataset_size number(12) NOT NULL,
  PREV_DATASET_ID number(12),
  CONSTRAINT "DATASET_INDICES_PK" PRIMARY KEY (dataset_id),
  CONSTRAINT "DATASET_CHECKSUM_UNIQUE" UNIQUE (dataset_checksum)
);

GRANT insert, update, delete on userlogins.dataset_indices to GUS_W;
GRANT select on userlogins.dataset_indices to GUS_R;


CREATE TABLE userlogins.dataset_values
(
  dataset_id NUMBER(12) NOT NULL,
  dataset_value varchar(4000) NOT NULL,
  CONSTRAINT "DATASET_VALUES_DATASET_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES userlogins.dataset_indices (dataset_id)
);

CREATE INDEX DATASET_VALUES_ID_INDEX ON userlogins.dataset_values (dataset_id);

GRANT insert, update, delete on userlogins.dataset_values to GUS_W;
GRANT select on userlogins.dataset_values to GUS_R;


CREATE TABLE userlogins.user_datasets
(
  dataset_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  create_time timestamp NOT NULL,
  upload_file varchar(2000),
  CONSTRAINT "USER_DATASET_PK" PRIMARY KEY (dataset_id, user_id),
  CONSTRAINT "USER_DATASETS_DS_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES userlogins.dataset_indices (dataset_id),
  CONSTRAINT "USER_DATASETS_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id)
);

GRANT insert, update, delete on userlogins.user_datasets to GUS_W;
GRANT select on userlogins.user_datasets to GUS_R;


CREATE TABLE userlogins.clob_values
(
  clob_checksum VARCHAR(40) NOT NULL,
  clob_value CLOB NOT NULL,
  CONSTRAINT "CLOB_VALUES_PK" PRIMARY KEY (clob_checksum)
);

GRANT insert, update, delete on userlogins.clob_values to GUS_W;
GRANT select on userlogins.clob_values to GUS_R;


CREATE TABLE userlogins.answers
(
    answer_id NUMBER(10) NOT NULL,
    project_id varchar(50) NOT NULL,
    question_name varchar(255) NOT NULL,
    estimate_size NUMBER(12),
    query_signature VARCHAR(40),
    query_instance_checksum VARCHAR(40),
    is_boolean NUMBER(1),
    params CLOB,
    CONSTRAINT "ANSWERS_PK" PRIMARY KEY (answer_id, project_id)
);

GRANT insert, update, delete on userlogins.answers to GUS_W;
GRANT select on userlogins.answers to GUS_R;


CREATE TABLE userlogins.user_answers
(
     user_answer_id NUMBER(12) NOT NULL,
     project_id varchar(50) NOT NULL,
     user_id NUMBER(12) NOT NULL,
     answer_id NUMBER(12) NOT NULL,
     create_time timestamp NOT NULL,
     last_run_time timestamp NOT NULL,
     site_version varchar(5),
     custom_name varchar(200),
     is_deleted NUMBER(1),
     CONSTRAINT "USER_ANSWERS_PK" PRIMARY KEY (user_id, user_answer_id, project_id),
     CONSTRAINT "USER_ANSWERS_ANSWER_ID_FK" FOREIGN KEY (answer_id, project_id)
         REFERENCES answers (answer_id, project_id),
     CONSTRAINT "USER_ANSWERS_USER_ID_FK" FOREIGN KEY (user_id)
         REFERENCES userlogins.users (user_id)
);

GRANT insert, update, delete on userlogins.user_answers to GUS_W;
GRANT select on userlogins.user_answers to GUS_R;


CREATE TABLE userlogins.user_strategies
(
     user_strategy_id NUMBER(12) NOT NULL,
     user_id NUMBER(12) NOT NULL,
     user_answer_id NUMBER(12) NOT NULL,
     project_id varchar(50) NOT NULL,
     is_saved NUMBER(1) NOT NULL,
     name varchar(200),
     CONSTRAINT "USER_STRATEGIES_PK" PRIMARY KEY (user_id, user_strategy_id, project_id),
     CONSTRAINT "USER_STRATEGIES_ANSWER_FK" FOREIGN KEY (user_id, user_answer_id, project_id)
         REFERENCES userlogins.user_answers (user_id, user_answer_id, project_id),
     CONSTRAINT "USER_STRATEGIES_USER_ID_FK" FOREIGN KEY (user_id)
         REFERENCES userlogins.users (user_id)
     CONSTRAINT "USER_STRATEGIES_NAME_UNIQUE" UNIQUE (name)
);

GRANT insert, update, delete on userlogins.user_strategies to GUS_W;
GRANT select on userlogins.user_strategies to GUS_R;


CREATE TABLE userlogins.user_answer_tree
(
    parent_answer_id NUMBER(12) NOT NULL,
    child_answer_id NUMBER(12) NOT NULL,
    user_id NUMBER(12) NOT NULL,
    project_id varchar(50) NOT NULL,
    child_order NUMBER(1) NOT NULL,
    CONSTRAINT "USER_ANSWER_TREE_PK" PRIMARY KEY (user_id, parent_answer_id, child_answer_id, project_id),
    CONSTRAINT "USER_ANSWER_TREE_PARENT_FK" FOREIGN KEY (user_id, parent_answer_id, project_id)
        REFERENCES userlogins.user_answers (user_id, user_answer_id, project_id),
    CONSTRAINT "USER_ANSWER_TREE_CHILD_FK" FOREIGN KEY (user_id, child_answer_id, project_id)
        REFERENCES userlogins.user_answers (user_id, user_answer_id, project_id),
    CONSTRAINT "USER_ANSWER_TREE_USER_ID_FK" FOREIGN KEY (user_id)
        REFERENCES userlogins.users (user_id)
);

GRANT insert, update, delete on userlogins.user_answer_tree to GUS_W;
GRANT select on userlogins.user_answer_tree to GUS_R;


CREATE TABLE userlogins.user_strategy_tree
(
    parent_strategy_id NUMBER(12) NOT NULL,
    child_strategy_id NUMBER(12) NOT NULL,
    user_id NUMBER(12) NOT NULL,
    project_id varchar(50) NOT NULL,
    CONSTRAINT "USER_STRATEGY_TREE_PK" PRIMARY KEY (user_id, parent_strategy_id, child_strategy_id, project_id),
    CONSTRAINT "USER_STRATEGY_TREE_PARENT_FK" FOREIGN KEY (user_id, parent_strategy_id, project_id)
        REFERENCES userlogins.user_strategies (user_id, user_strategy_id, project_id),
    CONSTRAINT "USER_STRATEGY_TREE_CHILD_FK" FOREIGN KEY (user_id, child_strategy_id, project_id)
        REFERENCES userlogins.user_strategies (user_id, user_strategy_id, project_id),
    CONSTRAINT "USER_STRATEGY_TREE_USER_ID_FK" FOREIGN KEY (user_id)
        REFERENCES userlogins.users (user_id)
);

GRANT insert, update, delete on userlogins.user_strategy_tree to GUS_W;
GRANT select on userlogins.user_strategy_tree to GUS_R;
