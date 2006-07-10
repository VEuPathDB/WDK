CREATE TABLE users
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


CREATE TABLE user_roles
(
  email varchar(255) NOT NULL,
  "role" varchar(50) NOT NULL,
  CONSTRAINT "USER_ROLES_PK" PRIMARY KEY (email, "role"),
  CONSTRAINT "USER_ROLES_EMAIL_FK" FOREIGN KEY (email)
      REFERENCES users (email) 
);


CREATE TABLE preferences
(
  preference_id NUMBER(10) NOT NULL,
  email varchar(255) NOT NULL,
  project_id varchar(50) NOT NULL,
  preference_name varchar(100) NOT NULL,
  preference_value CLOB,
  CONSTRAINT "PREFERENCES_PK" PRIMARY KEY (preference_id),
  CONSTRAINT "USER_PREFERENCE_EMAIL_FK" FOREIGN KEY (email)
      REFERENCES users (email) 
);


CREATE TABLE histories
(
  history_id NUMBER(10) NOT NULL,
  email varchar(255) NOT NULL,
  project_id varchar(50) NOT NULL,
  full_name varchar(20) NOT NULL,
  created_time date NOT NULL,
  custom_name varchar(200),
  params clob,
  CONSTRAINT "HISTORIES_PK" PRIMARY KEY (history_id),
  CONSTRAINT "USER_HISTORY_EMAIL_FK" FOREIGN KEY (email)
      REFERENCES users (email) 
);
