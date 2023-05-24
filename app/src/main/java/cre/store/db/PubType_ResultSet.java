package cre.store.db;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;


/**
 * Iterator to generate a stream of PubType_DB from SQL result set
 * @author Andreas Thor
 */

public class PubType_ResultSet implements Iterator<PubType_DB> {
	
	private ResultSet rs; 
	
	public PubType_ResultSet(ResultSet rs) throws IOException, SQLException {
		this.rs = rs;
	}
	
	public void close() throws IOException, SQLException {
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
	public PubType_DB next() {
		try {
			PubType_DB pub = new PubType_DB();
			pub.setID(rs.getInt("PUB_ID"));
			
			pub.setID(rs.getInt("PUB_ID"));
			pub.setPT(rs.getString("PUB_PT"));
			PubType_DB.splitByNewLine.apply(rs.getString("PUB_AU")).forEach(it -> pub.addAU(it));
			PubType_DB.splitByNewLine.apply(rs.getString("PUB_AF")).forEach(it -> pub.addAF(it));
			PubType_DB.splitByNewLine.apply(rs.getString("PUB_C1")).forEach(it -> pub.addC1(it.split(PubType_DB.arraySeparator2.toString(), 2)));
			PubType_DB.splitByNewLine.apply(rs.getString("PUB_EM")).forEach(it -> pub.addEM(it));
			PubType_DB.splitByNewLine.apply(rs.getString("PUB_AA")).forEach(it -> pub.addAA(it));
			pub.setTI(rs.getString("PUB_TI"));
			pub.setPY(rs.getInt("PUB_PY"));		if (rs.wasNull()) pub.setPY(null);
			pub.setSO(rs.getString("PUB_SO"));
			pub.setVL(rs.getString("PUB_VL"));
			pub.setIS(rs.getString("PUB_IS"));
			pub.setAR(rs.getString("PUB_AR"));
			pub.setBP(rs.getInt("PUB_BP"));		if (rs.wasNull()) pub.setBP(null);
			pub.setEP(rs.getInt("PUB_EP"));		if (rs.wasNull()) pub.setEP(null);	
			pub.setPG(rs.getInt("PUB_PG"));		if (rs.wasNull()) pub.setPG(null);
			pub.setTC(rs.getInt("PUB_TC"));		if (rs.wasNull()) pub.setTC(null);
			pub.setDI(rs.getString("PUB_DI"));
			pub.setLI(rs.getString("PUB_LI"));
			pub.setAB(rs.getString("PUB_AB"));
			pub.setDE(rs.getString("PUB_DE"));
			pub.setDT(rs.getString("PUB_DT"));
			pub.setFS(rs.getString("PUB_FS"));
			pub.setUT(rs.getString("PUB_UT"));
			
			return pub;
		} catch (Exception e) {
			return null;
		}
	}
	
	public Iterable<PubType_DB> getIterable () { 
		return () -> this;
	}
	
}