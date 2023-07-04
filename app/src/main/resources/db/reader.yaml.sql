on_new_cr:
    INSERT INTO CR  ( 
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
        CR_VI,	
        CR_Format
    ) 
    VALUES (
        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
    );

on_new_pub:
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

on_new_pub_cr:
    INSERT INTO PUB_CR  ( 
        PUB_ID, 
        CR_ID
    ) 
    VALUES ( 
        ?, ? 
    );

on_new_matchpair_manu:
    INSERT INTO CR_MATCH_MANU (
        CR_ID1, 
        CR_ID2, 
        sim
    )
    VALUES (
        ?, ?, ?
    );

on_new_matchpair_auto:
    INSERT INTO CR_MATCH_AUTO (
        CR_ID1, 
        CR_ID2, 
        sim
    )
    VALUES (
        ?, ?, ?
    );

on_after_load:
    WITH CRPUBCOUNT AS (
        SELECT CR_ID, COUNT(*) AS PUBCOUNT
        FROM PUB_CR
        GROUP BY CR_ID
    )
    UPDATE CR
    SET 
        CR_N_CR = CRPUBCOUNT.PUBCOUNT,
        CR_ClusterId1 = 1, 
        CR_ClusterId2 = 1
    FROM CRPUBCOUNT
    WHERE CR.CR_ID = CRPUBCOUNT.CR_ID;