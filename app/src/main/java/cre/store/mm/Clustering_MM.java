package cre.store.mm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.Clustering;
import cre.data.type.abs.MatchPairGroup;
import cre.ui.statusbar.StatusBar;

public class Clustering_MM extends Clustering<CRType_MM, PubType_MM> {

	
	private class CRPair {
		
		CRType_MM cr1;
		CRType_MM cr2;
		Double s;

		public CRPair(CRType_MM cr1, CRType_MM cr2, Double s) {
			super();
			
			if (cr1.getID()<cr2.getID()) {
				this.cr1 = cr1;
				this.cr2 = cr2;
			} else {
				this.cr1 = cr2;
				this.cr2 = cr1;
			}
			this.s = s;
		}
		

		@Override
		public String toString () {
			return (this.cr1.getID() + "/" + this.cr2.getID() + "/" + this.s);
		}
		
	}
	
	
	
	/**
	 * matchResult: M -> (CR1 -> (CR2 -> sim)
	 * match pair (CR1, CR2, sim) with CR1.ID<CR2.ID
	 * M=true --> manual match; M=false --> automatic match
	 * sim=similarity : -2=manual NON MATCH, +2=manual MATCH, in [0,1]=automatic match
	 */
	public Map <Boolean, Map<CRType_MM, Map<CRType_MM, Double>>> matchResult;
	private TreeMap <Long, ArrayList<CRPair>> timestampedPairs;

	private CRTable_MM crTab;
	
	
	public Clustering_MM (CRTable_MM crTab) {
		this.crTab = crTab;
		init();
	}
	
	
	
	public void init() {
		matchResult = new HashMap<Boolean, Map<CRType_MM, Map<CRType_MM,Double>>>();
		matchResult.put(false, new HashMap<CRType_MM,Map<CRType_MM,Double>>());		// automatic match result
		matchResult.put(true,  new HashMap<CRType_MM,Map<CRType_MM,Double>>());		// manual match result
		timestampedPairs = new TreeMap<Long, ArrayList<CRPair>>();
	}
	
	@Override
	public void generateAutoMatching () {
	
		// standard blocking: year + first letter of last name
		StatusBar.get().setValue(String.format("Blocking of %d objects...", CRTable.get().getStatistics().getNumberOfCRs()));
		Map<String, List<CRType_MM>> blocks = crTab.getCR().collect(Collectors.groupingBy(
			cr -> ((cr.getRPY() != null) && (cr.getAU_L() != null) && (cr.getAU_L().length() > 0)) ? cr.getRPY() + cr.getAU_L().substring(0,1).toLowerCase() : "", 
			Collectors.toList()
		));

		StatusBar.get().initProgressbar(blocks.entrySet().stream().mapToInt(entry -> (entry.getValue().size()*(entry.getValue().size()-1))/2).sum(), String.format("Matching %d objects in %d blocks", CRTable.get().getStatistics().getNumberOfCRs(), blocks.size()));
		matchResult.put(false, new HashMap<CRType_MM,Map<CRType_MM,Double>>());		// remove automatic match result, but preserve manual matching
		StringMetric l = StringMetrics.levenshtein();
		
		AtomicLong testCount = new AtomicLong(0);
		
		// TODO: handle missing values
		// TODO: incorporate title (from scopus)
		
		
		// Matching: author lastname & journal name
		List<CRPair> matchResult = blocks.entrySet().parallelStream().map ( entry -> {

			StatusBar.get().incProgressbar(entry.getValue().size()*(entry.getValue().size()-1)/2);
			
			List<CRPair> result = new ArrayList<CRPair>();
			if (entry.getKey().equals("")) return result;	// non-matchable block 

			List<CRType_MM> crlist = entry.getValue();
			
			crossCompareCR(crlist, l, (CRType_MM cr1, CRType_MM cr2, double sim) -> {
				result.add(new CRPair (cr1, cr2, sim));
				testCount.incrementAndGet();
			});
		
			return result;
		})
		.flatMap(it -> it.stream())
        .collect(Collectors.toList());
		
		// ... and invoke sequentially
		matchResult.forEach(it -> { addPair(it, false, true, null); });
		
		
		
		assert testCount.get() == getNumberOfMatches(false);
		
		StatusBar.get().setValue("Matching done");
	}
	
	

	
	@Override
	public Set<CRType_MM> addManuMatching (List<Integer> selCR, Clustering.ManualMatchType matchType) {
		
		assert selCR != null;
		assert selCR.stream().filter(crId -> crId==null).count() == 0;
		
		Long timestamp = System.currentTimeMillis();		// used to group together all individual mapping pairs of match operation
		
		// manual-same is indicated by similarity = 2; different = -2
		if ((matchType==Clustering.ManualMatchType.SAME) || (matchType==Clustering.ManualMatchType.DIFFERENT)) {
			double sim = (matchType==Clustering.ManualMatchType.SAME) ? 2d : -2d;
			for (Integer cr1Id: selCR) {
				for (Integer cr2Id: selCR) {
					if (cr1Id.intValue() < cr2Id.intValue()) addPair(new CRPair (crTab.getCRById(cr1Id), crTab.getCRById(cr2Id), sim), true, false, timestamp);
				}
			}
		}

		if (matchType==Clustering.ManualMatchType.EXTRACT) {
			for (Integer cr1Id: selCR) {
				CRType_MM cr1 = crTab.getCRById(cr1Id);
				cr1.getCluster().getCR().filter(cr2 -> cr1!=cr2).forEach(cr2 -> addPair (new CRPair (cr1, cr2, -2d), true, false, timestamp));
			}
		}
		
		return selCR.stream().map(crId -> crTab.getCRById(crId)).flatMap(cr -> cr.getCluster().getCR()).distinct().collect(Collectors.toSet());
	}	
	

	@Override
	public Set<CRType_MM> undoManuMatching () {
		
		// check if undo-able operations are available
		if (timestampedPairs.keySet().size()==0) return new HashSet<CRType_MM>();

		// copy old values and remove last undo/able operation 
		Long lastTimestamp = timestampedPairs.lastKey();
		List<CRPair> undoPairs = timestampedPairs.get(lastTimestamp);
		undoPairs.forEach(pair -> addPair(pair, true, false, null));
		
		return null; 
	}
	
	
	@Override
	public void updateClustering (Clustering.ClusteringType type, Set<CRType_MM> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI, boolean nullEqualsNull) {
		

		int pbSize = matchResult.get(false).size()+matchResult.get(true).size();
		
		if (type == Clustering.ClusteringType.INIT) {	// consider manual (automatic?) matches only
			crTab.getCR().forEach(cr -> cr.setCluster(new CRCluster(cr)));
			pbSize = matchResult.get(false).size();
		}
		
		if (type == Clustering.ClusteringType.REFRESH) {
			((changeCR == null) ? crTab.getCR() : changeCR.stream()).forEach(cr -> cr.setCluster (new CRCluster(cr, cr.getCluster().getC1())));
		}

		StatusBar.get().initProgressbar(pbSize, String.format("Clustering %d objects (%s) with threshold %.2f", CRTable.get().getStatistics().getNumberOfCRs(), type.toString(), threshold));
		
		// automatic matches
		matchResult.get(false).forEach((cr1, pairs) -> {
			
			StatusBar.get().incProgressbar();
			
			pairs.forEach((cr2, sim) -> {
				
				boolean changed = (changeCR == null) || (changeCR.contains(cr1) && changeCR.contains(cr2));
				boolean manualDifferent = (matchResult.get(true).get(cr1) != null) && (matchResult.get(true).get(cr1).get(cr2) != null) && (matchResult.get(true).get(cr1).get(cr2) == -2d);
				
				if (changed && (cr1.getCluster() != cr2.getCluster()) && (sim >= threshold) && !manualDifferent) {

					boolean vol = !useVol;
					vol = vol || ((cr1.getVOL()!=null) && (cr2.getVOL()!=null) && (cr1.getVOL().equals (cr2.getVOL())));
					vol = vol || (nullEqualsNull && (cr1.getVOL()==null) && (cr2.getVOL()==null));

					boolean pag = !usePag;
					pag = pag || ((cr1.getPAG()!=null) && (cr2.getPAG()!=null) && (cr1.getPAG().equals (cr2.getPAG()))); 
					pag = pag || (nullEqualsNull && (cr1.getPAG()==null) && (cr2.getPAG()==null));
					
					boolean doi = !useDOI;
					doi = doi || ((cr1.getDOI()!=null) && (cr2.getDOI()!=null) && (cr1.getDOI().equalsIgnoreCase (cr2.getDOI())));
					doi = doi || (nullEqualsNull && (cr1.getDOI()==null) && (cr2.getDOI()==null));
					
					if (vol && pag && doi) {
						cr1.getCluster().merge(cr2.getCluster());
					}
				}
			});
		});

		// add manual matches
		if (type != Clustering.ClusteringType.INIT) {
			matchResult.get(true).forEach((cr1, pairs) -> {
				StatusBar.get().incProgressbar();
				pairs.forEach((cr2, sim) -> {
					boolean changed = (changeCR == null) || (changeCR.contains(cr1) && changeCR.contains(cr2));
					if (changed && (sim!=null) && (sim==2d)) {
						cr1.getCluster().merge(cr2.getCluster());
					}
				});
			});
		}
		
		
		StatusBar.get().setValue("Clustering done");

		
	}
	
	

	
	
	
	public void addPair (CRType_MM cr1, CRType_MM cr2, double s, boolean isManual) {
		addPair(new CRPair (cr1, cr2, s), isManual, false, null);
	}

	private void addPair (CRPair matchPair, boolean isManual, boolean add, Long timestamp) {

		if (matchPair.cr1==matchPair.cr2) return;
		
		matchResult.get( isManual).putIfAbsent(matchPair.cr1, new HashMap<CRType_MM, Double>());
		matchResult.get(!isManual).putIfAbsent(matchPair.cr1, new HashMap<CRType_MM, Double>());

		// store old value for undo operation of manual mappings
		if ((isManual) && (timestamp!=null)) {
			timestampedPairs.putIfAbsent(timestamp, new ArrayList<CRPair>());
			timestampedPairs.get(timestamp).add(new CRPair(matchPair.cr1, matchPair.cr2, (matchResult.get(isManual)).get(matchPair.cr1).get(matchPair.cr2)));
		}

		// update value
		double v = add ? matchResult.get(isManual).get(matchPair.cr1).getOrDefault(matchPair.cr2, 0d) : 0d;
		matchResult.get(isManual).get(matchPair.cr1).put(matchPair.cr2, (matchPair.s==null) ? null : matchPair.s+v);
	}


	@Override
	public long getNumberOfMatches(boolean manual) {
		return matchResult.get(manual).entrySet().stream().mapToLong( entry -> entry.getValue().size()).sum();
	}


	@Override
	public long getNumberOfClusters() {
		return crTab.getCR().map(cr -> cr.getCluster()).distinct().count();
	}


	@Override
	public Stream<MatchPairGroup> getMatchPairGroups(boolean manual) {
		
		return 
			this.matchResult.get(manual).entrySet().stream()
				.sorted((e1, e2) -> e1.getKey().getID()-e2.getKey().getID())
				.map(e -> {
					MatchPairGroup res = new MatchPairGroup(e.getKey().getID());
					for (Entry<CRType_MM, Double> p: e.getValue().entrySet()) {
						res.addMatch(p.getKey().getID(), p.getValue());
					}
					return res;
				});
	}
	
}
