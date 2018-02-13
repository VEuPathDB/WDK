GRANT references ON wdkengine.dataset_indices TO userlogins4;

/* =========================================================================
   create sequences
   ========================================================================= */

-- SELECT max(user_id) + 1 FROM userlogins3.users;                 -- 77687291
--                                                                    34213873
CREATE SEQUENCE userlogins4.users_pkseq INCREMENT BY 10 START WITH 80000000;

CREATE SEQUENCE userlogins4.migration_pkseq INCREMENT BY 10 START WITH 1;

-- SELECT max(strategy_id) + 1 FROM userlogins3.strategies;             -- 35033521
CREATE SEQUENCE userlogins4.strategies_pkseq INCREMENT BY 10 START WITH 40000000;

-- SELECT max(step_id) + 1 FROM userlogins3.steps;                -- 52482151
CREATE SEQUENCE userlogins4.steps_pkseq INCREMENT BY 10 START WITH 60000000;

-- SELECT max(user_dataset_id) + 1 FROM userlogins3.user_datasets2;        -- 5686211
CREATE SEQUENCE userlogins4.user_datasets2_pkseq INCREMENT BY 10 START WITH 6000000;

CREATE SEQUENCE userlogins4.user_baskets_pkseq INCREMENT BY 10 START WITH 1;

CREATE SEQUENCE userlogins4.favorites_pkseq INCREMENT BY 10 START WITH 1;



/* =========================================================================
   tables in user schema
   ========================================================================= */
   
CREATE TABLE userlogins4.users (
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

CREATE INDEX userlogins4.users_idx01 ON userlogins4.users (is_guest);
CREATE INDEX userlogins4.users_idx02 ON userlogins4.users (prev_user_id);

GRANT SELECT ON userlogins4.users TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON userlogins4.users TO GUS_W;


CREATE TABLE userlogins4.user_roles
(
  user_id NUMBER(12) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  migration_id NUMBER(12),
  CONSTRAINT "user_roles_pk" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "user_roles_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id) 
);

GRANT SELECT ON userlogins4.user_roles TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON userlogins4.user_roles TO GUS_W;


CREATE TABLE userlogins4.preferences
(
  user_id NUMBER(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  preference_name VARCHAR(200) NOT NULL,
  preference_value VARCHAR(4000),
  migration_id NUMBER(12),
  CONSTRAINT "preferences_pk" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "preferences_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id) 
);

GRANT SELECT ON userlogins4.preferences TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON userlogins4.preferences TO GUS_W;



CREATE TABLE userlogins4.steps
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
  prev_step_id NUMBER(12),
  assigned_weight NUMBER(12),
  migration_id NUMBER(12),
  project_id VARCHAR(50) NOT NULL,
  project_version VARCHAR(50) NOT NULL,
  question_name VARCHAR(200) NOT NULL,
  strategy_id NUMBER(12),
  display_params CLOB,
  result_message CLOB,
  CONSTRAINT "steps_pk" PRIMARY KEY (step_id),
  CONSTRAINT "steps_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id)
);

CREATE INDEX userlogins4.steps_idx01 ON userlogins4.steps (user_id, left_child_id, right_child_id);
CREATE INDEX userlogins4.steps_idx02 ON userlogins4.steps (project_id, question_name, user_id);
CREATE INDEX userlogins4.steps_idx03 ON userlogins4.steps (is_deleted, user_id, project_id);
CREATE INDEX userlogins4.steps_idx04 ON userlogins4.steps (is_valid, user_id, project_id);
CREATE INDEX userlogins4.steps_idx05 ON userlogins4.steps (last_run_time, user_id, project_id);
CREATE INDEX userlogins4.steps_idx06 ON userlogins4.steps (strategy_id, user_id, project_id);

GRANT SELECT ON userlogins4.steps TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON userlogins4.steps TO GUS_W;




CREATE TABLE userlogins4.strategies
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
         REFERENCES userlogins4.steps (step_id),
     CONSTRAINT "strategies_fk02" FOREIGN KEY (user_id)
         REFERENCES userlogins4.users (user_id)
);

CREATE INDEX userlogins4.strategies_idx01 ON userlogins4.strategies (signature, project_id);
CREATE INDEX userlogins4.strategies_idx02 ON userlogins4.strategies (user_id, project_id, is_deleted, is_saved);
CREATE INDEX userlogins4.strategies_idx03 ON userlogins4.strategies (root_step_id, project_id, user_id, is_saved, is_deleted);
CREATE INDEX userlogins4.strategies_idx04 ON userlogins4.strategies (is_deleted, is_saved, name, project_id, user_id);
CREATE INDEX userlogins4.strategies_idx05 ON userlogins4.strategies (project_id, is_public, is_saved, is_deleted);

GRANT SELECT ON userlogins4.strategies TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON userlogins4.strategies TO GUS_W;



CREATE TABLE userlogins4.user_datasets2
(
  user_dataset_id NUMBER(12) NOT NULL,
  dataset_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  upload_file VARCHAR(2000),
  prev_user_dataset_id NUMBER(12),
  migration_id NUMBER(12),
  CONSTRAINT "user_datasets2_pk" PRIMARY KEY (user_dataset_id),
  CONSTRAINT "user_datasets2_uq01" UNIQUE (dataset_id, user_id),
  CONSTRAINT "user_datasets2_fk01" FOREIGN KEY (dataset_id)
      REFERENCES wdkengine.dataset_indices (dataset_id),
  CONSTRAINT "user_datasets2_fk02" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id)
);

CREATE INDEX userlogins4.user_datasets2_idx01 ON userlogins4.user_datasets2 (user_id);

GRANT SELECT ON userlogins4.user_datasets2 TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON userlogins4.user_datasets2 TO GUS_W;


CREATE TABLE userlogins4.user_baskets
(
  basket_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  record_class VARCHAR(100) NOT NULL,
  pk_column_1 VARCHAR(1999) NOT NULL,
  pk_column_2 VARCHAR(1999),
  pk_column_3 VARCHAR(1999),
  CONSTRAINT "user_baskets_pk" PRIMARY KEY (basket_id),
  CONSTRAINT "user_baskets_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "user_baskets_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id)
);

CREATE INDEX userlogins4.user_baskets_idx01 ON userlogins4.user_baskets (project_id, record_class);

GRANT SELECT ON userlogins4.user_baskets TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON userlogins4.user_baskets TO GUS_W;



CREATE TABLE userlogins4.favorites
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
  CONSTRAINT "favorites_pk" PRIMARY KEY (favorite_id),
  CONSTRAINT "favorites_uq01" UNIQUE (user_id, project_id, record_class, pk_column_1, pk_column_2, pk_column_3),
  CONSTRAINT "favorites_fk01" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id)
);

CREATE INDEX userlogins4.favorites_idx01 ON userlogins4.favorites (record_class, project_id);

GRANT SELECT ON userlogins4.favorites TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON userlogins4.favorites TO GUS_W;
