
-- drop tables in reverse order of creation to avoid FK violations
DROP TABLE announce.message_projects;
DROP TABLE announce.messages;
DROP TABLE announce.category;
DROP TABLE announce.projects;

DROP SEQUENCE announce.projects_id_pkseq;
DROP SEQUENCE announce.category_id_pkseq;
DROP SEQUENCE announce.messages_id_pkseq;

DROP USER announce CASCADE;
