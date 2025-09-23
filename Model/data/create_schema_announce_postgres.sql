/*==============================================================================
 * This SQL script will create additional User DB schemas and all required
 * tables and sequences common to Ebrc websites.
 *============================================================================*/

set role COMM_WDK_W;  -- TODO: remove GRANTs

DROP SCHEMA IF EXISTS announce CASCADE;
CREATE SCHEMA IF NOT EXISTS announce;
GRANT USAGE ON SCHEMA announce TO COMM_WDK_W;

/*==============================================================================
 * create tables
 *============================================================================*/

CREATE TABLE announce.projects 
(
  PROJECT_ID    INTEGER NOT NULL, 
  PROJECT_NAME  VARCHAR(150) NOT NULL,
  UNIQUE (PROJECT_NAME),
  CONSTRAINT "PROJECTS_PKEY" PRIMARY KEY (PROJECT_ID)
);

-- existing projects
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(10, 'CryptoDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(20, 'GiardiaDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(30, 'PlasmoDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(40, 'ToxoDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(50, 'TrichDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(60, 'TriTrypDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(70, 'EuPathDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(80, 'MicrosporidiaDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(90, 'AmoebaDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(100, 'VectorBase');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(110, 'MicrobiomeDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(120, 'UniDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(3, 'PiroplasmaDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(13, 'FungiDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(23, 'OrthoMCL');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(33, 'ClinEpiDB');
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(43, 'HostDB');


GRANT SELECT, INSERT, UPDATE, DELETE ON announce.projects TO COMM_WDK_W;

--==============================================================================

CREATE TABLE announce.category
(
  CATEGORY_ID    INTEGER NOT NULL,
  CATEGORY_NAME  VARCHAR(150) NOT NULL,
  UNIQUE (CATEGORY_NAME),
  CONSTRAINT "CATEGORY_PKEY" PRIMARY KEY (CATEGORY_ID)
);

INSERT INTO announce.category(CATEGORY_ID, CATEGORY_NAME) VALUES(10, 'Information');
INSERT INTO announce.category(CATEGORY_ID, CATEGORY_NAME) VALUES(20, 'Degraded');
INSERT INTO announce.category(CATEGORY_ID, CATEGORY_NAME) VALUES(30, 'Down');
INSERT INTO announce.category(CATEGORY_ID, CATEGORY_NAME) VALUES(230, 'Event');

GRANT SELECT, INSERT, UPDATE, DELETE ON announce.category TO COMM_WDK_W;

--==============================================================================

CREATE TABLE announce.messages
(
  MESSAGE_ID        BIGINT NOT NULL,
  MESSAGE_TEXT      VARCHAR(4000) NOT NULL,
  MESSAGE_CATEGORY  VARCHAR(150) NOT NULL,
  START_DATE        DATE NOT NULL,
  STOP_DATE         DATE NOT NULL,
  ADMIN_COMMENTS    VARCHAR(4000),
  TIME_SUBMITTED    TIMESTAMP NOT NULL,
  CONSTRAINT "MESSAGES_PKEY" PRIMARY KEY (MESSAGE_ID)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON announce.messages TO COMM_WDK_W;

--==============================================================================

CREATE TABLE announce.message_projects
(
  MESSAGE_ID  BIGINT NOT NULL,
  PROJECT_ID  INTEGER NOT NULL,
  CONSTRAINT "MESSAGE_PROJECT_PKEY" PRIMARY KEY (MESSAGE_ID, PROJECT_ID),
  CONSTRAINT "MESSAGE_ID_FKEY" FOREIGN KEY (MESSAGE_ID)
      REFERENCES announce.messages (MESSAGE_ID),
  CONSTRAINT "PROJECT_ID_FKEY" FOREIGN KEY (PROJECT_ID)
      REFERENCES announce.projects (PROJECT_ID)
);

CREATE INDEX message_projects_idx01 ON announce.message_projects (project_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON announce.message_projects TO COMM_WDK_W;

/*==============================================================================
 * create sequences
 * ApiCommN for 100000000, ApiCommS for 100000003
 *============================================================================*/

-- note start value may change depending on initial project list: see above
CREATE SEQUENCE announce.projects_id_pkseq INCREMENT BY 10 START WITH 100;
GRANT SELECT ON announce.projects_id_pkseq TO COMM_WDK_W;

-- note start value may change depending on initial project list; see above
CREATE SEQUENCE announce.category_id_pkseq INCREMENT BY 10 START WITH 40;
GRANT SELECT ON announce.category_id_pkseq TO COMM_WDK_W;

CREATE SEQUENCE announce.messages_id_pkseq INCREMENT BY 10 START WITH 10;
GRANT SELECT ON announce.messages_id_pkseq TO COMM_WDK_W;

