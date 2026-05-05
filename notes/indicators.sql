-- double expected = (1.0d*NCR_CR[crIdx]*NCR_PY[pyIdx]/NCR[0]);



-- alle Ziterungen werden gezählt bei N_CR, aber PUB_PY nur betrachtet, wenn nicht vor CR_RPY
WITH PubYear AS (
    SELECT 
        CR.CR_ID, 
        CR.CR_RPY AS CR_RPY, 
        (CASE WHEN CR_RPY > PUB_PY THEN NULL ELSE PUB_PY END) AS PUB_PY, 
        COUNT(*) AS N_CR,
        COUNT (CASE WHEN CR_RPY > PUB_PY THEN NULL ELSE PUB_PY END) AS N_CR_VALID 
    FROM CR 
    JOIN PUB_CR ON (CR.CR_ID = PUB_CR.CR_ID)
    JOIN PUB ON (PUB_CR.PUB_ID = PUB.PUB_ID)
    GROUP BY CR.CR_ID, CR.CR_RPY, PUB.PUB_PY
),
-- Anzahl der PUB_PYs pro CR_RPY, d.h. in wie vielen Jahren (pub_py) wurden CRs auf dem Jahr cr_rpy zitiert
CountPY AS (
    SELECT 
        CR_RPY,
        COUNT (DISTINCT PUB_PY) AS CNT_PY,
        SUM (N_CR_VALID) AS N_CR_RPY
    FROM PubYear
    GROUP BY CR_RPY
)
,
-- Berechnung der PYears inkl. prozentualem Anteil 
CR_Indicators1 AS (
    SELECT 
        CR_ID, 
        PubYear.CR_RPY, 
        SUM(N_CR) AS N_CR, 
        COUNT (PUB_PY) AS N_PYEARS, 
        COALESCE (1.0*COUNT (PUB_PY) /( CountPY.CNT_PY), 0) AS PERC_PYEARS,
        COALESCE (1.0*SUM(N_CR_VALID) / CountPY.N_CR_RPY, 0) AS PERC_YR,
        COALESCE (1.0*SUM(N_CR_VALID) / (SELECT SUM (N_CR_VALID) FROM PubYear), 0) AS PERC_ALL
    FROM PubYear 
    LEFT OUTER JOIN CountPY USING (CR_RPY)
    GROUP BY CR_ID
)
SELECT * FROM CR_Indicators1

,
-- Ergänzung um PY-Jahre, in denen keine Zitierung vorkam mit N_CR = 0
PubYearComplete AS (
    SELECT C.CR_ID, C.CR_RPY, S.PUB_PY, COALESCE (N_CR_VALID, 0) AS N_CR_VALID
    FROM (SELECT DISTINCT PUB_PY FROM PubYear) AS S
    JOIN (SELECT CR_ID, CR_RPY FROM CR_Indicators1) AS C ON (1=1)
    LEFT OUTER JOIN PubYear ON (S.PUB_PY = PubYear.PUB_PY AND C.CR_ID = PubYear.CR_ID)
),
-- Bildung der Zitierungszahlen unter Berücksichtigung von NPCT_RANGE (in Join-Bedingung <= ...)
PubYearRange AS (
    SELECT P1.CR_ID, P1.CR_RPY, P1.PUB_PY, SUM (P2.N_CR_VALID) AS N_CR_VALID
    FROM PubYearComplete P1 
    JOIN PubYearComplete P2 ON (P1.CR_ID = P2.CR_ID AND ABS (P1.PUB_PY - P2.PUB_PY) <= 0)
    GROUP BY P1.CR_ID, P1.PUB_PY
)


, 
-- Bildung der prozentualen Ranking-Werte
RankPerCent AS (
    SELECT CR_ID, PUB_PY, N_CR_VALID, 
    (CASE WHEN CR_RPY IS NULL OR PUB_PY IS NULL THEN 0 ELSE 
    PERCENT_RANK()  
        OVER ( 
            PARTITION BY CR_RPY, PUB_PY
            ORDER BY N_CR_VALID ASC
        ) END) AS P
    FROM PubYearRange
)


, 
-- Markieren, ob in den Top-x %
-- CountTopN AS (
--     SELECT *, (CASE WHEN P>=0.5 THEN 1 ELSE 0 END) AS P50,
--         (CASE WHEN P>=0.75 THEN 1 ELSE 0 END) AS P75,
--         (CASE WHEN P>=0.9 THEN 1 ELSE 0 END) AS P90
--         (CASE WHEN P>=0.99 THEN 1 ELSE 0 END) AS P99
--         (CASE WHEN P>=0.999 THEN 1 ELSE 0 END) AS P999
--     FROM RankPerCent
--     ORDER BY CR_ID, PUB_PY
-- ),
-- Zählen, wie häufig jeweils in den Top-x %
CR_Indicators2 AS (
    SELECT CR_ID, 
        SUM (CASE WHEN P>=0.5 THEN 1 ELSE 0 END) AS P50,
        SUM (CASE WHEN P>=0.75 THEN 1 ELSE 0 END) AS P75,
        SUM (CASE WHEN P>=0.90 THEN 1 ELSE 0 END) AS P90,
        SUM (CASE WHEN P>=0.99 THEN 1 ELSE 0 END) AS P99,
        SUM (CASE WHEN P>=0.999 THEN 1 ELSE 0 END) AS P999
    FROM RankPerCent
    GROUP BY CR_ID
),
CR_New AS (
    SELECT * 
    FROM CR_Indicators1
    JOIN CR_Indicators2 USING (CR_ID)
) 
UPDATE CR 
SET
    CR_N_PYEARS = CR_New.N_PYEARS,	
    CR_PYEAR_PERC = CR_New.PERC_PYEARS,
    CR_PERC_YR = CR_New.PERC_YR, 
    CR_PERC_ALL = CR_New.PERC_ALL, 
    CR_N_PCT_P50 = CR_New.P50,
    CR_N_PCT_P75 = CR_New.P75,
    CR_N_PCT_P90 = CR_New.P90,
    CR_N_PCT_P99 = CR_New.P99,
    CR_N_PCT_P999 = CR_New.P999
FROM CR_New
WHERE CR.CR_ID = CR_New.CR_ID

UPDATE CR AS C
SET N_CR = X.Y
FROM (SELECT CR_ID, COUNT(*) AS Y FROM PUB_CR GROUP BY CR_ID) AS X
WHERE C.CR_ID = X.CR_ID


select * from cr limit 1


-- SELECT *
-- FROM RankPercent
-- where cr_id = 64


-- SELECT *
-- FROM PubYearRange
-- WHERE pub_py=2013
-- AND  N_CR_VALID > 0
-- AND CR_ID IN (
-- SELECT CR_ID FROM CR WHERE CR_RPY=2006)