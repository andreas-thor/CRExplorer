package cre.store.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.Clustering;
import cre.data.type.abs.MatchPairGroup;
import cre.ui.statusbar.StatusBar;

public class Clustering_DB extends Clustering<CRType_DB, PubType_DB> {

	private Connection dbCon;
	
	public Clustering_DB(Connection dbCon) {
		this.dbCon = dbCon;
		

	}
	
	
	
	
	
	@Override
	public Set<CRType_DB> addManuMatching(List<Integer> selCR, ManualMatchType matchType) {

		assert selCR != null;
		assert selCR.stream().filter(crId-> crId==null).count() == 0;
		
		Long timestamp = System.currentTimeMillis();		// used to group together all individual mapping pairs of match operation
		String crIds = selCR.stream().map(crId -> String.valueOf(crId)).collect(Collectors.joining(","));

		try {
			// manual-same is indicated by similarity = 2; different = -2
			if ((matchType==Clustering.ManualMatchType.SAME) || (matchType==Clustering.ManualMatchType.DIFFERENT)) {
				double sim = (matchType==Clustering.ManualMatchType.SAME) ? 2d : -2d;
			
				PreparedStatement insertMatchManu_PrepStmt = dbCon.prepareStatement(DB_Store.Queries.getQuery("pst_insert_match_manu.sql"));

				for (Integer cr1Id: selCR) {
					for (Integer cr2Id: selCR) {
						if (cr1Id.intValue()<cr2Id.intValue()) {
							insertMatchManu_PrepStmt.setInt(1,  cr1Id);
							insertMatchManu_PrepStmt.setInt(2,  cr2Id);
							insertMatchManu_PrepStmt.setDouble(3,  sim);
							insertMatchManu_PrepStmt.setLong(4,  timestamp);
							insertMatchManu_PrepStmt.addBatch();
						}
					}
				}
				insertMatchManu_PrepStmt.executeBatch();
			}
	
	
			
			if (matchType==Clustering.ManualMatchType.EXTRACT) {
				dbCon.createStatement().execute(
					String.format(
						"MERGE INTO CR_MATCH_MANU  (CR_ID1, CR_ID2, sim , tstamp) " + 
						"SELECT (CASE WHEN CR1.CR_ID < CR2.CR_ID THEN CR1.CR_ID ELSE CR2.CR_ID END), " + 
						"       (CASE WHEN CR1.CR_ID > CR2.CR_ID THEN CR1.CR_ID ELSE CR2.CR_ID END), " + 
						"       -2, %d " +
						"FROM  CR  AS CR1 JOIN CR AS CR2 " + 
						"ON (CR1.CR_ID != CR2.CR_ID AND CR1.CR_ClusterId1 = CR2.CR_ClusterId1 AND CR1.CR_ClusterId2 = CR2.CR_ClusterId2) " +
						"WHERE CR1.CR_ID IN (%s)" ,
						timestamp, crIds)
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		// changeCR = all CRs that are in the same cluster as selCR
		return CRTable_DB.get().getDBStore().selectCR(String.format("WHERE (CR_ClusterId1, CR_ClusterId2) IN (SELECT CR_ClusterId1, CR_ClusterId2 FROM CR WHERE CR.CR_ID IN (%s))", crIds)).collect(Collectors.toSet());
	}
	

	@Override
	public void generateAutoMatching() {

		
		Long stop1 = System.currentTimeMillis(); 
		
		// standard blocking: year + first letter of last name
		StatusBar.get().setValue(String.format("Blocking of %d objects...", CRTable.get().getStatistics().getNumberOfCRs()));
		
		try {

			dbCon.createStatement().execute(
				"UPDATE CR SET CR_BLOCKINGKEY = " + 
				"CASE WHEN (cr_RPY is not null) AND  (cr_AU_L is not null) AND (length(cr_AU_L)>0) " + 
				"THEN concat (cr_rpy, lower (substring(cr_AU_L,  1, 1))) ELSE NULL END ");

			ResultSet rs = dbCon.createStatement().executeQuery ("SELECT COUNT (DISTINCT CR_BLOCKINGKEY) FROM CR WHERE NOT (CR_BLOCKINGKEY IS NULL)");
			rs.next();
			int noOfBlocks = rs.getInt(1);
			rs.close();
			
			StatusBar.get().initProgressbar(noOfBlocks, String.format("Matching %d objects in %d blocks", CRTable.get().getStatistics().getNumberOfCRs(), noOfBlocks));

			StringMetric l = StringMetrics.levenshtein();
			
			
			// TODO: handle missing values
			// TODO: incorporate title (from scopus)
			
			
			// Matching: author lastname & journal name

			this.dbCon.createStatement().execute("TRUNCATE TABLE CR_MATCH_AUTO");
			PreparedStatement insertMatchAuto_PrepStmt = dbCon.prepareStatement(DB_Store.Queries.getQuery("pst_insert_match_auto.sql"));
			AtomicInteger insertMatchAuto_Counter = new AtomicInteger(0);
			
			NewMatchingPair<CRType_DB> newMatchPair = (CRType_DB cr1, CRType_DB cr2, double sim) -> {
				try {
					insertMatchAuto_PrepStmt.setInt(1, Math.min(cr1.getID(), cr2.getID()));
					insertMatchAuto_PrepStmt.setInt(2, Math.max(cr1.getID(), cr2.getID()));
					insertMatchAuto_PrepStmt.setDouble(3, sim);
					insertMatchAuto_PrepStmt.addBatch();
					
					if (insertMatchAuto_Counter.incrementAndGet()>=1000) {
						insertMatchAuto_PrepStmt.executeBatch();
						insertMatchAuto_Counter.set(0);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			};
			
			List<CRType_DB> crlist = new ArrayList<CRType_DB>();
			StringBuffer lastBlockingKey = new StringBuffer();
			
			CRTable_DB.get().getDBStore().selectCR("WHERE NOT (CR_BLOCKINGKEY IS NULL) ORDER BY CR_BLOCKINGKEY").forEach( cr -> {
			
				if (!lastBlockingKey.toString().equals(cr.getBlockingkey())) {
					StatusBar.get().incProgressbar();
					crossCompareCR(crlist, l, newMatchPair);
					crlist.clear();
					lastBlockingKey.delete(0, lastBlockingKey.length());
					lastBlockingKey.append(cr.getBlockingkey());
				}
				
				crlist.add(cr);
			});
			
			crossCompareCR(crlist, l, newMatchPair);
			
			if (insertMatchAuto_Counter.get()>0) {
				insertMatchAuto_PrepStmt.executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Long stop2 = System.currentTimeMillis();
		System.out.println("Match time is " + ((stop2-stop1)/100) + " deci-seconds");
		StatusBar.get().setValue("Matching done");
	}




	@Override
	public Set<CRType_DB> undoManuMatching() {

		try {
			// get latest timestamp
			ResultSet rs = dbCon.createStatement().executeQuery("SELECT MAX(tstamp) FROM CR_MATCH_MANU");
			if (!rs.next()) return new HashSet<CRType_DB>();
			long tstamp = rs.getLong(1);
			rs.close();

			// remove all manual matches of lates timestamp
			dbCon.createStatement().execute(String.format("DELETE FROM CR_MATCH_MANU WHERE tstamp = %d", tstamp));
			return null;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return new HashSet<CRType_DB>();
		}
	}

	@Override
	public void updateClustering(ClusteringType type, Set<CRType_DB> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		try {

			String changeCRIds = (changeCR != null) ? changeCR.stream().map(cr -> String.valueOf(cr.getID())).collect(Collectors.joining(",")) : null; 

			if (type == Clustering.ClusteringType.INIT) {	// consider manual (automatic?) matches only
				// reset all clusters (each CR forms an individual clustering)
				dbCon.createStatement().execute("UPDATE CR SET CR_ClusterId1 = CR_ID, CR_ClusterId2 = CR_ID");
			}
			
			if (type == Clustering.ClusteringType.REFRESH) {
				// reset clusterId2 only 
				dbCon.createStatement().execute(String.format("UPDATE CR SET CR_ClusterId2 = CR_ID %s", changeCRIds==null ? "" : String.format("WHERE CR_ID IN (%s)", changeCRIds)));
			}
		
			StatusBar.get().initProgressbar(1, String.format("Clustering %d objects (%s) with threshold %.2f", CRTable.get().getStatistics().getNumberOfCRs(), type.toString(), threshold));

			
			String and = "";
			and += useVol ? "AND COALESCE(CR1.CR_VOL, 'A') = COALESCE(CR2.CR_VOL, 'B') " : "";
			and += usePag ? "AND COALESCE(CR1.CR_PAG, 'A') = COALESCE(CR2.CR_PAG, 'B') " : "";
			and += useDOI ? "AND COALESCE(CR1.CR_DOI, 'A') = COALESCE(CR2.CR_DOI, 'B') " : "";
			and += (changeCRIds != null) ? String.format ("AND CR1.CR_ID IN (%s) ", changeCRIds) : "";
			and += (changeCRIds != null) ? String.format ("AND CR2.CR_ID IN (%s) ", changeCRIds) : "";
			
			// Locale.US makes sure that the threshold value has a point and no comma (-> would lead to SQL syntax error)
			PreparedStatement updateclustering_PrepStmt = dbCon.prepareStatement(String.format(Locale.US, DB_Store.Queries.getQuery("updateclustering.sql"), threshold, and));
			
			int noOfUpdates = -1;
			while ((noOfUpdates = updateclustering_PrepStmt.executeUpdate()) > 0) { 
				// System.out.println("NoOfUpdates = " + noOfUpdates);
				
			}
			updateclustering_PrepStmt.close();
			
			dbCon.createStatement().execute(DB_Store.Queries.getQuery("updateclustersize.sql"));

			
		} catch (SQLException e) {
			e.printStackTrace(); 	// TODO Auto-generated catch block
		}

		
		
		
	}

	@Override
	public long getNumberOfMatches(boolean manual) {
		try {
			dbCon.setAutoCommit(true);
			Statement stmt = dbCon.createStatement();
			ResultSet rs = stmt.executeQuery(String.format("SELECT COUNT(*) FROM CR_MATCH_%s", manual ? "MANU" : "AUTO"));
			rs.next();
			long res = rs.getLong(1);
			stmt.close();
			return res;
		} catch (Exception e) {
			return -1l;
		}
	}

	@Override
	public long getNumberOfClusters() {
		try {
			dbCon.setAutoCommit(true);
			Statement stmt = dbCon.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ( SELECT  DISTINCT CR_ClusterId1, CR_ClusterId2  FROM CR ) AS T");
			rs.next();
			long res = rs.getLong(1);
			stmt.close();
			return res;
		} catch (Exception e) {
			return -1l;
		}
	}





	@Override
	public Stream<MatchPairGroup> getMatchPairGroups(boolean manual) {
		// sortById is ignored since we need to have ORDER BY in the SQL query anyway to get the groups
		try {
			ResultSet rs = dbCon.createStatement().executeQuery(String.format("SELECT CR_ID1, CR_ID2, sim FROM CR_MATCH_%s ORDER BY CR_ID1, CR_ID2", manual?"MANU":"AUTO")); 
			return StreamSupport.stream(new MatchPairGroup_Resultset(rs).getIterable().spliterator(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Stream<MatchPairGroup> emptyStr = Stream.of();
			return emptyStr;
		}
	}

}
