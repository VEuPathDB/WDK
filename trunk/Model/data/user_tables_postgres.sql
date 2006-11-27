create schema logins;


CREATE TABLE logins.users
(
  email varchar(255) NOT NULL,
  "password" varchar(50),
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
  CONSTRAINT "USERS_PK" PRIMARY KEY (email)
);


CREATE TABLE logins.user_roles
(
  email varchar(255) NOT NULL,
  "role" varchar(50) NOT NULL,
  CONSTRAINT "USER_ROLES_PK" PRIMARY KEY (email, "role"),
  CONSTRAINT "USER_ROLES_EMAIL_FK" FOREIGN KEY (email)
      REFERENCES logins.users (email) 
);


CREATE TABLE logins.preferences
(
  email varchar(255) NOT NULL,
  project_id varchar(50) NOT NULL,
  preference_name varchar(200) NOT NULL,
  preference_value varchar(4000),
  CONSTRAINT "PREFERENCES_PK" PRIMARY KEY (email, project_id, preference_name),
  CONSTRAINT "USER_PREFERENCE_EMAIL_FK" FOREIGN KEY (email)
      REFERENCES logins.users (email) 
);


CREATE TABLE logins.histories
(
  history_id NUMERIC(10) NOT NULL,
  email varchar(255) NOT NULL,
  project_id varchar(50) NOT NULL,
  full_name varchar(20) NOT NULL,
  created_time date NOT NULL,
  custom_name varchar(200),
  params varchar(4000),
  CONSTRAINT "HISTORIES_PK" PRIMARY KEY (history_id),
  CONSTRAINT "USER_HISTORY_EMAIL_FK" FOREIGN KEY (email)
      REFERENCES logins.users (email) 
);
