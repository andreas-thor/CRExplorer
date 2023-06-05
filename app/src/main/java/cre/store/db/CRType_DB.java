package cre.store.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Stream;

import cre.data.type.abs.CRType;

class CRType_DB extends CRType<PubType_DB> {

	private int N_CR;
	private int c1;
	private int c2;
	private int clusterSize;
	
	private String blockingkey;
	private int sortOrder;
	
	/**
	 * Add cr to the batch of pst that eventually inserts the cr into the database
	 * @param pst
	 * @param cr
	 * @param pubId
	 * @throws SQLException
	 */
	
	static void addToBatch (PreparedStatement pst, CRType<?> cr, int pubId) throws SQLException {
		
		pst.clearParameters();
		pst.setInt		(1, cr.getID());
		pst.setString	(2, cr.getCR());
		
		if (cr.getRPY() == null) {
			pst.setNull	(3, java.sql.Types.INTEGER);
		} else {
			pst.setInt 	(3, cr.getRPY());
		}
		
		pst.setString	( 4, cr.getAU());
		pst.setString	( 5, cr.getAU_L());
		pst.setString	( 6, cr.getAU_F());
		pst.setString	( 7, cr.getAU_A());
		pst.setString	( 8, cr.getTI());
		pst.setString	( 9, cr.getJ());
		pst.setString	(10, cr.getJ_N());
		pst.setString	(11, cr.getJ_S());
		pst.setString	(12, cr.getVOL());
		pst.setString	(13, cr.getPAG());
		pst.setString	(14, cr.getDOI());
		pst.setInt		(15, cr.getClusterC1());
		pst.setInt		(16, cr.getClusterC2());
		pst.setInt		(17, cr.getClusterSize());
		pst.setBoolean	(18, cr.getVI());
		pst.setString	(19, cr.getFormatType().toString());
		pst.setInt		(20, pubId);
		pst.addBatch();
	}
	

	@Override
	public int getN_CR() {
		return this.N_CR;
	}

	@Override
	public int getClusterC1() {
		return c1;
	}

	@Override
	public int getClusterC2() {
		return c2;
	}
	
	@Override
	public int getClusterSize() {
		return clusterSize;
	}
	
	String getBlockingkey() {
		return blockingkey;
	}

	void setBlockingkey(String blockingkey) {
		this.blockingkey = blockingkey;
	}

	int getSortOrder() {
		return sortOrder;
	}

	void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	void setN_CR (int N_CR) {
		this.N_CR = N_CR;
	}

	void setCluster (int c1, int c2, int clusterSize) {
		this.c1 = c1;
		this.c2 = c2;
		this.clusterSize = clusterSize;
	}

	@Override
	public Stream<PubType_DB> getPub() {
		return CRTable_DB.get().getDBStore().selectPub(String.format("WHERE Pub_ID IN (SELECT PUB_ID FROM PUB_CR WHERE CR_ID = %d)", this.getID()));
	}
	
}
