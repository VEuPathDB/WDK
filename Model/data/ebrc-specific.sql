/*==============================================================================
 * This SQL script will create additional User DB schemas and all required
 * tables and sequences common to Ebrc websites.
 *============================================================================*/

CREATE USER announce
  IDENTIFIED BY DCB802868A9EF9F9 -- encoding of standard password
  QUOTA UNLIMITED ON gus
  DEFAULT TABLESPACE gus
  TEMPORARY TABLESPACE temp;

GRANT GUS_R TO announce;
GRANT GUS_W TO announce;
GRANT CREATE VIEW TO announce;
GRANT CREATE MATERIALIZED VIEW TO announce;
GRANT CREATE TABLE TO announce;
GRANT CREATE SYNONYM TO announce;
GRANT CREATE SESSION TO announce;
GRANT CREATE ANY INDEX TO announce;
GRANT CREATE TRIGGER TO announce;
GRANT CREATE ANY TRIGGER TO announce;

/*==============================================================================
 * create tables
 *============================================================================*/

CREATE TABLE announce.projects 
(
  PROJECT_ID    NUMBER(3,0) NOT NULL ENABLE, 
  PROJECT_NAME  VARCHAR2(150 BYTE) NOT NULL ENABLE, 
  CONSTRAINT "PROJECTS_PKEY" PRIMARY KEY (PROJECT_ID)
);

-- TODO: insert project IDs for all relevant DBs (see apicommN)
INSERT INTO announce.projects(PROJECT_ID, PROJECT_NAME) VALUES(33, 'ClinEpiDB');

GRANT SELECT ON announce.projects TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON announce.projects TO GUS_W;

--==============================================================================

CREATE TABLE announce.category
(
  CATEGORY_ID    NUMBER(3,0) NOT NULL ENABLE,
  CATEGORY_NAME  VARCHAR2(150 BYTE) NOT NULL ENABLE,
  CONSTRAINT "CATEGORY_PKEY" PRIMARY KEY (CATEGORY_ID)
);

INSERT INTO announce.category(CATEGORY_ID, CATEGORY_NAME) VALUES(10, 'Information');
INSERT INTO announce.category(CATEGORY_ID, CATEGORY_NAME) VALUES(20, 'Degraded');
INSERT INTO announce.category(CATEGORY_ID, CATEGORY_NAME) VALUES(30, 'Down');
INSERT INTO announce.category(CATEGORY_ID, CATEGORY_NAME) VALUES(230, 'Event');

GRANT SELECT ON announce.category TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON announce.category TO GUS_W;

--==============================================================================

CREATE TABLE announce.messages
(
  MESSAGE_ID        NUMBER(10,0) NOT NULL ENABLE,
  MESSAGE_TEXT      VARCHAR2(4000 BYTE) NOT NULL ENABLE,
  MESSAGE_CATEGORY  VARCHAR2(150 BYTE) NOT NULL ENABLE,
  START_DATE        DATE NOT NULL ENABLE,
  STOP_DATE         DATE NOT NULL ENABLE,
  ADMIN_COMMENTS    VARCHAR2(4000 BYTE),
  TIME_SUBMITTED    TIMESTAMP NOT NULL ENABLE,
  CONSTRAINT "MESSAGES_PKEY" PRIMARY KEY (MESSAGE_ID)
);

GRANT SELECT ON announce.messages TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON announce.messages TO GUS_W;

--==============================================================================

CREATE TABLE announce.message_projects
(
  MESSAGE_ID  NUMBER(10,0) NOT NULL ENABLE,
  PROJECT_ID  NUMBER(3,0) NOT NULL ENABLE,
  CONSTRAINT "MESSAGE_PROJECT_PKEY" PRIMARY KEY (MESSAGE_ID, PROJECT_ID),
  CONSTRAINT "MESSAGE_ID_FKEY" FOREIGN KEY (MESSAGE_ID)
      REFERENCES announce.messages (MESSAGE_ID) ENABLE,
  CONSTRAINT "PROJECT_ID_FKEY" FOREIGN KEY (PROJECT_ID)
      REFERENCES announce.projects (PROJECT_ID) ENABLE
);

CREATE INDEX announce.message_projects_idx01 ON announce.message_projects (project_id);

GRANT SELECT ON announce.message_projects TO GUS_R;
GRANT INSERT, UPDATE, DELETE ON announce.message_projects TO GUS_W;

/*==============================================================================
 * create sequences
 * ApiCommN for 100000000, ApiCommS for 100000003
 *============================================================================*/

CREATE SEQUENCE announce.projects_id_pkseq INCREMENT BY 10 START WITH 100000000;

GRANT SELECT ON announce.projects_id_pkseq TO GUS_R;
GRANT SELECT ON announce.projects_id_pkseq TO GUS_W;


CREATE SEQUENCE announce.category_id_pkseq INCREMENT BY 10 START WITH 100000000;

GRANT SELECT ON announce.category_id_pkseq TO GUS_R;
GRANT SELECT ON announce.category_id_pkseq TO GUS_W;

CREATE SEQUENCE announce.messages_id_pkseq INCREMENT BY 10 START WITH 100000000;

GRANT SELECT ON announce.messages_id_pkseq TO GUS_R;
GRANT SELECT ON announce.messages_id_pkseq TO GUS_W;

