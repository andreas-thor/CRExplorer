package cre.store.db;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import cre.data.type.abs.CRType.FORMATTYPE;

/**
 * Iterator to generate a stream of CRType_DB from SQL result set
 * @author Andreas
 *
 */
class CRType_ResultSet implements Iterator<CRType_DB> {

	private ResultSet rs; 
	
	CRType_ResultSet(ResultSet rs) throws IOException, SQLException {
		this.rs = rs;
	}
	
	void close() throws IOException, SQLException {
		this.rs.close();
	}
	
	@Override
	public boolean hasNext() {
		try {
			return this.rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public CRType_DB next() {
		try {
			CRType_DB cr = new CRType_DB();
			cr.setID(rs.getInt("CR_ID"));
			cr.setCR(rs.getString("CR_CR"));
			cr.setRPY(rs.getInt("CR_RPY"));
			if (rs.wasNull()) {
				cr.setRPY(null);
			}
			cr.setN_CR (rs.getInt("CR_N_CR"));
			cr.setAU(rs.getString("CR_AU"));
			cr.setAU_L(rs.getString("CR_AU_L"));
			cr.setAU_F(rs.getString("CR_AU_F"));
			cr.setAU_A(rs.getString("CR_AU_A"));
			cr.setTI(rs.getString("CR_TI"));
			cr.setJ(rs.getString("CR_J"));
			cr.setJ_N(rs.getString("CR_J_N"));
			cr.setJ_S(rs.getString("CR_J_S"));
			cr.setVOL(rs.getString("CR_VOL"));
			cr.setPAG(rs.getString("CR_PAG"));
            cr.setDOI(rs.getString("CR_DOI"));
            cr.setCluster(rs.getInt("CR_ClusterId1"), rs.getInt("CR_ClusterId2"), rs.getInt("CR_ClusterSize"));
			cr.setVI(rs.getBoolean("CR_VI"));
			cr.setFormatType(FORMATTYPE.valueOf(rs.getString("CR_Format")));

			cr.setPERC_YR(rs.getDouble("CR_PERC_YR"));
			cr.setPERC_ALL(rs.getDouble("CR_PERC_ALL"));
			cr.setN_PYEARS(rs.getInt("CR_N_PYEARS"));
			cr.setPYEAR_PERC(rs.getDouble("CR_PYEAR_PERC"));

			int[] N_PCT = { rs.getInt("CR_N_PCT_P50"), rs.getInt("CR_N_PCT_P75"), rs.getInt("CR_N_PCT_P90"), rs.getInt("CR_N_PCT_P99"), rs.getInt("CR_N_PCT_P999") };
			if (!rs.wasNull()) {
				cr.setN_PCT(N_PCT);
			}
			int[] N_PCT_AboveAverage = { rs.getInt("CR_N_PCT_AboveAverage_P50"),
					rs.getInt("CR_N_PCT_AboveAverage_P75"), rs.getInt("CR_N_PCT_AboveAverage_P90"),
					rs.getInt("CR_N_PCT_AboveAverage_P99"), rs.getInt("CR_N_PCT_AboveAverage_P999") };
			if (!rs.wasNull()) {
				cr.setN_PCT_AboveAverage(N_PCT_AboveAverage);
			}

			cr.setSEQUENCE(rs.getString("CR_SEQUENCE"));
			cr.setTYPE(rs.getString("CR_TYPE"));

			cr.setBlockingkey(rs.getString("CR_BLOCKINGKEY"));

			return cr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Iterable<CRType_DB> getIterable () { 
		return () -> this;
	}
	
}