
-- drop tables in reverse order of creation to avoid FK violations
DROP TABLE userlogins5.step_analysis;
DROP TABLE userlogins5.strategies;
DROP TABLE userlogins5.steps;
DROP TABLE userlogins5.dataset_values;
DROP TABLE userlogins5.datasets;
DROP TABLE userlogins5.favorites;
DROP TABLE userlogins5.user_baskets;
DROP TABLE userlogins5.preferences;
DROP TABLE userlogins5.user_roles;
DROP TABLE userlogins5.users;
DROP TABLE userlogins5.config;

DROP SEQUENCE userlogins5.favorites_pkseq;
DROP SEQUENCE userlogins5.user_baskets_pkseq;
DROP SEQUENCE userlogins5.datasets_pkseq;
DROP SEQUENCE userlogins5.dataset_values_pkseq;
DROP SEQUENCE userlogins5.steps_pkseq;
DROP SEQUENCE userlogins5.strategies_pkseq;
DROP SEQUENCE userlogins5.step_analysis_pkseq;

DROP USER userlogins5 CASCADE;
