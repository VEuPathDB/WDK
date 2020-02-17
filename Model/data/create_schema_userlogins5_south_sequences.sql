/*==============================================================================
 * This SQL script should be run on SOUTH instances AFTER
 * create_schema_userlogins5.sql is run.  It will drop the sequences created by
 * that script and create new sequences with proper start values for south
 * instances.
 *============================================================================*/

-- drop sequences created by schema creation script
--   they have incorrect initial values for south instances
DROP SEQUENCE userlogins5.users_pkseq;
DROP SEQUENCE userlogins5.strategies_pkseq;
DROP SEQUENCE userlogins5.steps_pkseq;
DROP SEQUENCE userlogins5.datasets_pkseq;
DROP SEQUENCE userlogins5.dataset_values_pkseq;
DROP SEQUENCE userlogins5.user_baskets_pkseq;
DROP SEQUENCE userlogins5.favorites_pkseq;
DROP SEQUENCE userlogins5.categories_pkseq;
DROP SEQUENCE userlogins5.step_analysis_pkseq;

/* Save for api-specific script
DROP SEQUENCE userlogins5.commentStableId_pkseq;
DROP SEQUENCE userlogins5.commentTargetCategory_pkseq;
DROP SEQUENCE userlogins5.commentReference_pkseq;
DROP SEQUENCE userlogins5.commentFile_pkseq;
DROP SEQUENCE userlogins5.commentSequence_pkseq;
DROP SEQUENCE userlogins5.comments_pkseq;
DROP SEQUENCE userlogins5.locations_pkseq;
DROP SEQUENCE userlogins5.external_databases_pkseq;
*/

/*==============================================================================
 * recreate sequences with different ones digit in initial value
 * ApiCommN for 100000000, ApiCommS for 100000003
 *============================================================================*/

CREATE SEQUENCE userlogins5.user_baskets_pkseq INCREMENT BY 10 START WITH 100000003;
GRANT SELECT ON userlogins5.user_baskets_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.favorites_pkseq INCREMENT BY 10 START WITH 100000003;
GRANT SELECT ON userlogins5.favorites_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.datasets_pkseq INCREMENT BY 10 START WITH 100000003;
GRANT SELECT ON userlogins5.datasets_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.dataset_values_pkseq INCREMENT BY 10 START WITH 100000003;
GRANT SELECT ON userlogins5.dataset_values_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.strategies_pkseq INCREMENT BY 10 START WITH 100000003;
GRANT SELECT ON userlogins5.strategies_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.steps_pkseq INCREMENT BY 10 START WITH 100000003;
GRANT SELECT ON userlogins5.steps_pkseq TO COMM_WDK_W;

CREATE SEQUENCE userlogins5.step_analysis_pkseq INCREMENT BY 10 START WITH 100000003;
GRANT SELECT ON userlogins5.step_analysis_pkseq TO COMM_WDK_W;

--==============================================================================
exit
--==============================================================================

/* Save for api-specific script
CREATE SEQUENCE userlogins5.comments_pkseq START WITH 100000000 INCREMENT BY 10;
GRANT select on userlogins5.comments_pkseq to COMM_WDK_W;

CREATE SEQUENCE userlogins5.locations_pkseq START WITH 100000000 INCREMENT BY 10;
GRANT select on userlogins5.locations_pkseq to COMM_WDK_W;

CREATE SEQUENCE userlogins5.external_databases_pkseq START WITH 100000000 INCREMENT BY 10;
GRANT select on userlogins5.external_databases_pkseq to COMM_WDK_W;

CREATE SEQUENCE userlogins5.commentTargetCategory_pkseq START WITH 100000000 INCREMENT BY 10;
GRANT select on userlogins5.commentTargetCategory_pkseq to COMM_WDK_W;

CREATE SEQUENCE userlogins5.commentReference_pkseq START WITH 100000000 INCREMENT BY 10;
GRANT select on userlogins5.commentReference_pkseq to COMM_WDK_W;

CREATE SEQUENCE userlogins5.commentSequence_pkseq START WITH 100000000 INCREMENT BY 10;
GRANT select on userlogins5.commentSequence_pkseq to COMM_WDK_W;

CREATE SEQUENCE userlogins5.commentFile_pkseq START WITH 100000000 INCREMENT BY 10;
GRANT select on userlogins5.commentFile_pkseq to COMM_WDK_W;

CREATE SEQUENCE userlogins5.commentStableId_pkseq START WITH 100000000 INCREMENT BY 10;
GRANT select on userlogins5.commentStableId_pkseq to COMM_WDK_W;
*/
