MERGE INTO CR (CR_ID, CR_ClusterId1, CR_ClusterId2)

    SELECT CR_ID, Clus1, Clus2 
    FROM (
        SELECT ROW_NUMBER() OVER (PARTITION BY CR_ID ORDER BY Clus1, Clus2) AS R,  CR_ID, Clus1, Clus2
        FROM (
            SELECT CR1.CR_ID, 
                CASE WHEN (CR1.CR_ClusterID1 < CR2.CR_ClusterId1) OR (CR1.CR_ClusterID1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterID2 < CR2.CR_ClusterId2) THEN CR1.CR_ClusterID1 ELSE CR2.CR_ClusterID1 END AS Clus1, 
                CASE WHEN (CR1.CR_ClusterID1 < CR2.CR_ClusterId1) OR (CR1.CR_ClusterID1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterID2 < CR2.CR_ClusterId2) THEN CR1.CR_ClusterID2 ELSE CR2.CR_ClusterID2 END AS Clus2 
            FROM CR AS CR1 
            JOIN
                (
                SELECT CR_ID1, CR_ID2 FROM CR_MATCH_AUTO WHERE sim >= %1$.2f
                UNION 
                SELECT CR_ID1, CR_ID2 FROM CR_MATCH_MANU WHERE sim = 2
                EXCEPT 
                SELECT CR_ID1, CR_ID2 FROM CR_MATCH_MANU WHERE sim = -2
                ) AS MATCH 
            ON (CR1.CR_ID = MATCH.CR_ID1)
            JOIN CR AS CR2 
            ON (CR2.CR_ID = MATCH.CR_ID2)
            WHERE ((CR1.CR_ClusterID1 != CR2.CR_ClusterId1) OR (CR1.CR_ClusterID2 != CR2.CR_ClusterId2))
            %2$s

            UNION

            SELECT CR2.CR_ID, 
                CASE WHEN (CR1.CR_ClusterID1 < CR2.CR_ClusterId1) OR (CR1.CR_ClusterID1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterID2 < CR2.CR_ClusterId2) THEN CR1.CR_ClusterID1 ELSE CR2.CR_ClusterID1 END AS Clus1, 
                CASE WHEN (CR1.CR_ClusterID1 < CR2.CR_ClusterId1) OR (CR1.CR_ClusterID1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterID2 < CR2.CR_ClusterId2) THEN CR1.CR_ClusterID2 ELSE CR2.CR_ClusterID2 END AS Clus2 
            FROM CR AS CR1 
            JOIN
            (
            SELECT CR_ID1, CR_ID2 FROM CR_MATCH_AUTO WHERE sim >= %1$.2f
            UNION 
            SELECT CR_ID1, CR_ID2 FROM CR_MATCH_MANU WHERE sim = 2
            EXCEPT 
            SELECT CR_ID1, CR_ID2 FROM CR_MATCH_MANU WHERE sim = -2
            ) AS MATCH 
            ON (CR1.CR_ID = MATCH.CR_ID1)
            JOIN CR AS CR2 
            ON (CR2.CR_ID = MATCH.CR_ID2)
            WHERE ((CR1.CR_ClusterID1 != CR2.CR_ClusterId1) OR (CR1.CR_ClusterID2 != CR2.CR_ClusterId2))
            %2$s

        ) AS X

    ) AS Y
    WHERE R = 1