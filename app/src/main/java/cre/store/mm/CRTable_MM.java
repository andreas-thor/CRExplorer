package cre.store.mm;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cre.CRELogger;
// import cre.data.CRSearch;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRType;
import cre.data.type.abs.Clustering;
import cre.data.type.abs.Filter;
import cre.data.type.abs.Statistics;
import cre.data.type.abs.Statistics.IntRange;
import cre.ui.statusbar.StatusBar;

public class CRTable_MM extends CRTable<CRType_MM, PubType_MM> {

	private static CRTable_MM crTab = null;

	private HashMap<Integer, CRType_MM> crById;		// CR.ID --> CR
	private HashMap<CRType_MM, CRType_MM> allCRs; 	//  CR to get duplicates
	private HashMap<PubType_MM, PubType_MM> allPubs; 
	
	private Loader_MM loader;
	private Filter_MM filter;
	
	private Statistics_MM statistics;
	
	private boolean duringUpdate;
	
	public boolean isDuringUpdate() {
		return duringUpdate;
	}


	private Clustering_MM crmatch;

	private CRTableView_MM tableView;
	
	public static CRTable_MM get() {
		if (crTab == null) {
			crTab = new CRTable_MM();
		}
		return crTab;
	}
	
	



	

	@Override
	public Statistics getStatistics() {
		return this.statistics;
	}
	
	@Override
	public Clustering_MM getClustering() {
		return this.crmatch;
	}



	@Override
	public CRTableView_MM getTableView() {
		tableView = new CRTableView_MM(this);
		return this.tableView;
	}

	

	// @Override
	// public ObservableList<CRType_MM> getObservableCRList() {
	// 	// we return the entire CRTable as list for the TableView in the UI
	// 	return FXCollections.observableArrayList(getCR().filter(cr -> cr.getVI()).collect(Collectors.toList()));
	// }


	
	private CRTable_MM () { 
		this.statistics = new Statistics_MM();
		this.loader = new Loader_MM();
		this.filter = new Filter_MM (this);
		init();
	}
	

	/**
	 * Initialize empty CRTable
	 */
	
	@Override
	public void init() {
		
		
		if (allPubs != null) {
			for (PubType_MM pub: allPubs.keySet()) {
				pub.removeAllCRs(true);
			}
		}
		
		crById = new HashMap<Integer, CRType_MM>();
		allCRs = new HashMap<CRType_MM, CRType_MM>();
		allPubs = new HashMap<PubType_MM, PubType_MM>();
		
		crmatch = new Clustering_MM(this);
		duringUpdate = false;
		this.setAborted(false);
		this.setShowNull(true);
		
		/* REMOVED Lucene Search due to module problems
		CRSearch.get().init();
		*/
	}	
	
	@Override
	public CRType_MM getCRById(Integer id) {
		return this.crById.get(id);
	}




	@Override
	public Stream<CRType_MM> getCR(boolean sortById) {
		return sortById ? allCRs.values().stream().sorted() : allCRs.values().stream();
	}

	/**
	 * 
	 * @param includePubsWithoutCRs default=false
	 * @return
	 */
	@Override
	public Stream<PubType_MM> getPub (boolean includePubsWithoutCRs, boolean sortById) {
		Stream<PubType_MM> res = includePubsWithoutCRs ? allPubs.keySet().stream() : getCR().flatMap(cr -> cr.getPub()).distinct();
		return sortById ? res.sorted() : res;
	}
	

	


	public CRType_MM addCR(CRType_MM cr, boolean checkForDuplicatesAndSetId) {
		
		if (checkForDuplicatesAndSetId) {
			CRType_MM crMain = this.allCRs.get(cr);
			if (crMain == null) {
				cr.setID(this.crById.size() + 1);
				cr.setCluster(new CRCluster(cr));
				this.crById.put(cr.getID(), cr);
				this.allCRs.put(cr, cr);
				return cr;
			} else {
				return crMain;
			}
		} else {
			this.crById.put(cr.getID(), cr);
			this.allCRs.put(cr, cr);
			return cr;
		}
	}
	
	
	/*
	 * We additionally store all pubs in allPubs
	 * This is later only used for export (to Scopus, WoS, CSV_Pub) when the user setting "include pubs without CRs" is set
	 */
	
//	public PubType addPub (PubType pub, boolean addCRs) {
//		return addPub (pub, addCRs, false);
//	}
	




	
	/**
	 * Merge CRs based on clustering
	 */

	@Override
	public void merge () {
		
		// get all clusters with size > 1
		Set<CRCluster> clusters = getCR().filter(cr -> cr.getClusterSize()>1).map(cr -> cr.getCluster()).distinct().collect(Collectors.toSet());
		StatusBar.get().setValue(String.format("Merging of %d clusters...", clusters.size()));

		// merge clusters
		clusters.forEach(cl -> {
			
			StatusBar.get().incProgressbar();
			
			// get mainCR = CR with highest number of citations
			CRType_MM crMain = cl.getMainCR();
			Set<CRType_MM> crMerge = cl.getCR().collect(Collectors.toSet());
			crMerge.remove(crMain);

			// merge CRs with main CR
			for (CRType_MM cr:crMerge) {
				cr.getPub().collect(Collectors.toList()).stream().forEach(crPub -> {		// make a copy to avoid concurrent modification
					crPub.addCR(crMain, true);
					crPub.removeCR(cr, true);
				});
			}
			
			// remove merged CRs
			crMerge.stream().forEach(cr -> this.crById.remove(cr.getID()));
			this.allCRs.keySet().removeAll(crMerge);
		});
		
		// reset clusters and match result
		getCR().forEach(cr -> cr.setCluster(new CRCluster(cr)));
		crmatch.init();
		
		updateData();
		StatusBar.get().setValue("Merging done");

	}
	
	@Override
	protected void onNpctRangeChanged () {
		updateData();
	} 

	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	private void updateData () throws OutOfMemoryError {


		long ts2 = System.currentTimeMillis();
		long ms2 = Runtime.getRuntime().totalMemory();
		
		duringUpdate = true;		// mutex to avoid chart updates during computation
		
		CRELogger.get().logInfo("update Data");
		CRELogger.get().logInfo(String.valueOf(System.currentTimeMillis()));
		
		CRELogger.get().logInfo("Compute Ranges in CRTable_MM");
		
		IntRange range_RPY = getStatistics().getMaxRangeRPY();
		IntRange range_PY  = getStatistics().getMaxRangePY();

		
		int[] NCR_ALL = new int[1];	// NCR overall (array length=1; array to make it effectively final)
		int[] NCR_RPY = new int[range_RPY.getSize()];	// (sum of) NCR by RPY
		int[] CNT_RPY = new int[range_RPY.getSize()];	// number of CRs by RPY
		
		// Group CRs by RPY, compute NCR_ALL and NCR_RPY
		CRELogger.get().logInfo("mapRPY_CRs");
		CRTable.get().getCR().forEach(cr -> {
			NCR_ALL[0] += cr.getN_CR();
			if (cr.getRPY()!=null) {
				NCR_RPY[cr.getRPY()-range_RPY.getMin()] += cr.getN_CR();
				CNT_RPY[cr.getRPY()-range_RPY.getMin()] += 1;
			}
		});
		
		
		getCR()
			.filter(cr -> cr.getRPY()!=null)
			.collect(Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toList())))
			.entrySet().stream().parallel()
			.forEach(rpyGroup -> {	
				int rpy = rpyGroup.getKey().intValue();
				List<CRType_MM> crList = rpyGroup.getValue();
				
				computeForAllCRsOfTheSameRPY (rpy, rpy-range_RPY.getMin(), range_PY, NCR_ALL[0], NCR_RPY, crList);
			}
		);		
		
		
		getChartData().updateChartData(range_RPY, NCR_RPY, CNT_RPY);
		
		duringUpdate = false;

		
		
		long ts3 = System.currentTimeMillis();
		long ms3 = Runtime.getRuntime().totalMemory();

		CRELogger.get().logInfo("Update time is " + ((ts3-ts2)/1000d) + " seconds");
		CRELogger.get().logInfo("Update Memory usage " + ((ms3-ms2)/1024d/1024d) + " MBytes");
		
	}

	
	private void computeForAllCRsOfTheSameRPY (int rpy, int rpyIdx, IntRange range_PY, int NCR_ALL, int[] NCR_RPY, List<CRType_MM> crList) {
		
		int crSize = crList.size();

		int firstPY = (rpy<=range_PY.getMin()) ? range_PY.getMin() : rpy;	// usually: rpy<=range_PY[0] 
		int lastPY = range_PY.getMax();
		if (lastPY < firstPY) return;
		int pySize = lastPY-firstPY+1;
		
		int[][] NCR_CR_PY = new int[crSize][pySize];	
		int[] NCR_CR = new int[crSize];	
		int[] NCR_CR_all = new int[crSize];	
		int[] NPYEARS_CR = new int[crSize];
		int[] NCR_PY = new int[pySize];	
		int[] NCR = new int[1];
		
		
		for (int x=0; x<crSize; x++) {

			final int crIdx = x;
			CRType<?> cr = crList.get(crIdx);
			
			NCR_CR_all[crIdx] = cr.getN_CR();
			
//			int[] NPYEARS = new int[1];
			cr.getPub().filter(pub -> pub.getPY() != null).forEach(pub -> {
				
				if ((pub.getPY()>=firstPY) && (pub.getPY()<=lastPY)) {	// PY is out of range
				
					int pyIdx = pub.getPY()-firstPY;
					
					if (NCR_CR_PY[crIdx][pyIdx]==0) {	// we found a citation from a new PY
//						NPYEARS[0]++;
						NPYEARS_CR[crIdx]++;
					}
					NCR_CR_PY[crIdx][pyIdx]++;
					NCR_CR[crIdx]++;
					NCR_PY[pyIdx]++;
					NCR[0]++;
				}
			});
			
//			cr.setN_PYEARS   (NPYEARS[0]);
		}
		
		
		
			
		computeCRIndicators(rpyIdx, crSize, pySize, NCR_ALL, NCR_RPY, NCR_CR_PY, NCR_CR, NCR_CR_all, NPYEARS_CR, NCR_PY, NCR, 
			(int crIdx, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE) -> { 
				CRType<?> cr = crList.get(crIdx);
				
				if (crIdx == 0) {
					// System.out.println(cr.getID());
				}
				
				cr.setN_PYEARS   (N_PYEARS);
				cr.setPYEAR_PERC (PYEAR_PERC);
				cr.setPERC_YR 	 (PERC_YR);
				cr.setPERC_ALL	 (PERC_ALL);
				cr.setN_PCT		(N_PCT);
				cr.setN_PCT_AboveAverage(N_PCT_AboveAverage);
				cr.setSEQUENCE(SEQUENCE);
				cr.setTYPE(TYPE);
			}				
		);
			
	}
	
	


	// #region Importer ---------------------------------------------- 

	@Override
	public void onBeforeImport() {
		// Nothing to do here
	}


	@Override
	public void addPub (PubType_MM pub) {
		
		pub.setID(this.allPubs.size()+1);
		
		
		int debug_before = this.allPubs.size();
		this.allPubs.put(pub, pub);
		int debug_after = this.allPubs.size();

		if (debug_after != debug_before+1) {
			CRELogger.get().logInfo("debug_after != debug_before+1");
		}
		
		
		for(CRType_MM cr: pub.getCR().collect(Collectors.toSet())) {
			
			CRType_MM crMain = this.allCRs.get(cr);
			if (crMain == null) {
				this.crById.put(cr.getID(), cr);
				this.allCRs.put(cr, cr);
			} else {
				pub.removeCR(cr, false);	
				pub.addCR(crMain, false);
				crMain.addPub(pub, false);	
			}
		}
		
	}
	

	@Override
	public void onAfterImport() {
		updateData();
	}

	// #endregion
	

	// #region Loader ---------------------------------------------- 

	@Override
	public void onBeforeLoad() {
		this.loader.onBeforeLoad();
	}

	@Override
	public void onAfterLoad() {
		this.loader.onAfterLoad();
		updateData();
		getClustering().updateClustering(Clustering.ClusteringType.INIT, null, Clustering.min_threshold, false, false, false, false);
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

		
	// #region Remover ----------------------------------------------


	@Override
	public void removeCR (List<Integer> toDelete) {
		getCR().forEach(cr -> cr.setFlag(false));
		toDelete.forEach(crId -> getCRById(crId).setFlag(true));
		removeCR(cr -> cr.isFlag());
	}
	
	@Override
	public void retainCR (List<Integer> toRetain) {
		getCR().forEach(cr -> cr.setFlag(true));
		toRetain.forEach(crId -> getCRById(crId).setFlag(false));
		removeCR(cr -> cr.isFlag());
	}
	
	@Override
	public void removeCRWithoutYear () {
		removeCR (cr -> cr.getRPY() == null);
	}

	
	@Override
	public void removeCRByYear (IntRange range) {
		removeCR (cr -> ((cr.getRPY()!=null) && (range.getMin() <= cr.getRPY()) && (cr.getRPY() <= range.getMax())));
	}

	@Override
	public void removeCRByN_CR(IntRange range) {
		removeCR (cr -> (range.getMin() <= cr.getN_CR()) && (cr.getN_CR() <= range.getMax()));
	}
	
	@Override
	public void removeCRByPERC_YR (String comp, double threshold) {
		switch (comp) {
			case "<" : removeCR (cr -> cr.getPERC_YR() <  threshold); break;
			case "<=": removeCR (cr -> cr.getPERC_YR() <= threshold); break;
			case "=" : removeCR (cr -> cr.getPERC_YR() == threshold); break;
			case ">=": removeCR (cr -> cr.getPERC_YR() >= threshold); break;
			case ">" : removeCR (cr -> cr.getPERC_YR() >  threshold); break;
		}
	}
	
	@Override
	public void removePubByCR (List<Integer> selCR) {
		selCR.stream()
			.map(crId -> crTab.getCRById(crId))
			.flatMap (cr -> cr.getPub())
			.forEach(pub -> pub.setFlag(true));

		removePub (pub -> !pub.isFlag());
		getPub().forEach(pub -> pub.setFlag(false));
	}
	
	@Override
	public void retainPubByCitingYear (IntRange range) {
		removePub (pub -> (pub.getPY()==null) || (range.getMin() > pub.getPY()) || (pub.getPY() > range.getMax()));
	}	
	
	private void removePub (Predicate<PubType_MM> cond) {
		getPub().filter(cond).collect(Collectors.toList()).forEach(pub -> pub.removeAllCRs(true));
		removeCR(cr -> cr.getN_CR()==0);
	}
	
	private void removeCR (Predicate<CRType_MM> cond) {
		
		crById.values().removeIf( cr ->  { 
			if (cond.test(cr)) {
				cr.removeAllPubs(true);
				allCRs.remove(cr);
				return true;
			} else {
				return false;
			}
		});
		updateData();
	}

	// #endregion

	// #region Filter ----------------------------------------------

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
		this.filter.setShowNull(showNull);
	}

	@Override
	public void showAll() {
		this.filter.showAll();
	}

	// #endregion

}

