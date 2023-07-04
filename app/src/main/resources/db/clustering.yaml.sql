pst_insert_match_manu:
    INSERT INTO CR_MATCH_MANU  ( 
        CR_ID1, 
        CR_ID2, 
        sim,
        tstamp
    ) 
    VALUES (
        ?, ?, ?, ?
    )
    ON CONFLICT (CR_ID1, CR_ID2) 
    DO UPDATE SET 
        sim = EXCLUDED.sim, 
        tstamp = EXCLUDED.tstamp;


pst_insert_match_auto:
    INSERT INTO CR_MATCH_AUTO  ( 
        CR_ID1, 
        CR_ID2, 
        sim
    ) 
    VALUES (
        ?, ?, ?
    );

init:
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

refresh:
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


    UPDATE CR_Cluster
    SET CR_ClusterId2 = CR_ID %2$s;

    DROP TABLE IF EXISTS CR_Match;

    CREATE TEMPORARY TABLE CR_MATCH AS (
        SELECT CR_ID1, CR_ID2 FROM CR_MATCH_AUTO WHERE sim >= %1$.2f
        UNION 
        SELECT CR_ID1, CR_ID2 FROM CR_MATCH_MANU WHERE sim = 2
        EXCEPT 
        SELECT CR_ID1, CR_ID2 FROM CR_MATCH_MANU WHERE sim = -2
    );

update:
    WITH CRWithNewCluster AS (
        SELECT CR_ID, Clus1, Clus2 
        FROM ( 
            SELECT ROW_NUMBER() OVER (PARTITION BY CR_ID ORDER BY Clus1, Clus2) AS R, CR_ID, Clus1, Clus2
            FROM (
                SELECT 
                    CASE WHEN (CR1.CR_ClusterID1 < CR2.CR_ClusterId1) OR (CR1.CR_ClusterID1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterID2 < CR2.CR_ClusterId2) THEN CR2.CR_ID ELSE CR1.CR_ID END AS CR_ID,
                    CASE WHEN (CR1.CR_ClusterID1 < CR2.CR_ClusterId1) OR (CR1.CR_ClusterID1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterID2 < CR2.CR_ClusterId2) THEN CR1.CR_ClusterID1 ELSE CR2.CR_ClusterID1 END AS Clus1,
                    CASE WHEN (CR1.CR_ClusterID1 < CR2.CR_ClusterId1) OR (CR1.CR_ClusterID1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterID2 < CR2.CR_ClusterId2) THEN CR1.CR_ClusterID2 ELSE CR2.CR_ClusterID2 END AS Clus2
                FROM CR_Cluster AS CR1 
                JOIN CR_MATCH ON (CR1.CR_ID = CR_MATCH.CR_ID1)
                JOIN CR_Cluster AS CR2 ON (CR2.CR_ID = CR_MATCH.CR_ID2)
                WHERE ((CR1.CR_ClusterID1 != CR2.CR_ClusterId1) OR (CR1.CR_ClusterID2 != CR2.CR_ClusterId2)) %1$s
            ) AS X
        ) AS Y
        WHERE R = 1
    )
    UPDATE CR_Cluster
    SET CR_ClusterId1 = CRWithNewCluster.Clus1, CR_ClusterId2 = CRWithNewCluster.Clus2
    FROM CRWithNewCluster
    WHERE CR_Cluster.CR_ID = CRWithNewCluster.CR_ID;

finish:
    CREATE INDEX cr_cluster_index ON cr_cluster(cr_clusterid1);

    WITH ClusterInfo AS (
        select c.cr_id, c.cr_clusterid1, c.cr_clusterid2, t.size
        from cr_cluster c
        join (
            select cr_clusterid1, cr_clusterid2, count(*) as size
            from cr_cluster
            group by cr_clusterid1, cr_clusterid2
        ) as t on (c.cr_clusterid1 = t.cr_clusterid1 and c.cr_clusterid2 = t.cr_clusterid2)
    )
    UPDATE CR
    SET 
        cr_clusterid1 = ClusterInfo.cr_clusterid1, 
        cr_clusterid2 = ClusterInfo.cr_clusterid2, 
        cr_clustersize = size
    FROM ClusterInfo
    WHERE CR.CR_ID = ClusterInfo.CR_ID;

