reset role;

CREATE SCHEMA IF NOT EXISTS UPLOADS;
GRANT USAGE ON SCHEMA UPLOADS TO COMM_WDK_W;

set role COMM_WDK_W;

CREATE TABLE UPLOADS.USERFILE (
  USERFILEID BIGINT NOT NULL, 
  FILENAME VARCHAR(255) NOT NULL, 
  PATH VARCHAR(255), 
  CHECKSUM VARCHAR(64), 
  FILESIZE INTEGER, 
  FORMAT VARCHAR(255), 
  ISDIR BOOLEAN, 
  UPLOADTIME TIMESTAMP NOT NULL, 
  OWNERUSERID VARCHAR(40) NOT NULL, 
  EMAIL VARCHAR(255), 
  TITLE VARCHAR(4000), 
  NOTES VARCHAR(4000), 
  PROJECTNAME VARCHAR(200), 
  PROJECTVERSION VARCHAR(100), 
  IS_VISIBLE BOOLEAN DEFAULT TRUE, 
  CONSTRAINT FIELDID_PKEY PRIMARY KEY (USERFILEID)
);

GRANT insert, update, delete on uploads.userfile to COMM_WDK_W;
GRANT select on uploads.userfile to GUS_R;

/*==============================================================================
 * create sequences
 * ApiCommN for 100000000, ApiCommS for 100000003
 *============================================================================*/

-- note start value may change depending on initial project list: see above
CREATE SEQUENCE uploads.userfile_pkseq INCREMENT BY 10 START WITH 100;
GRANT SELECT ON uploads.userfile_pkseq TO COMM_WDK_W;
