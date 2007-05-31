-- delete rows
DELETE FROM userlogins2.SUMMARY_ATTRIBUTES;
DELETE FROM userlogins2.CLOB_VALUES;
DELETE FROM userlogins2.USER_DATASETS;
DELETE FROM userlogins2.DATASET_VALUES;
DELETE FROM userlogins2.DATASET_INDICES;
DELETE FROM userlogins2.HISTORIES;
DELETE FROM userlogins2.PREFERENCES;
DELETE FROM userlogins2.SORTING_ATTRIBUTES;
DELETE FROM userlogins2.USER_ROLES;
DELETE FROM userlogins2.USERS;


INSERT INTO userlogins2.users 
    SELECT user_id, email, passwd, is_guest, signature, register_time, last_active timestamp, 
        last_name, first_name, middle_name, title, organization, department, address, city,
        state, zip_code, phone_number, country 
    FROM userlogins.users ou 
    WHERE ou.email NOT LIKE 'WDK_GUEST_%';


-- do not copy instance checksum, instead, use it as a flag for migration mark
INSERT INTO userlogins2.histories
    (history_id, user_id, project_id, question_name, create_time, last_run_time, custom_name,
        estimate_size, query_signature, is_boolean, is_deleted, params)
    SELECT oh.history_id, oh.user_id, oh.project_id, oh.question_name, oh.create_time, 
        oh.last_run_time, oh.custom_name, oh.estimate_size, oh.signature, oh.is_boolean, 
        oh.is_deleted, oh.params
    FROM userlogins.histories oh, userlogins2.users nu
    WHERE oh.user_id = nu.user_id;


-- copy other tables
INSERT INTO userlogins2.user_roles
    SELECT our.user_id, our.user_role
    FROM userlogins.user_roles our, userlogins2.users nu
    WHERE our.user_id = nu.user_id;


INSERT INTO userlogins2.preferences
    SELECT op.user_id, op.project_id, op.preference_name, op.preference_value
    FROM userlogins.preferences op, userlogins2.users nu
    WHERE op.user_id = nu.user_id;
      
  
-- alter the sequence
SELECT max(user_id)+1 AS var FROM userlogins2.users
ALTER SEQUENCE userlogins2.users_pkseq INCREMENT BY <var>;
SELECT userlogins2.users_pkseq.nextval FROM dual;
ALTER SEQUENCE userlogins2.users_pkseq INCREMENT BY 1;

