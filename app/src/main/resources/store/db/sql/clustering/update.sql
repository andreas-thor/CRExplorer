MERGE INTO CR_Cluster (CR_ID, CR_ClusterId1, CR_ClusterId2)
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
WHERE R = 1;