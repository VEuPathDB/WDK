RESET ROLE;

CREATE SCHEMA IF NOT EXISTS UPLOADS AUTHORIZATION userdb_owner;
GRANT USAGE ON SCHEMA UPLOADS TO uploads_r;

CREATE TABLE UPLOADS.USERFILE (
    USERFILEID     BIGINT       NOT NULL,
    FILENAME       VARCHAR(255) NOT NULL,
    PATH           VARCHAR(255),
    CHECKSUM       VARCHAR(64),
    FILESIZE       INTEGER,
    FORMAT         VARCHAR(255),
    ISDIR          BOOLEAN,
    UPLOADTIME     TIMESTAMP    NOT NULL,
    OWNERUSERID    VARCHAR(40)  NOT NULL,
    EMAIL          VARCHAR(255),
    TITLE          VARCHAR(4000),
    NOTES          VARCHAR(4000),
    PROJECTNAME    VARCHAR(200),
    PROJECTVERSION VARCHAR(100),
    IS_VISIBLE     BOOLEAN DEFAULT TRUE,
    CONSTRAINT FIELDID_PKEY PRIMARY KEY (USERFILEID)
);

ALTER TABLE UPLOADS.USERFILE OWNER TO userdb_owner;
GRANT SELECT ON uploads.userfile TO uploads_r;
GRANT INSERT, UPDATE, DELETE ON uploads.userfile TO uploads_w;

/*==============================================================================
 * create sequences
 * ApiCommN for 100000000, ApiCommS for 100000003
 *============================================================================*/

-- note start value may change depending on initial project list: see above
CREATE SEQUENCE uploads.userfile_pkseq INCREMENT BY 10 START WITH 100;
ALTER SEQUENCE uploads.userfile_pkseq OWNER TO userdb_owner;
-- GRANT SELECT ON uploads.userfile_pkseq TO COMM_WDK_W;
GRANT USAGE, SELECT ON uploads.userfile_pkseq TO uploads_r;
