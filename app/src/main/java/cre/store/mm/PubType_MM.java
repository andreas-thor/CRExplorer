package cre.store.mm;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import cre.data.type.abs.PubType;
import cre.data.type.abs.Statistics.IntRange;

public class PubType_MM extends PubType<CRType_MM> {


	
	private Set<CRType_MM> crList;

	
	
	public PubType_MM() {
		super();
		
		crList = new LinkedHashSet<CRType_MM>();
		setFlag(false);
	}


	@Override
	public Stream<CRType_MM> getCR(boolean sortById) {
		return sortById ? crList.stream().sorted() : crList.stream();
	}

	@Override
	public int getSizeCR() {
		return crList.size();
	}

	/**
	 * Adds a CR to a PUB
	 * 
	 * @param cr
	 *            to be added
	 * @param inverse
	 *            true, if this PUB should also be added to the publist of the
	 *            CR
	 */

	public void addCR(CRType_MM cr, boolean inverse) {
		if (cr == null)
			return;
		if (inverse) {
			cr.addPub(this, false);
		}
		this.crList.add(cr);
	}

	/**
	 * Removes a CR from a PUB
	 * 
	 * @param cr
	 *            to be removed
	 * @param inverse
	 *            true, if this PUB should also be removed from the publist of
	 *            the CR
	 */
	
	public boolean removeCR(CRType_MM cr, boolean inverse) {
		if (cr == null)
			return false;
		if (inverse) {
			cr.removePub(this, false);
		}
		return this.crList.remove(cr);
	}

	public void removeCRByYear (IntRange range, boolean keepCRsWithoutYear, boolean inverse) {
		
		
		this.crList.removeIf(cr -> {
			
			boolean toBeRemoved = false;
			if (cr.getRPY()==null) {
				toBeRemoved = !keepCRsWithoutYear;
			} else {
				int rpy = cr.getRPY().intValue();
				if ((range.getMin()!=IntRange.NONE) && (range.getMin()>rpy)) toBeRemoved = true;
				if ((range.getMax()!=IntRange.NONE) && (range.getMax()<rpy)) toBeRemoved = true;
			}
			
			if (toBeRemoved && inverse) {
				cr.removePub(this, false);
			}
			
			if (toBeRemoved) {
				// System.out.println("DEBUG: tobeRemoved");
			}
			
			return toBeRemoved;	
		});
		
	}
	
	public void removeCRByProbability (float probability, int offset, AtomicLong noToImportCRs, AtomicLong noAvailableCRs, AtomicInteger currentOffset) {
		
		this.crList.removeIf(cr -> {
			
			boolean remove = true;
			
			if ((noToImportCRs.get()>0) && (probability*noAvailableCRs.get() <= 1.0f*noToImportCRs.get())) {
			
				if (currentOffset.get()==offset) {
					noToImportCRs.decrementAndGet();
					currentOffset.set(0);
					remove = false;
				} else {
					currentOffset.incrementAndGet();
				}
			}
			
			noAvailableCRs.decrementAndGet();
			return remove;
			
		});
	}

	public void removeAllCRs(boolean inverse) {
		if (inverse) {
			crList.forEach(cr -> cr.removePub(this, false));
		}
		this.crList.clear();
	}

	
	

}
