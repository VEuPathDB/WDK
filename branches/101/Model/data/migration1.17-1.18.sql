/* delete test data */

DELETE FROM userlogins3.histories;
DELETE FROM userlogins3.preferences;
DELETE FROM userlogins3.user_datasets;
DELETE FROM userlogins3.user_roles;
DELETE FROM userlogins3.users;

DELETE FROM wdkstorage.dataset_values;
DELETE FROM wdkstorage.dataset_indices;
DELETE FROM wdkstorage.clob_values;
DELETE FROM wdkstorage.answer;

DROP SEQUENCE wdkstorage.dataset_indices_pkseq;
DROP SEQUENCE wdkstorage.answer_pkseq;
DROP SEQUENCE userlogins3.users_pkseq;

CREATE SEQUENCE wdkstorage.dataset_indices_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON wdkstorage.dataset_indices_pkseq TO GUS_W;
GRANT select ON wdkstorage.dataset_indices_pkseq TO GUS_R;


CREATE SEQUENCE wdkstorage.answer_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON wdkstorage.answer_pkseq TO GUS_W;
GRANT select ON wdkstorage.answer_pkseq TO GUS_R;


CREATE SEQUENCE userlogins3.users_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON userlogins3.users_pkseq TO GUS_W;
GRANT select ON userlogins3.users_pkseq TO GUS_R;


/* copy user information */
INSERT INTO userlogins3.users
  (user_id, email, passwd, is_guest, signature, register_time, last_active,
   last_name, first_name, middle_name, title, organization, department, address,
   city, state, zip_code, phone_number, country, prev_user_id)
  (SELECT (userlogins3.users_pkseq.NEXTVAL) AS user_id,
          email, passwd, is_guest, signature, register_time, last_active,
          last_name, first_name, middle_name, title, organization, department, 
          address, city, state, zip_code, phone_number, country,
          user_id AS prev_user_id
   FROM userlogins2.users
   WHERE is_guest = 0);
   

/* copy user roles */
INSERT INTO userlogins3.user_roles (user_id, user_role)
  (SELECT DISTINCT u3.user_id, ur2.user_role
   FROM userlogins3.users u3, userlogins2.user_roles ur2
   WHERE u3.prev_user_id = ur2.user_id);
   
/* copy preferences */
INSERT INTO userlogins3.preferences (user_id, project_id, preference_name, preference_value)
  (SELECT DISTINCT u3.user_id, p2.project_id, p2.preference_name, p2.preference_value
   FROM userlogins3.users u3, userlogins2.preferences p2
   WHERE u3.prev_user_id = p2.user_id);
   
/* copy dataset indices */
INSERT INTO wdkstorage.dataset_indices 
  (dataset_id, dataset_checksum, summary, dataset_size, prev_dataset_id)
  (SELECT (wdkstorage.dataset_indices_pkseq.NEXTVAL) AS dataset_id, f.* 
   FROM (SELECT DISTINCT di2.dataset_checksum, di2.summary, di2.dataset_size, 
                di2.dataset_id AS prev_dataset_id
         FROM userlogins2.dataset_indices di2, 
              userlogins2.dataset_values dv2,
              userlogins2.user_datasets ud2,
              userlogins3.users u3
         WHERE di2.dataset_id = dv2.dataset_id
           AND di2.dataset_id = ud2.dataset_id
           AND ud2.user_id = u3.prev_user_id) f);
   
/* copy dataset values */
INSERT INTO wdkstorage.dataset_values (dataset_id, dataset_value) 
  (SELECT DISTINCT di3.dataset_id, dv2.dataset_value
   FROM wdkstorage.dataset_indices di3, userlogins2.dataset_values dv2
   WHERE di3.prev_dataset_id = dv2.dataset_id);

   
/* copy user datasets */
INSERT INTO userlogins3.user_datasets (dataset_id, user_id, create_time, upload_file)
  (SELECT di3.dataset_id, u3.user_id, ud2.create_time, ud2.upload_file
   FROM wdkstorage.dataset_indices di3, 
        userlogins3.users u3, 
        userlogins2.user_datasets ud2
   WHERE di3.prev_dataset_id = ud2.dataset_id
     AND u3.prev_user_id = ud2.user_id);


/* copy clob values */
INSERT INTO wdkstorage.clob_values (clob_checksum, clob_value)
  (SELECT clob_checksum, clob_value FROM userlogins2.clob_values);
  
INSERT INTO wdkstorage.clob_values (clob_checksum, clob_value)
  (SELECT sorting_checksum AS clob_checksum, attributes ASclob_value 
   FROM userlogins2.sorting_attributes
   WHERE sorting_checksum NOT IN (SELECT clob_checksum FROM wdkstorage.clob_values));
  
INSERT INTO wdkstorage.clob_values (clob_checksum, clob_value)
  (SELECT summary_checksum AS clob_checksum, attributes ASclob_value 
   FROM userlogins2.summary_attributes
   WHERE summary_checksum NOT IN (SELECT clob_checksum FROM wdkstorage.clob_values));

COMMIT;
