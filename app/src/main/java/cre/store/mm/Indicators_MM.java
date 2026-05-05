package cre.store.mm;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import cre.CRELogger;
import cre.data.type.abs.CRIndicatorsUpdate;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRType;
import cre.data.type.abs.CRType.PERCENTAGE;
import cre.data.type.abs.Indicators;
import cre.data.type.abs.Statistics.IntRange;

public class Indicators_MM implements Indicators {

    @Override
    public void updateIndicators() {

		long ts2 = System.currentTimeMillis();
		long ms2 = Runtime.getRuntime().totalMemory();
		
        CRTable_MM.get().setDuringUpdate(true); // mutex to avoid chart updates during computation

		CRELogger.get().logInfo("update Data");
		CRELogger.get().logInfo(String.valueOf(System.currentTimeMillis()));
		
		CRELogger.get().logInfo("Compute Ranges in CRTable_MM");
		
		IntRange range_RPY = CRTable_MM.get().getMaxRangeRPY();
		IntRange range_PY  = CRTable_MM.get().getMaxRangePY();

		
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
		
		
		CRTable_MM.get().getCR()
			.filter(cr -> cr.getRPY()!=null)
			.collect(Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toList())))
			.entrySet().stream().parallel()
			.forEach(rpyGroup -> {	
				int rpy = rpyGroup.getKey().intValue();
				List<CRType_MM> crList = rpyGroup.getValue();
				
				computeForAllCRsOfTheSameRPY (rpy, rpy-range_RPY.getMin(), range_PY, NCR_ALL[0], NCR_RPY, crList);
			}
		);		
		
		
		CRTable_MM.get().getChartData().updateChartData(range_RPY, NCR_RPY, CNT_RPY);
		
		CRTable_MM.get().setDuringUpdate(false);

		
		
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
			
			if (cr.getID()==64) System.out.println(rpy);
			
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



	
	protected void computeCRIndicators (int rpyIdx, int crSize, int pySize, int NCR_ALL, int[] NCR_RPY, int[][] NCR_CR_PY, int[] NCR_CR, int[] NCR_CR_all, int[] NPYEARS_CR, int[] NCR_PY, int[] NCR, CRIndicatorsUpdate updateCR) {

		int noPYWithoutCR = 0;	// number of PY where we do not have any Publication citing a CR in RPY
		for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
			if (NCR_PY[pyIdx]==0) noPYWithoutCR++;
		}
		
//		if (noPYWithoutCR>0) {
//			System.out.println(String.format("RPY=%d, PYSize=%d, w/o=%d", rpy, pySize, noPYWithoutCR));
//		}
 
		int[][] borders = this.getPercentileBorders(NCR_CR_PY, crSize, pySize, NCR_PY);

		
		for (int crIdx=0; crIdx<crSize; crIdx++) {
			
	//		System.out.println("CR x=" + x);
	//		final int crIdx = x;
			
			
			int[] NPCT = new int[PERCENTAGE.values().length];
			int[] NPCT_AboveAverage = new int[PERCENTAGE.values().length];
			
			int[] type = new int[11];
			char[] sequence = new char[pySize];
			
			/* just for debugging */
	//		double[] expectedArray = new double[pySize];
	//		double[] zvalueArray  = new double[pySize];
			
			
			for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
				
				double expected = (1.0d*NCR_CR[crIdx]*NCR_PY[pyIdx]/NCR[0]);
				double zvalue = (expected == 0) ? 0 : (NCR_CR_PY[crIdx][pyIdx] - expected) / Math.sqrt(expected);
	
				
				/* just for debugging */
	//			expectedArray[pyIdx] = expected;
	//			zvalueArray[pyIdx] = zvalue;
	//			System.out.println(String.format("CR=%d\tPY=%d\tExpected=%10.2f\tzValue=%10.2f", crIdx, pyIdx, expected, zvalue));
				
				sequence[pyIdx] = (zvalue>1) ? ZValueSymbol.PLUS.label : ((zvalue<-1) ? ZValueSymbol.MINUS.label : ZValueSymbol.ZERO.label);
				
				type[0]  +=                      (zvalue<-1)?0:1;	// # at least average
				type[1]  += ((pyIdx< 3) 		&& (zvalue<-1)) ? 1 : 0;	// # below average in the first 3 py
				type[2]  += ((pyIdx>=3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 4th+ py 
				type[3]  += ((pyIdx< 3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the first 3 py 
				type[4]  += ((pyIdx< 4) 		&& (zvalue<=1)) ? 1 : 0;	// # average or lower in the first 4 py
				type[5]  += ((pyIdx>=4) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 5th+ py 
				type[6]  += ((pySize-pyIdx<=3) 	&& (zvalue<=1)) ? 1: 0;		// # average or lower in the last 3 py
				type[7]  += (NCR_CR_PY[crIdx][pyIdx]>0) ? 1 : 0;			// # no of citing years with at least 1 citation
				type[8]  += ((pyIdx==0) || (sequence[pyIdx-1]==ZValueSymbol.MINUS.label) ||  (sequence[pyIdx]==ZValueSymbol.PLUS.label) || ((sequence[pyIdx-1]==ZValueSymbol.ZERO.label) && (sequence[pyIdx]==ZValueSymbol.ZERO.label))) ? 1:0;
				type[9]  +=                      (zvalue>1)?1:0;			// above average
				type[10] += 1;	// # citing years
				
				for (int b=0; b<PERCENTAGE.values().length; b++) {
					if (borders[pyIdx][b]<NCR_CR_PY[crIdx][pyIdx]) {
						NPCT[b]++;
						if (zvalue>1) {
							NPCT_AboveAverage[b]++;
						}
					}
				}
	
			}
			
			// Sleeping Beauty = Publication which has been cited below average in the first three citing years ("-"; z<-1) at least twice and above average ("+"; z>1) in the following citing years at least once
			boolean sbeauty   = (type[1]>=2) && (type[2]>=1);
			
			// Constant Performer = Publication which has been cited in more than 80% of the citing years at least once. In more than 80% of the citing years it has been cited at least on the average level 
			boolean constant  = ((1.0d*type[0]/type[10])>0.8) && ((1.0d*type[7]/type[10])>0.8);
			
			// Hot Paper = Publication which has been cited above average ("+"; z>1) in the first three years after publication at least twice
			boolean hotpaper  = (type[3]>=2);
			
			// Life cycle = Publication which has been cited in the first four years in at least two years on the average level ("0"; -1<=z<=1) or lower ("-"; z<-1), in at least two years of the following years above average ("+"; z>1), and in the last three years on the average level ("0"; -1<=z<=1) or lower ("-"; z<-1)
			boolean lifecycle = (type[4]>=2) && (type[5]>=2) && (type[6]>1);
			
			StringBuffer typeLabel = new StringBuffer();
			if (sbeauty) 	typeLabel.append (typeLabel.length()>0?" + ":"").append(CRTypes.SB.label);
			if (constant) 	typeLabel.append (typeLabel.length()>0?" + ":"").append(CRTypes.CP.label);
			if (hotpaper) 	typeLabel.append (typeLabel.length()>0?" + ":"").append(CRTypes.HP.label);
			if (lifecycle) 	typeLabel.append (typeLabel.length()>0?" + ":"").append(CRTypes.LC.label);
			
			
			
			updateCR.update(crIdx, NPYEARS_CR[crIdx], ((double)NPYEARS_CR[crIdx]) / (pySize-noPYWithoutCR), ((double)NCR_CR_all[crIdx]) / NCR_RPY[rpyIdx], ((double)NCR_CR_all[crIdx]) / NCR_ALL, 
					NPCT, NPCT_AboveAverage, new String (sequence), typeLabel.toString());
		}	
	}
	
	
	
	private int[][] getPercentileBorders (int[][] NCR_CR_PY, int crSize, int pySize, int[] NCR_PY) {

		int rangeSize_NPCT = CRTable_MM.get().getNpctRange();
		
		
		int[][] borders = new int[pySize][];	// borders (50%, 75%, 90%, 99%, 99.9%) for each PY
		for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
			
			int rangeStart = (pyIdx-rangeSize_NPCT>=0) ? pyIdx-rangeSize_NPCT : 0;
			int rangeEnd = (pyIdx+rangeSize_NPCT<pySize) ? pyIdx+rangeSize_NPCT : pySize-1;
			
			int[] temp = new int[(rangeEnd-rangeStart+1)*crSize];
			
			for (int rIdx=0; rIdx<rangeEnd-rangeStart+1; rIdx++) {
				for (int crIdx=0; crIdx<crSize; crIdx++) {
					temp[rIdx*crSize + crIdx] = NCR_CR_PY[crIdx][rIdx+rangeStart];
				}
			}
			
			Arrays.sort(temp);
			
			borders[pyIdx] = new int[PERCENTAGE.values().length];
			for (PERCENTAGE perc: PERCENTAGE.values()) {
				borders[pyIdx][perc.ordinal()] = temp[Math.max(0, (int) Math.floor(perc.threshold * temp.length)-1)];
			}
		}
		
		return borders;
		
	}
	
		


}
