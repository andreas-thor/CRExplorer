package cre.store.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import cre.data.type.abs.CRType;
import cre.data.type.abs.PubType;
import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;

public class Importer_DB {
    
    private Connection dbCon;
    private int numberOfPubs;

	private PreparedStatement insertCR_PrepStmt;
	private int insertCR_Counter;
	private int testcounter;
	
	private PreparedStatement insertPub_PrepStmt;
	private int insertPub_Counter;    

	private final int BATCH_SIZE_MAX = 50000;


    public Importer_DB(Connection dbCon) throws SQLException {
        this.dbCon = dbCon;

		/* create prepared statements & sql scripts */
		insertCR_PrepStmt = dbCon.prepareStatement(Queries.getQuery("Importer_DB", "pst_insert_cr").get(0)); 
		insertPub_PrepStmt = dbCon.prepareStatement(Queries.getQuery("Importer_DB", "pst_insert_pub").get(0));

    }


    public void onBeforeImport () {
		this.insertCR_Counter = 0;
		this.insertPub_Counter = 0;        

        this.numberOfPubs = 0;
        this.testcounter = 0;   
		
		try {
			Statement stmt = dbCon.createStatement();
			for (String s: Queries.getQuery("Importer_DB", "before_import")) {
				stmt.execute(s);
			}
			stmt.close();
			dbCon.commit();		
		} catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();			
		}
    }




	
	public void addPub(PubType_MM pub) {

		this.numberOfPubs++;
		pub.setID(this.numberOfPubs);
		
		try {

			insertPub(pub);
			for(CRType_MM cr: pub.getCR().collect(Collectors.toSet())) {
				
				try {
					insertCR(cr, pub.getID(), pub.getPY());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		
		
	}    


	void insertCR (CRType<?> cr, int pubId, Integer pubYear) throws SQLException {
		
		CRType_DB.addToBatch(insertCR_PrepStmt, cr, pubId, pubYear);
		
		if (++insertCR_Counter>=BATCH_SIZE_MAX) {

			testcounter += insertCR_Counter;
			System.out.println(testcounter);

			insertCR_PrepStmt.executeBatch();
			insertCR_Counter = 0;
			dbCon.commit();
			

		}		
	}
	
	void insertPub (PubType<?> pub) throws SQLException {
		
		PubType_DB.addToBatch(insertPub_PrepStmt, pub);
		
		if (++insertPub_Counter>=BATCH_SIZE_MAX) {
			insertPub_PrepStmt.executeBatch();
			insertPub_Counter = 0;
			dbCon.commit();
		}	
	}    



	public void onAfterImport () {
    	// we may have some insert statements in the batch to be executed after loading
		try {

            if (insertCR_Counter>0) {
                insertCR_PrepStmt.executeBatch();
                insertCR_Counter = 0;
            }
            
            if (insertPub_Counter>0) {
                insertPub_PrepStmt.executeBatch();
                insertPub_Counter = 0;
            }	
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		// wrap-up import process
		try {
			Statement stmt = dbCon.createStatement();
			for (String s: Queries.getQuery("Importer_DB", "after_import")) {
				System.out.println(s);
				stmt.execute(s);
			}
			stmt.close();
			dbCon.commit();		

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		System.out.println("...done!");        

	}


}
