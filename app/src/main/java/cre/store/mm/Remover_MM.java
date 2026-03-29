package cre.store.mm;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import cre.data.type.abs.Remover;
import cre.data.type.abs.Statistics.IntRange;

/**
 * Removes Cited References based on different criterias
 */

public class Remover_MM implements Remover {
    
    private CRTable_MM crTab;

	public Remover_MM (CRTable_MM crTab) {
        this.crTab = crTab;
    }


	
	
	/**
	 * Remove list of CRs
	 * @param toDelete list of CRs to be deleted
	 */
	@Override
	public void removeCR (List<Integer> toDelete) {
		this.crTab.getCR().forEach(cr -> cr.setFlag(false));
		toDelete.forEach(crId -> this.crTab.getCRById(crId).setFlag(true));
		this.crTab.removeCR(cr -> cr.isFlag());
	}
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	@Override
	public void retainCR (List<Integer> toRetain) {
		this.crTab.getCR().forEach(cr -> cr.setFlag(true));
		toRetain.forEach(crId -> crTab.getCRById(crId).setFlag(false));
		this.crTab.removeCR(cr -> cr.isFlag());
	}
	
	
	/**
	 * Remove all CRs without year (RPY)
	 */
	@Override
	public void removeCRWithoutYear () {
		this.crTab.removeCR (cr -> cr.getRPY() == null);
	}

	
	/**
	 * Remove all CRS within a given RPY range
	 * @param range
	 */
	@Override
	public void removeCRByYear (IntRange range) {
		this.crTab.removeCR (cr -> ((cr.getRPY()!=null) && (range.getMin() <= cr.getRPY()) && (cr.getRPY() <= range.getMax())));
	}

	
	/**
	 * Remove all CRs within a given N_CR range
	 * @param range
	 */
	@Override
	public void removeCRByN_CR(IntRange range) {
		this.crTab.removeCR (cr -> (range.getMin() <= cr.getN_CR()) && (cr.getN_CR() <= range.getMax()));
	}
	
	
	/**
	 * Remove all CRs < / <= / = / >= / > PERC_YR
	 * @param comp comparator (as string); TODO: ENUMERATION
	 * @param threshold
	 */
	@Override
	public void removeCRByPERC_YR (String comp, double threshold) {
		switch (comp) {
			case "<" : this.crTab.removeCR (cr -> cr.getPERC_YR() <  threshold); break;
			case "<=": this.crTab.removeCR (cr -> cr.getPERC_YR() <= threshold); break;
			case "=" : this.crTab.removeCR (cr -> cr.getPERC_YR() == threshold); break;
			case ">=": this.crTab.removeCR (cr -> cr.getPERC_YR() >= threshold); break;
			case ">" : this.crTab.removeCR (cr -> cr.getPERC_YR() >  threshold); break;
		}
	}
	
	
	/**
	 * Remove all citing publications, that do *not* reference any of the given CRs 
	 * @param selCR list of CRs
	 */
	@Override
	public void removePubByCR (List<Integer> selCR) {
		selCR.stream()
			.map(crId -> crTab.getCRById(crId))
			.flatMap (cr -> cr.getPub())
			.forEach(pub -> pub.setFlag(true));

		removePub (pub -> !pub.isFlag());
		this.crTab.getPub().forEach(pub -> pub.setFlag(false));
		
//		removePub (pub -> !selCR.stream().flatMap (cr -> cr.getPub()).distinct().collect(Collectors.toList()).contains(pub));
	}
	
	
	
	private void removePub (Predicate<PubType_MM> cond) {
		this.crTab.getPub().filter(cond).collect(Collectors.toList()).forEach(pub -> pub.removeAllCRs(true));
		this.crTab.removeCR(cr -> cr.getN_CR()==0);
	}
	
	/**
	 * Retail all citing publications within given citiny year (PY) range, 
	 * i.e., remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range
	 */
	@Override
	public void retainPubByCitingYear (IntRange range) {
		removePub (pub -> (pub.getPY()==null) || (range.getMin() > pub.getPY()) || (pub.getPY() > range.getMax()));
	}
	
	    
}
