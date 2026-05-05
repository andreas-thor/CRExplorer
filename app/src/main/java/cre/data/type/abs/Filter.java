package cre.data.type.abs;

import java.util.List;

import cre.data.type.abs.Statistics.IntRange;

public interface Filter {
    
    
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public abstract void filterByYear (IntRange range);
	
	public abstract void filterByCluster (List<Integer> sel);
	
	public abstract void setShowNull (boolean showNull);
	
	public abstract void showAll();
	

    
}
