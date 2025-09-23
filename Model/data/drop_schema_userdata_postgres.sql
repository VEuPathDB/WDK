
-- drop tables in reverse order of creation to avoid FK violations
DROP TABLE userdata.step_analysis;
DROP TABLE userdata.strategies;
DROP TABLE userdata.steps;
DROP TABLE userdata.dataset_values;
DROP TABLE userdata.datasets;
DROP TABLE userdata.favorites;
DROP TABLE userdata.user_baskets;
DROP TABLE userdata.preferences;
DROP TABLE userdata.users;

DROP SEQUENCE userdata.favorites_pkseq;
DROP SEQUENCE userdata.user_baskets_pkseq;
DROP SEQUENCE userdata.datasets_pkseq;
DROP SEQUENCE userdata.dataset_values_pkseq;
DROP SEQUENCE userdata.steps_pkseq;
DROP SEQUENCE userdata.strategies_pkseq;
DROP SEQUENCE userdata.step_analysis_pkseq;

DROP USER userdata CASCADE;
