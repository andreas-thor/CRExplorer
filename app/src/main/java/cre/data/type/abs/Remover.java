package cre.data.type.abs;

import java.util.List;

import cre.data.type.abs.Statistics.IntRange;

public interface Remover {
    

	/**
	 * Remove list of CRs
	 * @param toDelete list of CRs to be deleted
	 */
	public abstract void removeCR (List<Integer> toDelete);
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	public abstract void retainCR (List<Integer> toRetain);
	
	
	/**
	 * Remove all CRs without year (RPY)
	 */
	public abstract void removeCRWithoutYear ();

	
	/**
	 * Remove all CRs within a given RPY range
	 * @param range range of cited years RPY 
	 */
	public abstract void removeCRByYear (IntRange range);

	
	/**
	 * Remove all CRs within a given N_CR range
	 * @param range range ov N_CRs
	 */
	public abstract void removeCRByN_CR(IntRange range);
	
	
	/**
	 * Remove all CRs < / <= / = / >= / > PERC_YR
	 * @param comp comparator (as string); TODO: ENUMERATION
	 * @param threshold threshold 
	 */
	
	public abstract void removeCRByPERC_YR (String comp, double threshold);
	
	
	/**
	 * Remove all citing publications, that do *not* reference any of the given CRs 
	 * @param selCR list of CRs
	 */
	public abstract void removePubByCR (List<Integer> selCR);
	
	
	
	
	/**
	 * Retain all citing publications within given citiny year (PY) range, 
	 * i.e., remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range citing year (PY) range
	 */
	public abstract void retainPubByCitingYear (IntRange range);

    
}
