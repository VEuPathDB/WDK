/*
Block comments are started as above and ended as below.  The delimiters must 
be on their own line.
*/

// Single line comments are done like this.  The comment may not start in the middle of a line

/*
SELECT sequence as translation
FROM dots.translatedaasequence tas,
     dots.translatedaafeature taf,
     dots.transcript t
WHERE taf.aa_sequence_id = tas.aa_sequence_id
AND taf.na_feature_id = t.na_feature_id
AND UPPER(t.source_id) = UPPER('$$primaryKey$$')
*/

SELECT X, Y, Z
FROM ( 
  SELECT A 
  FROM B 
  WHERE C = D 
) t
WHERE t.E = $$primaryKey$$

SELECT name, rna_count FROM (SELECT testgene.gene_id, TestGene.name, count(*) as rna_count FROM TestGene, TestRna WHERE TestGene.gene_id = $$primaryKey$$ AND TestGene.gene_id = TestRna.gene_id GROUP BY TestGene.gene_id, TestGene.name) AS whatever

SELECT A, 'select ''from''' FROM B WHERE X = 'from' and B = '$$primaryKey$$'
	
SELECT A, count(X), 'select ''from''' FROM (SELECT B FROM C WHERE D), E WHERE X = 'from' and F = '$$primaryKey$$'

selECT A FROM B WHERE X = 2 and B = $$primaryKey$$

SELECT cheese AS fromage FROM B WHERE C = $$primaryKey$$

SELECT A, ')' FROM B WHERE X = 2 and B = $$primaryKey$$
       
SELECT A FROM (SELECT B FROM C WHERE $$primaryKey$$ = D)

(SELECT A FROM (SELECT B FROM C WHERE $$primaryKey$$ = D))

SELECT A FROM (SELECT B FROM C WHERE D) WHERE $$primaryKey$$ = E

SELECT A FROM B WHERE C IN (SELECT D FROM E) AND $$primaryKey$$ = E

(SELECT A FROM B WHERE C IN (SELECT D FROM E) AND $$primaryKey$$ = E)

(select A from B where C = $$primaryKey$$) union (select A from D where E = $$primaryKey$$)

SELECT SUBSTR(g.source_id, 1, 1) FROM dots.genefeature g WHERE  g.source_id = '$$primaryKey$$' 

