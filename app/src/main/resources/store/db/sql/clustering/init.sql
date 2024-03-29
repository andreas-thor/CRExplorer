/* -- H2 

DROP TABLE IF EXISTS CR_Cluster;

CREATE TEMPORARY TABLE CR_Cluster (
    CR_ID INT,
    CR_ClusterId1 INT, 
    CR_ClusterId2 INT,
	CR_VOL 	varchar, 
	CR_PAG 	varchar, 
	CR_DOI 	varchar,     
    PRIMARY KEY (CR_ID)  
) AS (
    SELECT CR_ID, CR_ID AS CR_ClusterId1, CR_ID AS CR_ClusterId2, CR_VOL, CR_PAG, CR_DOI
    FROM CR
);
*/

DROP TABLE IF EXISTS CR_Cluster;

CREATE TEMPORARY TABLE CR_Cluster (
    CR_ID INT,
    CR_ClusterId1 INT, 
    CR_ClusterId2 INT,
	CR_VOL 	varchar, 
	CR_PAG 	varchar, 
	CR_DOI 	varchar,     
    PRIMARY KEY (CR_ID)  
); 

INSERT INTO CR_Cluster (CR_ID, CR_ClusterId1, CR_ClusterId2, CR_VOL, CR_PAG, CR_DOI)
SELECT CR_ID, CR_ID AS CR_ClusterId1, CR_ID AS CR_ClusterId2, CR_VOL, CR_PAG, CR_DOI
FROM CR;


DROP TABLE IF EXISTS CR_Match;

CREATE TEMPORARY TABLE CR_MATCH AS (
    SELECT CR_ID1, CR_ID2 FROM CR_MATCH_AUTO WHERE sim >= %1$.2f
    UNION 
    SELECT CR_ID1, CR_ID2 FROM CR_MATCH_MANU WHERE sim = 2
    EXCEPT 
    SELECT CR_ID1, CR_ID2 FROM CR_MATCH_MANU WHERE sim = -2
);

