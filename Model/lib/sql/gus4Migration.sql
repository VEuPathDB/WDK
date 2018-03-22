	------------------------------------------------------------------------------------
-- GUS4 MIGRATION SEP 2016   we migrate old invalid steps (only non-guest steps?)
-----------------------------------------------------------------------------------------
prompt **** display_params: replace "Pythium vexans," with "Phytopythium vexans,"
prompt **** display_params: replace "Leishmania sp.," with "unclassified Leishmania,"
prompt **** display_params: replace  "organism":   with   "organismSinglePick": in location queries and a few others CHECK WHIHC ONES
---------------------------------------------------------------------------------------
UPDATE userlogins5.steps
SET display_params=replace(display_params,'Pythium vexans,','Phytopythium vexans,')
WHERE  is_deleted = 0
  and display_params like '%Pythium vexans,%' 
  and (project_id = 'FungiDB' or project_id = 'EuPathDB');

UPDATE userlogins5.steps
SET display_params=replace(display_params,'Leishmania sp.,','unclassified Leishmania,')
WHERE  is_deleted = 0
  and display_params like '%Leishmania sp.,%' 
  and (project_id = 'TriTrypDB' or project_id = 'EuPathDB');

UPDATE userlogins5.steps
SET display_params=replace(display_params,'"organism":','"organismSinglePick":')
WHERE  question_name in ('GeneQuestions.GenesByLocation','EstQuestions.EstsByLocation','GeneQuestions.GenesByNgsSnps','GeneQuestions.GenesBySnps','GeneQuestions.GenesByTelomereProximity','OrfQuestions.OrfsByLocation','SnpChipQuestions.SnpsByGeneId','SnpChipQuestions.SnpsByIsolatePattern','SnpChipQuestions.SnpsByLocation','SnpChipQuestions.SnpsByStrain','SnpQuestions.NgsSnpsByGeneIds','SnpQuestions.NgsSnpsByIsolateGroup','SnpQuestions.NgsSnpsByLocation','SnpQuestions.NgsSnpsByTwoIsolateGroups','SpanQuestions.DynSpansBySourceId')
 and  display_params like '%"organism":%';

----------------------------------------------------------------------------------------------------------
prompt **** display_params: remove global_min_max parameter in gene questions
prompt **** display_params: remove type parameter in popset(still isolate)  questions
prompt **** display_params: remove ',Genes of previous release' in plasmo genetextsearch text_fields param
prompt **** display_params: remove ',Community annotation' in giardia,toxo  genetextsearch text_fields param
----------------------------------------------------------------------------------------------------------
UPDATE userlogins5.steps
SET display_params=replace(display_params,'"global_min_max":"Don''t care",','')
WHERE  display_params like '%"global_min_max":"Don%t care",%'
  and  question_name like 'GeneQuestions.%';

UPDATE userlogins5.steps
SET display_params=replace(display_params,'"type":"HTS,Sequencing Typed",','')
WHERE display_params like '%"type":"HTS,Sequencing Typed",%' 
  and (question_name like 'IsolateQuestions.%'   OR  question_name like 'PopsetQuestions.%') ;

UPDATE userlogins5.steps
SET display_params=replace(display_params,',Genes of previous release','')
WHERE  project_id = 'PlasmoDB' 
  and question_name = 'GeneQuestions.GenesByTextSearch'
  and display_params like '%,Genes of previous release%';

UPDATE userlogins5.steps
SET display_params=replace(display_params,',Community annotation','')
WHERE  (project_id = 'GiardiaDB' OR project_id = 'ToxoDB')
  and question_name = 'GeneQuestions.GenesByTextSearch'
  and display_params like '%,Community annotation%';

-----------------------------------------------------------------------------------------
prompt **** display_params: replace gene record with transcript record on a few questions
---------------------------------------------------------------------------------------
UPDATE userlogins5.steps
SET display_params=replace(display_params, 'GeneRecordClass', 'TranscriptRecordClass')
WHERE question_name in ('GeneQuestions.GenesBySimilarity', 'GenomicSequenceQuestions.SequencesBySimilarity', 'InternalQuestions.GeneRecordClasses_GeneRecordClassBySnapshotBasket', 'InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass', 'GeneQuestions.GeneBySingleLocusTag')
and display_params like '%GeneRecordClass%';

-----------------------------------------------------------------------
prompt **** display_params: replace isolate record with popset record 
-----------------------------------------------------------------------
UPDATE userlogins5.steps
SET display_params=replace(display_params, 'IsolateRecordClass', 'PopsetRecordClass')
WHERE display_params like '%IsolateRecordClass%';

------------------------------------------------------------------------
prompt **** question_name: booleans: replace gene record with transcript record
-----------------------------------------------------------------------
UPDATE userlogins5.steps
SET question_name='InternalQuestions.boolean_question_TranscriptRecordClasses_TranscriptRecordClass'
WHERE question_name = 'InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass';

------------------------------------------------------------------------
prompt **** question_name: booleans: replace isolate record with popset record 
-----------------------------------------------------------------------
UPDATE userlogins5.steps
SET question_name='InternalQuestions.boolean_question_PopsetRecordClasses_PopsetRecordClass'
WHERE question_name = 'InternalQuestions.boolean_question_IsolateRecordClasses_IsolateRecordClass';

------------------------------------------------------------------------
prompt **** question_name: basket steps: replace gene record with transcript record
-----------------------------------------------------------------------
UPDATE userlogins5.steps
SET question_name='InternalQuestions.TranscriptRecordClasses_TranscriptRecordClassBySnapshotBasket'
WHERE question_name = 'InternalQuestions.GeneRecordClasses_GeneRecordClassBySnapshotBasket';

------------------------------------------------------------------------
prompt **** question_name: basket steps: replace isolate record with popset record 
-----------------------------------------------------------------------
UPDATE userlogins5.steps
SET question_name='InternalQuestions.PopsetRecordClasses_PopsetRecordClassBySnapshotBasket'
WHERE question_name = 'InternalQuestions.IsolateRecordClasses_IsolateRecordClassBySnapshotBasket';

----------------------------------------------------------------------------------------------------
prompt **** user baskets: reassign primary key fields AND replace gene record with transcript record
---------------------------------------------------------------------------------------------------
UPDATE userlogins5.user_baskets
SET pk_column_3=pk_column_2, pk_column_2 = null,
record_class='TranscriptRecordClasses.TranscriptRecordClass'
WHERE record_class='GeneRecordClasses.GeneRecordClass';

------------------------------------------------------------------------
prompt **** user baskets: map isolate record into popset record
-----------------------------------------------------------------------
UPDATE userlogins5.user_baskets
SET record_class='PopsetRecordClasses.PopsetRecordClass'
WHERE record_class='IsolateRecordClasses.IsolateRecordClass';

------------------------------------------------------------------------
prompt **** favorites: map isolate record into popset record
-----------------------------------------------------------------------

UPDATE userlogins5.favorites
SET record_class='PopsetRecordClasses.PopsetRecordClass'
WHERE record_class='IsolateRecordClasses.IsolateRecordClass';

-----------------------------------------------------------------------------------------------------
prompt **** datasets generated for a basket step: convert geneId|ProjectId into geneId_____Project_id
-----------------------------------------------------------------------------------------------------
-- datasetParams values do not ever change.
-- In validation world a dataset param is a string param, the value is the dataset_id; we only validate that the dataset exists.
-- TODO: in projects other than Ortho, go through basket datasets with geneid|PlasmoDB in userlogins5.datasets.content clob.. And convert into the new delimiter: “______” (6 underscore)
-- we only need to worry of Content in datasets.
---------------------------------------

UPDATE userlogins5.datasets
SET content=replace(content, '|', '______')
WHERE dataset_id in 
( -- datasets in basket steps in projects other than Ortho
  select dataset_id
  from userlogins5.datasets 
  where dataset_id in
  (
    SELECT distinct(TO_CHAR (REGEXP_SUBSTR(quasi_dataset_id, '\d+') ) ) 
    FROM 
    (
      SELECT  REGEXP_SUBSTR(display_params, 'ClassDataset":"[^"]+') as   quasi_dataset_id
      FROM userlogins5.steps
      WHERE question_name like '%SnapshotBasket' 
        AND project_id != 'OrthoMCL' 
        AND is_deleted != 1
    )
  )
  and content like '%|%'
);

------------------------------------------------------------------------
prompt **** COMMITTING...
-----------------------------------------------------------------------
commit;


-- VALIDATION: all should return 0 (2 steps remain with Gene in display_params)
-- select count(*) from userlogins5.steps where display_params like '%Pythium vexans,%' and  is_deleted = 0 and (project_id = 'FungiDB' or project_id = 'EuPathDB')
-- select count(*) from userlogins5.steps where display_params like '%IsolateRecordClass%';
-- select count(*) from userlogins5.steps where display_params like '%GeneRecordClass%';  
--  select count(*) from userlogins5.steps WHERE question_name = 'InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass';
-- select count(*) from userlogins5.user_baskets where user_id = 376 and project_id = 'PlasmoDB' and record_class = 'GeneRecordClasses.GeneRecordClass';

