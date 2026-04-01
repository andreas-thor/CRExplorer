package cre.store.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cre.data.type.abs.CRType;
import cre.data.type.abs.CRType.PERCENTAGE;
import cre.data.type.abs.PubType;

class DB_Store { 

	


	private PreparedStatement updateCRIndicators_PrepStmt;
	private int updateCRIndicators_Counter;

	
	private final int BATCH_SIZE_MAX = 50000;
	
	

	
	private Connection dbCon;

	
	
	DB_Store(Connection dbCon) throws SQLException, URISyntaxException, IOException {
		
		
		
		this.dbCon = dbCon;
		this.dbCon.setAutoCommit(false);

		/* create tables */
		if (CRTable_DB.createSchemaOnStartup) {
			Statement stmt = dbCon.createStatement();
			for (String s: Queries.getQuery("crpub", "create_schema")) {
				stmt.execute(s);
			}
			dbCon.commit();
			stmt.close();
		}

		// TODO: Add VACUUM command to reduce file size!

		/* create prepared statements & sql scripts */
		updateCRIndicators_PrepStmt = dbCon.prepareStatement(Queries.getQuery("crpub", "pst_update_cr_indicators").get(0));
		updateCRIndicators_Counter = 0;		
		dbCon.commit();

	}
	


	
	void init () throws SQLException, URISyntaxException, IOException {

		/* delete all rows from all tables */
		Statement stmt = dbCon.createStatement();
		for (String s: Queries.getQuery("crpub", "delete_all")) {
			stmt.execute(s);
		}
		dbCon.commit();

		/* reset batch counters */

		updateCRIndicators_Counter = 0;		
		
	}
	

	


	
	
	
	void updateCRIndicators (int crId, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE)  {
		
		try {
			updateCRIndicators_PrepStmt.clearParameters();
			updateCRIndicators_PrepStmt.setInt		( 1, N_PYEARS); 
			updateCRIndicators_PrepStmt.setDouble 	( 2, PYEAR_PERC); 
			updateCRIndicators_PrepStmt.setDouble 	( 3, PERC_YR); 
			updateCRIndicators_PrepStmt.setDouble 	( 4, PERC_ALL); 
			updateCRIndicators_PrepStmt.setInt		( 5, N_PCT[PERCENTAGE.P50.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		( 6, N_PCT[PERCENTAGE.P75.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		( 7, N_PCT[PERCENTAGE.P90.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		( 8, N_PCT[PERCENTAGE.P99.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		( 9, N_PCT[PERCENTAGE.P999.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(10, N_PCT_AboveAverage[PERCENTAGE.P50.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(11, N_PCT_AboveAverage[PERCENTAGE.P75.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(12, N_PCT_AboveAverage[PERCENTAGE.P90.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(13, N_PCT_AboveAverage[PERCENTAGE.P99.ordinal()]); 
			updateCRIndicators_PrepStmt.setInt		(14, N_PCT_AboveAverage[PERCENTAGE.P999.ordinal()]); 
			updateCRIndicators_PrepStmt.setString	(15, SEQUENCE); 
			updateCRIndicators_PrepStmt.setString	(16, TYPE); 
			updateCRIndicators_PrepStmt.setInt		(17, crId); 
			updateCRIndicators_PrepStmt.addBatch();
			
			if (++updateCRIndicators_Counter>=BATCH_SIZE_MAX) {
				updateCRIndicators_PrepStmt.executeBatch();
				updateCRIndicators_Counter = 0;
				dbCon.commit();
			}	
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	void finishUpdateCRIndicators() {
		try {
			if (updateCRIndicators_Counter>0) {
				updateCRIndicators_PrepStmt.executeBatch();
				updateCRIndicators_Counter = 0;
			}
			dbCon.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	Stream<CRType_DB> selectCR(String where ) {
		
		try {
			return StreamSupport.stream(new CRType_ResultSet(dbCon.prepareStatement("SELECT * FROM CR " + where).executeQuery()).getIterable().spliterator(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Stream<CRType_DB> emptyStr = Stream.of();
			return emptyStr;
		}
	}

	Stream<List<CRType_DB>> selectCRBlock(String blockAttribute, String where) {
		
		try {
			return StreamSupport.stream(new CRType_ResultSet_Block(blockAttribute, dbCon.prepareStatement("SELECT * FROM CR " + where).executeQuery()).getIterable().spliterator(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Stream<List<CRType_DB>> emptyStr = Stream.of();
			return emptyStr;
		}
	}


	
	
	Stream<PubType_DB> selectPub(String where) {
		
		// System.out.println("selectPub");
		try {
			return StreamSupport.stream(new PubType_ResultSet(dbCon.prepareStatement("SELECT * FROM Pub " + where).executeQuery()).getIterable().spliterator(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Stream<PubType_DB> emptyStr = Stream.of();
			return emptyStr;
		}

	}




	

	

	int getNumber (String sql) {
		
		try {
			ResultSet rs = dbCon.createStatement().executeQuery(sql);
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	
}
