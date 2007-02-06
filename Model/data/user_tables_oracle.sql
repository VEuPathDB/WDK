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


CREATE SEQUENCE userlogins.users_pkseq INCREMENT BY 1 START WITH 1;

GRANT select on userlogins.users_pkseq to GUS_W;
GRANT select on userlogins.users_pkseq to GUS_R;


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
  checksum varchar(40),
  signature varchar(40),
  is_boolean NUMBER(1),
  is_deleted NUMBER(1),
  params clob,
  CONSTRAINT "HISTORIES_PK" PRIMARY KEY (user_id, history_id, project_id),
  CONSTRAINT "HISTORY_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id) 
);

GRANT insert, update, delete on userlogins.histories to GUS_W;
GRANT select on userlogins.histories to GUS_R;


CREATE SEQUENCE userlogins.dataset_index_pkseq INCREMENT BY 1 START WITH 1;

GRANT select on userlogins.dataset_index_pkseq to GUS_W;
GRANT select on userlogins.dataset_index_pkseq to GUS_R;


CREATE TABLE userlogins.dataset_index
(
  dataset_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  create_time timestamp NOT NULL,
  upload_file varchar(2000),
  summary varchar(2000) NOT NULL,
  dataset_size number(12) NOT NULL,
  CONSTRAINT "DATASET_INDEX_PK" PRIMARY KEY (dataset_id),
  CONSTRAINT "DATASET_INDEX_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id)
);

GRANT insert, update, delete on userlogins.dataset_index to GUS_W;
GRANT select on userlogins.dataset_index to GUS_R;


CREATE TABLE userlogins.dataset_data
(
  dataset_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  dataset_value varchar(2000) NOT NULL,
  CONSTRAINT "DATASET_DATASET_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES userlogins.dataset_index (dataset_id),
  CONSTRAINT "DATASET_DATA_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins.users (user_id)
);

CREATE INDEX userlogins.dataset_data_id_idx on userlogins.dataset_data (dataset_id, user_id);

GRANT insert, update, delete on userlogins.dataset_data to GUS_W;
GRANT select on userlogins.dataset_data to GUS_R;

