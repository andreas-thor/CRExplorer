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


	private Integer readInt (String name, Integer defaultValue) throws SQLException   {
		int i = rs.getInt(name);
		return rs.wasNull() ? defaultValue : Integer.valueOf(i);
	}

	private Double readDouble (String name, Double defaultValue) throws SQLException   {
		double d = rs.getDouble(name);
		return rs.wasNull() ? defaultValue : Double.valueOf(d);
	}

	private String readString (String name, String defaultValue) throws SQLException   {
		String s = rs.getString(name);
		return rs.wasNull() ? defaultValue : s;
	}

	private Boolean readBoolean (String name, Boolean defaultValue) throws SQLException   {
		boolean b = rs.getBoolean(name);
		return rs.wasNull() ? defaultValue : Boolean.valueOf(b);
	}

	



	@Override
	public CRType_DB next() {
		try {
			CRType_DB cr = new CRType_DB();
			cr.setID(readInt("CR_ID", null));
			cr.setCR(readString("CR_CR",null));
			cr.setRPY(readInt("CR_RPY", null));
			cr.setN_CR (readInt("CR_N_CR", null));
			cr.setAU(readString("CR_AU",null));
			cr.setAU_L(readString("CR_AU_L",null));
			cr.setAU_F(readString("CR_AU_F",null));
			cr.setAU_A(readString("CR_AU_A",null));
			cr.setTI(readString("CR_TI",null));
			cr.setJ(readString("CR_J",null));
			cr.setJ_N(readString("CR_J_N",null));
			cr.setJ_S(readString("CR_J_S",null));
			cr.setVOL(readString("CR_VOL",null));
			cr.setPAG(readString("CR_PAG",null));
            cr.setDOI(readString("CR_DOI",null));
            cr.setCluster(readInt("CR_ClusterId1", null), readInt("CR_ClusterId2", null), readInt("CR_ClusterSize", null));
			cr.setVI(readBoolean("CR_VI", true));
			cr.setFormatType(FORMATTYPE.valueOf(readString("CR_Format",null)));

			cr.setPERC_YR(readDouble("CR_PERC_YR", 0d));
			cr.setPERC_ALL(readDouble("CR_PERC_ALL", 0d));
			cr.setN_PYEARS(readInt("CR_N_PYEARS", 0));
			cr.setPYEAR_PERC(readDouble("CR_PYEAR_PERC", 0d));

			cr.setN_PCT(new int[]{ 
				readInt("CR_N_PCT_P50", 0), 
				readInt("CR_N_PCT_P75", 0), 
				readInt("CR_N_PCT_P90", 0),
				readInt("CR_N_PCT_P99", 0),
				readInt("CR_N_PCT_P999", 0) 
			});
			
			cr.setN_PCT_AboveAverage(new int[]{ 
				readInt("CR_N_PCT_AboveAverage_P50", 0),
				readInt("CR_N_PCT_AboveAverage_P75", 0), 
				readInt("CR_N_PCT_AboveAverage_P90", 0),
				readInt("CR_N_PCT_AboveAverage_P99", 0), 
				readInt("CR_N_PCT_AboveAverage_P999", 0) 
			});
			
			cr.setSEQUENCE(readString("CR_SEQUENCE",null));
			cr.setTYPE(readString("CR_TYPE",null));

			cr.setBlockingkey(readString("CR_BLOCKINGKEY",null));

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