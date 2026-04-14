package cre.store.mm;

import java.util.List;

import cre.data.type.abs.Filter;
import cre.data.type.abs.Statistics.IntRange;

public class Filter_MM implements Filter {
    
	private CRTable_MM crTab;
	private boolean showNull;

	public Filter_MM (CRTable_MM crTab) {
        this.crTab = crTab;
		this.showNull = true;
    }

    /**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	@Override
	public void filterByYear (IntRange range) {
		if (! crTab.isDuringUpdate()) {
			crTab.getCR().forEach ( it -> { it.setVI(((it.getRPY()!=null) && (range.getMin()<=it.getRPY()) && (range.getMax()>=it.getRPY())) || ((it.getRPY()==null) && (this.showNull))); });
		}
	}
	

	@Override
	public void filterByCluster (List<Integer> sel) {
		if (! crTab.isDuringUpdate()) {
			crTab.getCR().forEach(cr -> cr.setVI(false));
			sel.stream()
				.map(id -> crTab.getCRById(id))
				.map(cr -> cr.getCluster())
				.flatMap(cluster -> cluster.getCR())
				.forEach( cr -> cr.setVI(true) );
		}
	}
	
	

	
	@Override
	public void setShowNull (boolean showNull) {
		this.showNull = showNull;
		crTab.getCR().forEach ( cr -> { if (cr.getRPY() == null) cr.setVI(showNull);  });
	}
	
	@Override
	public void showAll() {
		this.showNull = true;
		crTab.getCR().forEach ( cr -> cr.setVI(true) );
	}




}
