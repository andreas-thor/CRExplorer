package cre.store.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import cre.CRELogger;
import cre.Timestamp;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.Clustering;
import cre.data.type.abs.MatchPairGroup;
import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;
import cre.ui.statusbar.StatusBar;

public class CRTable_DB extends CRTable<CRType_DB, PubType_DB> {


	
	private static CRTable_DB crTab = null;

	private Connection dbCon;
	private DB_Store dbStore;
	public static String url = "jdbc:sqlite:db/default.db";	// default database url; can be overwritten by main program
	public static boolean createSchemaOnStartup = true;
	
	 
	
	private Statistics_DB statistics;

	private Clustering_DB clustering;
	private Loader_DB loader;
	private Remover_DB remover; 
	private Filter_DB filter;
	private Importer_DB importer;


	private OberservableCRList_DB observableCRList;

	private CRTableView_DB tableView;
	

	public static CRTable_DB get() {
		if (crTab == null) {
			crTab = new CRTable_DB();
		}
		return crTab;
	}
	
	


	

	@Override
	public CRTableView_DB getTableView() {
		tableView = new CRTableView_DB(this);
		return this.tableView;
	}




	// @Override
	// public OberservableCRList_DB getObservableCRList() {
	// 	observableCRList = new OberservableCRList_DB(dbCon, dbStore, statistics);
	// 	return observableCRList;
	// }

	public OberservableCRList_DB createNewObservableCRList_DB() {
		this.observableCRList = new OberservableCRList_DB(dbCon, dbStore, statistics); 
		return getObservableCRList_DB();
	}
	public OberservableCRList_DB getObservableCRList_DB() {
		return this.observableCRList;
	}


	public void updateObservableCRList() {
		this.observableCRList.invalidateCache();
	}

	
	DB_Store getDBStore() {
		return this.dbStore;
	}
	

	// public Connection getDBCon () throws SQLException {
	// 	Connection c = DriverManager.getConnection("jdbc:sqlite:test.db");
	// 	c.setAutoCommit(false);
	// 	return c;
	// }

	private CRTable_DB () { 
		
		try {
			// load specific database driver and create connection
			if (CRTable_DB.url.toLowerCase().startsWith("jdbc:postgresql")) {
				Class.forName("org.postgresql.Driver" );
				Queries.sqlDialect = "postgres";
			}
			if (CRTable_DB.url.toLowerCase().startsWith("jdbc:sqlite")) {
    			Class.forName("org.sqlite.JDBC");				
				Queries.sqlDialect = "sqlite";
			}
			System.out.println(CRTable_DB.url);
			dbCon = DriverManager.getConnection(CRTable_DB.url);

			dbStore = new DB_Store(dbCon);
			// dbStore.init();
			
			statistics = new Statistics_DB(dbCon);
			clustering = new Clustering_DB(dbCon);
			loader = new Loader_DB(dbCon);
			remover = new Remover_DB(dbCon);
			filter = new Filter_DB(dbCon);
			importer = new Importer_DB(dbCon);
			createNewObservableCRList_DB();
			


		} catch (ClassNotFoundException | SQLException | IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// init();
	}
	
	
	@Override
	public void init() {
		this.setAborted(false);
		
		try {
			this.dbStore.init();
		} catch (SQLException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.filter.setShowNull(true);

	}

	

	@Override
	public CRType_DB getCRById(Integer id) {
		return this.dbStore.selectCR("WHERE CR_ID = " + id).findFirst().get();
	}

	
	@Override
	public Stream<CRType_DB> getCR(boolean sortById) {
		return this.dbStore.selectCR(sortById ? "ORDER BY CR_ID" : "");
	}

	
	@Override
	public Stream<PubType_DB> getPub(boolean includePubsWithoutCRs, boolean sortById) {
		String where = includePubsWithoutCRs ? "" : "WHERE PUB_ID IN (SELECT PUB_ID FROM PUB_CR) ";
		String order = sortById ? "ORDER BY PUB_ID " : "";
		return this.dbStore.selectPub(where + order);
	}



	@Override
	protected void onNpctRangeChanged () {
		updateData();
	} 
	
	@Override
	public void updateData() throws OutOfMemoryError {
		
		
		try {
			
			Timestamp.ts("updateData start");

			Statement stmt = dbCon.createStatement();

			IntRange range_RPY  = statistics.getMaxRangeRPY();
			IntRange range_PY  = statistics.getMaxRangePY();
			int NCR_ALL = (int) statistics.getSumNCR();
			
			int[] NCR_RPY = new int[range_RPY.getSize()];
			int[] CNT_RPY = new int[range_RPY.getSize()];
			
			ResultSet rs = stmt.executeQuery("SELECT CR_RPY, SUM(CR_N_CR), COUNT(CR_ID) FROM CR WHERE NOT (CR_RPY IS NULL) GROUP BY CR_RPY ORDER BY CR_RPY");
			while (rs.next()) {
				int rpyIdx = rs.getInt(1)-range_RPY.getMin();
				NCR_RPY[rpyIdx] = rs.getInt(2);
				CNT_RPY[rpyIdx] = rs.getInt(3);
			}
			rs.close();

			Timestamp.ts("updateData for compute");

			computeForAllCRs (range_RPY, range_PY, NCR_ALL, NCR_RPY, CNT_RPY);

			Timestamp.ts("updateData nach compute");

			updateObservableCRList();
			
			getChartData().updateChartData(range_RPY, NCR_RPY, CNT_RPY);

			Timestamp.ts("updateData ende");

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private void computeForAllCRs (IntRange range_RPY, IntRange range_PY, int NCR_ALL, int[] NCR_RPY, int[] CNT_RPY) {
		
	
		int crSize = -1;
		int firstPY = -1; 
		int lastPY = -1;
		int pySize = -1;
		
		int lastCrId = -1;
		int idx = -1;
		
		int[][] NCR_CR_PY = null;	
		int[] NCR_CR = null;	
		int[] NCR_CR_all = null;	
		int[] NPYEARS_CR = null;
		int[] NCR_PY = null; 	
		int[] NCR = null; 
		int[] mapCrIdxToCrId = null;	
		
		try {
			// get all CRs ordered by RPY ...
			Statement stmt = dbCon.createStatement();
			ResultSet rs = stmt.executeQuery(
				" SELECT CR.CR_RPY, CR.CR_ID, CR.CR_N_CR, PUB.PUB_PY, COUNT(*) " + 
				" FROM CR " + 
				" JOIN PUB_CR ON (CR.CR_ID = PUB_CR.CR_ID) " + 
				" JOIN PUB ON (PUB_CR.PUB_ID = PUB.PUB_ID) " + 
				" WHERE NOT (CR.CR_RPY IS NULL) " + 
				" GROUP BY CR.CR_RPY,  CR.CR_ID, CR.CR_N_CR, PUB.PUB_PY " + 
				" ORDER BY CR.CR_RPY, CR.CR_ID  ");
		
			
			int lastRPY = -1;
			int rpyIdx = -1;
			boolean invalidRPY_PY_Range = false; 

			
		
			while (rs.next()) {
				
				int rpy = rs.getInt(1);
				
				if ((rpy == lastRPY) && (invalidRPY_PY_Range)) continue;
				
				// ... we determine the blocks of CRs sharing the same RPY and compute indicators per block (rpy)
				if (rpy != lastRPY) {
					
					if ((lastRPY != -1) && (!invalidRPY_PY_Range)) {
						int[] mapCrIdxToCrId_final = mapCrIdxToCrId;
						computeCRIndicators(rpyIdx, crSize, pySize, NCR_ALL, NCR_RPY, NCR_CR_PY, NCR_CR, NCR_CR_all, NPYEARS_CR, NCR_PY, NCR, 
								(int crIdx, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE) -> { 
									dbStore.updateCRIndicators(mapCrIdxToCrId_final[crIdx], N_PYEARS, PYEAR_PERC, PERC_YR, PERC_ALL, N_PCT, N_PCT_AboveAverage, SEQUENCE, TYPE);
								}
						);						
					}
					
					// new RPY
					lastRPY = rpy;
					rpyIdx = rpy-range_RPY.getMin();

					// check if meaningful range of PYs
					invalidRPY_PY_Range = false; 
					firstPY = (rpy<=range_PY.getMin()) ? range_PY.getMin() : rpy;	// usually: rpy<=range_PY[0] 
					lastPY = range_PY.getMax();
					if (lastPY < firstPY) {
						invalidRPY_PY_Range = true;
						continue;
					};
					
					// init all data structures
					crSize = CNT_RPY[rpyIdx];
					pySize = lastPY-firstPY+1;
					NCR_CR_PY = new int[crSize][pySize];	
					NCR_CR = new int[crSize];	
					NCR_CR_all = new int[crSize];	
					NPYEARS_CR = new int[crSize];
					NCR_PY = new int[pySize];	
					NCR = new int[1];
					mapCrIdxToCrId = new int[crSize];
					
					lastCrId = -1;
					idx = -1;
				}
				
				int crId = rs.getInt(2);
				if (crId != lastCrId) {
					lastCrId = crId;
					idx++;
					NCR_CR_all[idx] = rs.getInt(3);
					mapCrIdxToCrId[idx] = crId; 
				}
				
				int py = rs.getInt(4);
				int count = rs.getInt(5);
				if ((py>=firstPY) && (py<=lastPY)) {	// PY is out of range
					int pyIdx = py-firstPY;
					NPYEARS_CR[idx]++;
					NCR_CR_PY[idx][pyIdx] = count;
					NCR_CR[idx] += count;
					NCR_PY[pyIdx] += count;
					NCR[0] += count;
				}
			}
			rs.close();
			stmt.close();
			
			
			// wrap up: process last block ...
			if ((lastRPY != -1) && (!invalidRPY_PY_Range)) {
				int[] mapCrIdxToCrId_final = mapCrIdxToCrId;
				computeCRIndicators(rpyIdx, crSize, pySize, NCR_ALL, NCR_RPY, NCR_CR_PY, NCR_CR, NCR_CR_all, NPYEARS_CR, NCR_PY, NCR, 
						(int crIdx, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE) -> { 
							dbStore.updateCRIndicators(mapCrIdxToCrId_final[crIdx], N_PYEARS, PYEAR_PERC, PERC_YR, PERC_ALL, N_PCT, N_PCT_AboveAverage, SEQUENCE, TYPE);
						}
				);						
			}
			
			// ... and finish
			dbStore.finishUpdateCRIndicators();

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	// #region Loader ---------------------------------------------------------------------

	@Override
	public void onBeforeLoad() {
		this.loader.onBeforeLoad();
	}

	@Override
	public void onAfterLoad() {
		this.loader.onAfterLoad();
		updateData();
		updateClustering(Clustering.ClusteringType.INIT, null, Clustering.min_threshold, false, false, false, false);

	}

	@Override
	public void onNewCR(CRType_MM cr) {
		this.loader.onNewCR(cr);
	}

	@Override
	public void onNewPub(PubType_MM pub, List<Integer> crIds) {
		this.loader.onNewPub(pub, crIds);
	}

	@Override
	public void onNewMatchPair(int crId1, int crId2, double sim, boolean isManual) {
		this.loader.onNewMatchPair(crId1, crId2, sim, isManual);
	}
	
	// #endregion

	// #region Importer --------------------------------------------------------------

	@Override
	public void onBeforeImport () {
		this.importer.onBeforeImport ();
	}

	@Override
	public void addPub(PubType_MM pub) {
		this.importer.addPub(pub);
	}

	@Override
	public void onAfterImport () {
		this.importer.onAfterImport ();

		long ts2 = System.currentTimeMillis();
		long ms2 = Runtime.getRuntime().totalMemory();

		updateData();

		long ts3 = System.currentTimeMillis();
		long ms3 = Runtime.getRuntime().totalMemory();

		CRELogger.get().logInfo("Update time is " + ((ts3-ts2)/1000d) + " seconds");
		CRELogger.get().logInfo("Update Memory usage " + ((ms3-ms2)/1024d/1024d) + " MBytes");
	}

	// #endregion

	// #region Remover --------------------------------------------------------------

	@Override
	public void removeCR(List<Integer> toDelete) {
		this.remover.removeCR(toDelete);
		this.updateData();
	}

	@Override
	public void retainCR(List<Integer> toRetain) {
		this.remover.retainCR(toRetain);
		this.updateData();
	}

	@Override
	public void removeCRWithoutYear() {
		this.remover.removeCRWithoutYear();
		this.updateData();
	}

	@Override
	public void removeCRByYear(IntRange range) {
		this.remover.removeCRByYear(range);
		this.updateData();
	}

	@Override
	public void removeCRByN_CR(IntRange range) {
		this.remover.removeCRByN_CR(range);	
		this.updateData();
	}

	@Override
	public void removeCRByPERC_YR(String comp, double threshold) {
		this.remover.removeCRByPERC_YR(comp, threshold);
		this.updateData();
	}

	@Override
	public void removePubByCR(List<Integer> selCR) {
		this.remover.removePubByCR(selCR);
		this.updateData();
	}

	@Override
	public void retainPubByCitingYear(IntRange range) {
		this.remover.retainPubByCitingYear(range);
		this.updateData();
	}

	// #endregion

	// #region Filter --------------------------------------------------------------

	@Override
	public void filterByYear(IntRange range) {
		this.filter.filterByYear(range);
	}

	@Override
	public void filterByCluster(List<Integer> sel) {
		this.filterByCluster(sel);
	}

	@Override
	public void setShowNull(boolean showNull) {
		this.setShowNull(showNull);
	}

	@Override
	public void showAll() {
		this.showAll();
	}

	// #endregion

	// #region Statistics --------------------------------------------------

	@Override
	public long getNumberOfCRs() {
		return this.statistics.getNumberOfCRs();
	}

	@Override
	public long getSumNCR() {
		return this.statistics.getSumNCR();
	}


	@Override
	public long getNumberOfPubs() {
		return this.statistics.getNumberOfPubs();
	}

	@Override
	public long getNumberOfPubs(boolean includePubsWithoutCRs) {
		return this.statistics.getNumberOfPubs(includePubsWithoutCRs);
	}

	@Override
	public IntRange getMaxRangePY() {
		return this.statistics.getMaxRangePY();
	}

	@Override
	public int getNumberOfDistinctPY() {
		return this.statistics.getNumberOfDistinctPY();
	}

	@Override
	public IntRange getMaxRangeNCR() {
		return this.statistics.getMaxRangeNCR();
	}

	@Override
	public IntRange getMaxRangeRPY() {
		return this.statistics.getMaxRangeRPY();
	}

	@Override
	public IntRange getMaxRangeRPY(boolean visibleOnly) {
		return this.statistics.getMaxRangeRPY(visibleOnly);
	}

	@Override
	public int getNumberOfDistinctRPY() {
		return this.statistics.getNumberOfDistinctRPY();
	}

	@Override
	public int getNumberOfCRsByVisibility(boolean visible) {
		return this.statistics.getNumberOfCRsByVisibility (visible);
	}

	@Override
	public long getNumberOfCRsByNCR(IntRange range) {
		return this.statistics.getNumberOfCRsByNCR (range);
	}

	@Override
	public long getNumberOfCRsByPercentYear(String comp, double threshold) {
		return this.statistics.getNumberOfCRsByPercentYear (comp, threshold);
	}

	@Override
	public long getNumberOfCRsByRPY(IntRange range) {
		return this.statistics.getNumberOfCRsByRPY (range);
	}

	@Override
	public long getNumberOfPubsByCitingYear(IntRange range) {
		return this.statistics.getNumberOfPubsByCitingYear (range);
	}

	@Override
	public int getNumberOfCRsWithoutRPY() {
		return this.statistics.getNumberOfCRsWithoutRPY();
	}


	// #endregion

	// #region Clustering --------------------------------------------------


	@Override
	public void generateAutoMatching(String alg) {
		this.clustering.generateAutoMatching(alg);
	}

	@Override
	public Set<CRType_DB> addManuMatching(List<Integer> selCR, ManualMatchType matchType) {
		return this.clustering.addManuMatching(selCR, matchType);
	}

	@Override
	public Set<CRType_DB> undoManuMatching() {
		return this.clustering.undoManuMatching();
	}

	@Override
	public void updateClustering(ClusteringType type, Set<CRType_DB> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI, boolean nullEqualsNull) {
		this.clustering.updateClustering(type, changeCR, threshold, useVol, usePag, useDOI, nullEqualsNull);
	}

	@Override
	public void merge() {
		this.clustering.merge();
	}


	@Override
	public long getNumberOfMatches(boolean manual) {
		return this.clustering.getNumberOfMatches(manual);
	}

	@Override
	public long getNumberOfClusters() {
		return this.clustering.getNumberOfClusters();
	}

	@Override
	public Stream<MatchPairGroup> getMatchPairGroups(boolean manual) {
		return this.clustering.getMatchPairGroups(manual);
	}

	@Override
	public void setBlockingRPY(String s) {
		this.clustering.setBlockingRPY(s);
	}

	@Override
	public int getAlgorithm() {
		return this.clustering.getAlgorithm();
	}

	@Override
	public void setAlgorithm(int algorithm) {
		this.clustering.setAlgorithm(algorithm);
	}

	// #endregion


}
