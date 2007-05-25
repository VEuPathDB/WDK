INSERT INTO userlogins.users 
    SELECT user_id, email, passwd, is_guest, signature, register_time, last_active timestamp, 
        last_name, first_name, middle_name, title, organization, department, address, city,
        state, zip_code, phone_number, country 
    FROM userlogins.users@plasmodb.LOGIN_COMMENT ou 
    WHERE ou.email NOT LIKE 'WDK_GUEST_%';

-- do not copy instance checksum, instead, use it as a flag for migration mark
INSERT INTO userlogins.histories
    (history_id, user_id, project_id, question_name, create_time, last_run_time, custom_name,
        estimate_size, query_signature, is_boolean, is_deleted, params)
    SELECT oh.history_id, oh.user_id, oh.project_id, oh.question_name, oh.create_time, 
        oh.last_run_time, oh.custom_name, oh.estimate_size, oh.signature, oh.is_boolean, 
        oh.is_deleted, oh.params
    FROM userlogins.histories@plasmodb.LOGIN_COMMENT oh,
        userlogins.users nu
    WHERE oh.user_id = nu.user_id;
    
    
-- alter the sequence
SELECT max(user_id)+1 AS var FROM userlogins.users
ALTER SEQUENCE userlogins.users_pkseq INCREMENT BY <var>;
SELECT userlogins.users_pkseq.nextval FROM dual;
ALTER SEQUENCE userlogins.users_pkseq INCREMENT BY 1;

