create_schema:

  -- creates entire db schema for CRE

  DROP TABLE IF EXISTS CR;

  CREATE TABLE CR ( 
    CR_ID 	int, 
    CR_CR 	varchar , 
    CR_RPY 	int, 
    CR_N_CR int, 
    CR_AU 	varchar, 
    CR_AU_L varchar, 
    CR_AU_F varchar, 
    CR_AU_A varchar, 
    CR_TI 	varchar, 
    CR_J 	varchar, 
    CR_J_N 	varchar, 
    CR_J_S 	varchar, 
    CR_VOL 	varchar, 
    CR_PAG 	varchar, 
    CR_DOI 	varchar, 
    CR_ClusterId1 int, 
    CR_ClusterId2 int,  
    CR_ClusterSize int,  
    CR_VI boolean, 
    CR_SORT_ORDER int, 
    CR_Format varchar, 
    
    CR_PERC_YR double precision, 
    CR_PERC_ALL double precision, 
    CR_N_PYEARS int,	
    CR_PYEAR_PERC double precision,
    CR_N_PCT_P50 int,
    CR_N_PCT_P75 int,
    CR_N_PCT_P90 int,
    CR_N_PCT_P99 int,
    CR_N_PCT_P999 int,
    CR_N_PCT_AboveAverage_P50 int,
    CR_N_PCT_AboveAverage_P75 int,
    CR_N_PCT_AboveAverage_P90 int,
    CR_N_PCT_AboveAverage_P99 int,
    CR_N_PCT_AboveAverage_P999 int,
    CR_SEQUENCE varchar,
    CR_TYPE varchar,
    CR_BLOCKINGKEY varchar,
    CR_MERGED_INTO int, 
    
    PRIMARY KEY (CR_ID)  
  );

  /* we use a temp table for efficient import */

  DROP TABLE IF EXISTS CR_Temp;

  CREATE TABLE CR_Temp ( 
    CR_ID 	int, 
    CR_CR 	varchar , 
    CR_RPY 	int, 
    CR_AU 	varchar, 
    CR_AU_L varchar, 
    CR_AU_F varchar, 
    CR_AU_A varchar, 
    CR_TI 	varchar, 
    CR_J 	varchar, 
    CR_J_N 	varchar, 
    CR_J_S 	varchar, 
    CR_VOL 	varchar, 
    CR_PAG 	varchar, 
    CR_DOI 	varchar, 
    CR_ClusterId1 int, 
    CR_ClusterId2 int,  
    CR_ClusterSize int, 
    CR_VI boolean,	
    CR_Format varchar,
    PUB_ID int, 
    PRIMARY KEY (CR_ID)  
  );

  DROP TABLE IF EXISTS PUB;

  CREATE TABLE PUB ( 
    PUB_ID int, 
    PUB_PT varchar, 
    PUB_AU varchar,  
    PUB_AF varchar,
    PUB_C1 varchar,
    PUB_EM varchar,
    PUB_AA varchar,
    PUB_TI varchar,
    PUB_PY int, 
    PUB_SO varchar,
    PUB_VL varchar,
    PUB_IS varchar,
    PUB_AR varchar,
    PUB_BP int, 
    PUB_EP int, 
    PUB_PG int, 
    PUB_TC int, 
    PUB_DI varchar,
    PUB_LI varchar,
    PUB_AB varchar,
    PUB_DE varchar,
    PUB_DT varchar,
    PUB_FS varchar,
    PUB_UT varchar,
    PRIMARY KEY (PUB_ID)  
  );

  /* CREATE UNIQUE INDEX CRSTRING ON CR(CR_CR); */  

  DROP TABLE IF EXISTS PUB_CR;

  CREATE TABLE PUB_CR ( 
    PUB_ID	int, 
    CR_ID 	int, 
    PRIMARY KEY (PUB_ID, CR_ID)  
  );

  DROP TABLE IF EXISTS CR_MATCH_AUTO;

  CREATE TABLE CR_MATCH_AUTO ( 
    CR_ID1 	int, 
    CR_ID2 	int, 
    sim double precision, 
    PRIMARY KEY (CR_ID1, CR_ID2)  
  );

  DROP TABLE IF EXISTS CR_MATCH_MANU;

  CREATE TABLE CR_MATCH_MANU ( 
    CR_ID1 	int, 
    CR_ID2 	int, 
    sim 	double precision, 
    tstamp 	bigint, 
    PRIMARY KEY (CR_ID1, CR_ID2)  
  );  

merge_cr: 

  -- merged_into = CR of the cluster with the highest number of citations
  WITH CRWITHMERGE AS (
    SELECT CR_ID, FIRST_VALUE(CR_ID) OVER (PARTITION BY CR_ClusterId1, CR_ClusterId2 ORDER BY CR_N_CR DESC, CR_ID) AS V
    FROM CR
  )
  UPDATE CR
  SET CR_MERGED_INTO = V
  FROM CRWITHMERGE
  WHERE CR.CR_ID = CRWITHMERGE.CR_ID;

  -- add new pub_id/cr_id pairs
  INSERT INTO PUB_CR
  (PUB_ID, CR_ID)
  SELECT DISTINCT PUB_ID, CR_MERGED_INTO
  FROM PUB_CR JOIN CR ON (PUB_CR.CR_ID = CR.CR_ID)
  EXCEPT
  SELECT PUB_ID, CR_ID
  FROM PUB_CR;

  -- remove stale pub_id/cr_id pars
  DELETE
  FROM PUB_CR
  WHERE CR_ID NOT IN (SELECT CR_MERGED_INTO FROM CR);

  -- remove CRs that are merged into other CR
  DELETE 
  FROM CR
  WHERE CR_ID != CR_MERGED_INTO;

  -- update remainings CRs (sum of N_CR; re-init Clustering)
  WITH CRClusterInit AS (
    SELECT CR_ID, COUNT(*) AS N 
    FROM PUB_CR
    GROUP BY CR_ID
  ) 
  UPDATE CR
  SET 
    CR_N_CR = CRClusterInit.N, 
    CR_ClusterId1 = CRClusterInit.CR_ID, 
    CR_ClusterId2 = CRClusterInit.CR_ID, 
    CR_ClusterSize = 1, 
    CR_MERGED_INTO = NULL
  FROM CRClusterInit
  WHERE CR.CR_ID = CRClusterInit.CR_ID;

  -- remove match results
  TRUNCATE TABLE CR_MATCH_AUTO;
  TRUNCATE TABLE CR_MATCH_MANU;

pst_insert_cr:

  INSERT INTO CR_Temp  ( 
    CR_ID, 
    CR_CR, 
    CR_RPY, 
    CR_AU, 
    CR_AU_L, 
    CR_AU_F, 
    CR_AU_A, 
    CR_TI, 
    CR_J, 
    CR_J_N, 
    CR_J_S, 
    CR_VOL, 
    CR_PAG, 
    CR_DOI, 
    CR_ClusterId1, 
    CR_ClusterId2,
    CR_ClusterSize,
    CR_VI,	
    CR_Format,
    PUB_ID
  ) 
  VALUES (
    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
  )

pst_insert_pub:

  INSERT INTO Pub  ( 
    PUB_ID, 
    PUB_PT, 
    PUB_AU, 
    PUB_AF, 
    PUB_C1, 
    PUB_EM, 
    PUB_AA, 
    PUB_TI, 
    PUB_PY, 
    PUB_SO, 
    PUB_VL, 
    PUB_IS, 
    PUB_AR, 
    PUB_BP, 
    PUB_EP, 
    PUB_PG, 
    PUB_TC, 
    PUB_DI, 
    PUB_LI, 
    PUB_AB, 
    PUB_DE, 
    PUB_DT, 
    PUB_FS, 
    PUB_UT
  ) 
  VALUES (
    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
  );

pst_update_cr_indicators:

  UPDATE CR 
  SET
    CR_N_PYEARS = ?,	
    CR_PYEAR_PERC = ?,
    CR_PERC_YR = ?, 
    CR_PERC_ALL = ?, 
    CR_N_PCT_P50 = ?,
    CR_N_PCT_P75 = ?,
    CR_N_PCT_P90 = ?,
    CR_N_PCT_P99 = ?,
    CR_N_PCT_P999 = ?,
    CR_N_PCT_AboveAverage_P50 = ?,
    CR_N_PCT_AboveAverage_P75 = ?,
    CR_N_PCT_AboveAverage_P90 = ?,
    CR_N_PCT_AboveAverage_P99 = ?,
    CR_N_PCT_AboveAverage_P999 = ?,
    CR_SEQUENCE = ?,
    CR_TYPE = ?
  WHERE
    CR_ID = ? 

wrapup_insert:

  INSERT INTO PUB_CR (CR_ID, PUB_ID)
  SELECT DISTINCT first_value (CR_ID) over (partition by CR_CR order by CR_ID), PUB_ID
  FROM CR_Temp;

  INSERT INTO CR 
  (       CR_ID, CR_CR, CR_RPY, CR_N_CR, CR_AU, CR_AU_L, CR_AU_F, CR_AU_A, CR_TI, CR_J, CR_J_N, CR_J_S, CR_VOL, CR_PAG, CR_DOI, 
          CR_ClusterId1, CR_ClusterId2, CR_ClusterSize, CR_VI, CR_Format)
  SELECT CR_Temp.CR_ID, CR_CR, CR_RPY, T.PUBCOUNT, CR_AU, CR_AU_L, CR_AU_F, CR_AU_A, CR_TI, CR_J, CR_J_N, CR_J_S, CR_VOL, CR_PAG, CR_DOI, 
        CR_ClusterId1, CR_ClusterId2, CR_ClusterSize, CR_VI, CR_Format
  FROM CR_Temp
  JOIN (
    SELECT CR_ID, COUNT(*) AS PUBCOUNT
    FROM PUB_CR
    GROUP BY CR_ID
  ) AS T
  ON (CR_Temp.CR_ID = T.CR_ID);

  TRUNCATE TABLE CR_Temp;
