/*==============================================================================
 * This SQL script should be run on SOUTH instances AFTER
 * create_schema_announce.sql is run.  It will drop the sequences created by
 * that script and create new sequences with proper start values for south
 * instances.
 *============================================================================*/

-- drop sequences created by schema creation script
--   they have incorrect initial values for south instances
DROP SEQUENCE announce.projects_id_pkseq;
DROP SEQUENCE announce.category_id_pkseq;
DROP SEQUENCE announce.messages_id_pkseq;

/*==============================================================================
 * create sequences
 * ApiCommN for 100000000, ApiCommS for 100000003
 *============================================================================*/

-- note start value may change depending on initial project list
--   See create_schema_announce.sql
CREATE SEQUENCE announce.projects_id_pkseq INCREMENT BY 10 START WITH 43;
GRANT SELECT ON announce.projects_id_pkseq TO COMM_WDK_W;

-- note start value may change depending on initial project list
--   See create_schema_announce.sql
CREATE SEQUENCE announce.category_id_pkseq INCREMENT BY 10 START WITH 13;
GRANT SELECT ON announce.category_id_pkseq TO COMM_WDK_W;

CREATE SEQUENCE announce.messages_id_pkseq INCREMENT BY 10 START WITH 13;
GRANT SELECT ON announce.messages_id_pkseq TO COMM_WDK_W;