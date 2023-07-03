CREATE INDEX cr_cluster_index ON cr_cluster(cr_clusterid1);

-- H2 
/*
merge into cr (cr_id, cr_clusterid1, cr_clusterid2, cr_clustersize)
select c.cr_id, c.cr_clusterid1, c.cr_clusterid2, t.size
from cr_cluster c
join (
    select cr_clusterid1, cr_clusterid2, count(*) as size
    from cr_cluster
    group by cr_clusterid1, cr_clusterid2
) as t on (c.cr_clusterid1 = t.cr_clusterid1 and c.cr_clusterid2 = t.cr_clusterid2)
*/


-- POstgreSQL

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
SET cr_clusterid1 = ClusterInfo.cr_clusterid1, cr_clusterid2 = ClusterInfo.cr_clusterid2, cr_clustersize = size
FROM ClusterInfo
WHERE CR.CR_ID = ClusterInfo.CR_ID
