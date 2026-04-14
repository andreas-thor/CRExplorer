package cre.data.type.abs;

import java.util.Arrays;
import java.util.stream.Stream;

import cre.data.type.abs.CRType.PERCENTAGE;
import cre.store.db.CRTable_DB;
import cre.store.mm.CRTable_MM;
import cre.ui.CRTableView;

public abstract class CRTable <C extends CRType<P>, P extends PubType<C>> 
	implements Importer, Loader, Remover, Filter, Statistics, Clustering<C>, Indicators {
 
	public static enum COMPARATOR { LT, LTE, EQ, GTE, GT };
	
	

	
	
	public static enum TABLE_IMPL_TYPES { MM, DB }
	public static TABLE_IMPL_TYPES type = TABLE_IMPL_TYPES.MM;

	private boolean duringUpdate;
	private boolean aborted;
	private int npctRange = 1;
	
	private CRChartData chartData;

	
	
	public static CRTable<? extends CRType<?>, ? extends PubType<?>> get() {
		
		switch (type) {
		case MM: return CRTable_MM.get();
		case DB: return CRTable_DB.get();
		default: return null;
		}
	}
	
	
	public abstract void updateData();
	
	
	// public abstract Clustering<C,P> getClustering();
	
	// public abstract ObservableList<? extends CRType<?>> getObservableCRList();

	public abstract CRTableView<? extends CRType<?>> getTableView();


	public CRChartData getChartData() {
		if (chartData == null) {
			chartData = new CRChartData();
		}
		return chartData;
	}
	
	/**
	 * Initialize empty CRTable
	 */
	
	public abstract void init();
	
	public Stream<C> getCR() {
		return getCR(false);
	}

	public abstract C getCRById(Integer id);

	public abstract Stream<C> getCR(boolean sortById);

	/**
	 * 
	 * @param includePubsWithoutCRs default=false
	 * @return
	 */
	public abstract Stream<P> getPub (boolean includePubsWithoutCRs, boolean sortById);
	
	public Stream<P> getPub() {
		return this.getPub(false, false);
	}	

	public Stream<P> getPub (boolean includePubsWithoutCRs) {
		return this.getPub(includePubsWithoutCRs, false);
	};


	
	/*
	 * We additionally store all pubs in allPubs
	 * This is later only used for export (to Scopus, WoS, CSV_Pub) when the user setting "include pubs without CRs" is set
	 */
	
//	public abstract PubType addPub (PubType pub, boolean addCRs);
	

	/**
	 * Methods for importing data files, i.e., publication lists
	 */

	// public abstract void onBeforeImport ();
	// public abstract void addPub (PubType_MM pub);		
	// public abstract void onAfterImport ();
	
	

	

	
	/**
	 * Merge CRs based on clustering
	 */

	
	
	
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	

	
	public boolean isDuringUpdate() {
		return duringUpdate;
	}

	public void setDuringUpdate(boolean duringUpdate) {
		this.duringUpdate = duringUpdate;
	}

	
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}


	public int getNpctRange() {
		return npctRange;
	}

	public void setNpctRange(int npctRange) {
		if ((npctRange>=0) && (this.npctRange != npctRange)) {
			this.npctRange = npctRange;
			onNpctRangeChanged();
		}
	}
	
	protected abstract void onNpctRangeChanged (); 

}

