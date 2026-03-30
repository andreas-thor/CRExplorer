package cre.store.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import cre.data.type.abs.Remover;
import cre.data.type.abs.Statistics.IntRange;

public class Remover_DB implements Remover {
    

	private Connection dbCon;

    public Remover_DB(Connection dbCon) {
        this.dbCon = dbCon;
    }

    @Override
	public void removeCR(List<Integer> toDelete) {
		String crList = toDelete.stream().map(crId -> String.valueOf(crId)).collect(Collectors.joining(","));
		removeCR(String.format("CR_ID IN (%s)", crList));
	}

	@Override
	public void retainCR(List<Integer> toRetain) {
		String crList = toRetain.stream().map(crId -> String.valueOf(crId)).collect(Collectors.joining(","));
		removeCR(String.format("CR_ID NOT IN (%s)", crList));
	}

	@Override
	public void removeCRWithoutYear() {
		removeCR("CR_RPY IS NULL");
	}

	@Override
	public void removeCRByYear(IntRange range) {
		removeCR(String.format("NOT(CR_RPY IS NULL) AND (%d<=CR_RPY) AND (%d>=CR_RPY)", range.getMin(), range.getMax()));  
	}

	@Override
	public void removeCRByN_CR(IntRange range) {
		removeCR(String.format("(%d<=CR_N_CR) AND (%d>=CR_N_CR)", range.getMin(), range.getMax()));
	}

	@Override
	public void removeCRByPERC_YR(String comp, double threshold) {
		removeCR(String.format(Locale.US, "COALESCE(CR_PERC_YR,0) %s %f", comp, threshold));	// locale US to force decimal point
	}

	@Override
	public void removePubByCR(List<Integer> selCR) {
		String crIds = selCR.stream().map(crId -> String.valueOf(crId)).collect(Collectors.joining(","));
		removePub(String.format("NOT IN (SELECT PUB_ID FROM PUB_CR WHERE CR_ID IN (%s))", crIds));
	}
	
	@Override
	public void retainPubByCitingYear(IntRange range) {
		removePub(String.format("IN (SELECT PUB_ID FROM PUB WHERE (PUB_PY IS NULL) OR (%d > PUB_PY) OR (PUB_PY > %d))", range.getMin(), range.getMax()));
	}    


	private void removeCR (String predicate) {
		try {
			Statement stmt = dbCon.createStatement();
			stmt.executeUpdate(String.format ("DELETE FROM PUB_CR WHERE CR_ID IN (SELECT CR_ID FROM CR WHERE %s)",  predicate)); 
			stmt.executeUpdate(String.format ("DELETE FROM CR_MATCH_AUTO WHERE CR_ID1 IN (SELECT CR_ID FROM CR WHERE %1$s) OR CR_ID2 IN (SELECT CR_ID FROM CR WHERE %1$s)",  predicate)); 
			stmt.executeUpdate(String.format ("DELETE FROM CR_MATCH_MANU WHERE CR_ID1 IN (SELECT CR_ID FROM CR WHERE %1$s) OR CR_ID2 IN (SELECT CR_ID FROM CR WHERE %1$s)",  predicate)); 
			stmt.executeUpdate(String.format ("DELETE FROM CR WHERE %s",  predicate)); 
			dbCon.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	// PUB_ID NOT IN (SELECT PUB_ID FROM PUB_CR WHERE CR_ID IN (%s))"
	
	/**
	 * Remove all citing publications, that do *not* reference any of the given CRs 
	 * @param crIds comma-separated ids
	 */

	private void removePub (String predicate) {
		try {
			Statement stmt = dbCon.createStatement();

			// String.format("PUB_ID NOT IN (SELECT PUB_ID FROM PUB_CR WHERE CR_ID IN (%s))", crIds)

			// mark CR that reference at least one publication (--> N_CR=NULL)
			stmt.executeUpdate(String.format ("""
				UPDATE CR 
				SET CR_N_CR = NULL 
				WHERE CR_ID IN (
					SELECT CR_ID 
					FROM PUB_CR 
					WHERE PUB_ID %s
				)
				""",  predicate));
			
			// we do delete pubs, because we may consider pubs without references
			// stmt.executeUpdate(String.format ("""
			// 	DELETE PUB 
			// 	WHERE PUB_ID NOT IN (
			// 		SELECT PUB_ID 
			// 		FROM PUB_CR 
			// 		WHERE CR_ID IN (%s)
			// 	)
			// 	""", crIds));

			// delete pub-cr-relationship (must be after pubs because we need the pub_cr info to identify the pubs to be removed)
			stmt.executeUpdate(String.format ("DELETE FROM PUB_CR WHERE PUB_ID %s", predicate));
			
			// update N_CR 
			stmt.executeUpdate("""
				UPDATE CR 
				SET CR_N_CR = (
					SELECT COUNT(*) 
					FROM PUB_CR 
					WHERE PUB_CR.CR_ID = CR.CR_ID
				) 
				WHERE CR_N_CR IS NULL
			""");
			
			// remove CRs with N_CR=0
			removeCR("CR_N_CR = 0"); 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

    
}
