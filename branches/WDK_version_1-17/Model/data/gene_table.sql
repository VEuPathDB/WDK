DROP TABLE apidb.GeneTable;

CREATE TABLE apidb.GeneTable
(
  project_id VARCHAR(50) NOT NULL,
  source_id VARCHAR(50) NOT NULL,
  table_name VARCHAR(80) NOT NULL,
  row_count NUMBER(4) NOT NULL,
  content CLOB,
  CONSTRAINT "GENETABLES_PK" PRIMARY KEY (project_id, source_id, table_name)
);

CREATE INDEX GENETABLES_ROW_COUNT_IDX ON apidb.GeneTable (row_count);

GRANT insert, update, delete on apidb.GeneTable to GUS_W;
GRANT select on apidb.GeneTable to GUS_R;
