package cre.store.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cre.Timestamp;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.Indicators;
import cre.data.type.abs.Statistics.IntRange;

public class Indicators_DB implements Indicators{

	private Connection dbCon;
	
	public Indicators_DB(Connection dbCon) {
		this.dbCon = dbCon;
	}

    @Override
    public void updateIndicators() {
        try {
			
			Timestamp.ts("updateData start");

			String unionNPTCRange = "";
			String unionParam = Queries.getQuery("Indicators_DB", "npctrange").get(0);
			for (int i=1; i<=CRTable.get().getNpctRange(); i++) {
				unionNPTCRange += " " + String.format(unionParam, i);
			}

			Statement stmt = dbCon.createStatement();
			String s = String.format(Queries.getQuery("Indicators_DB", "npyears").get(0), unionNPTCRange);
			stmt.execute (s);
			dbCon.commit();	
			stmt.close();



			
			
			// CRTable_DB.get().getChartData().updateChartData(range_RPY, NCR_RPY, CNT_RPY);

			Timestamp.ts("updateData ende");

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

/*
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
    */
}
