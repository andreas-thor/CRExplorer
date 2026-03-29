package cre.store.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import cre.data.type.abs.Filter;
import cre.data.type.abs.Statistics.IntRange;

public class Filter_DB implements Filter {
 
    
    private Connection dbCon;
	private boolean showNull;    

    public Filter_DB(Connection dbCon) {
        this.dbCon = dbCon;
        this.showNull = true;
    }


	
	

	@Override
	public void filterByYear(IntRange range) {
		String newValue = String.format("(NOT(CR_RPY IS NULL) AND (%d<=CR_RPY) AND (%d>=CR_RPY)) %s", range.getMin(), range.getMax(), this.showNull?" OR (CR_RPY IS NULL)":"");  
		updateCR_VI(newValue, null);
	}

	@Override
	public void filterByCluster(List<Integer> sel) {
		// String clList = sel.stream().map(cr -> "(" + cr.getClusterC1() + "," + cr.getClusterC2() + ")").collect (Collectors.joining( "," ));
		String crList = sel.stream().map(crId -> String.valueOf(crId)).collect (Collectors.joining( "," ));
		updateCR_VI(String.format("(CR_Clusterid1, CR_Clusterid2) in (SELECT CR_Clusterid1, CR_Clusterid2 FROM CR WHERE CR_ID IN (%s))", crList), null);
	}

	@Override
	public void setShowNull(boolean showNull) {
		updateCR_VI(showNull?"1":"0", "CR_RPY IS NULL");
		this.showNull = showNull;
	}

	@Override
	public void showAll() {
		updateCR_VI("1", null);
		this.showNull = true;		
	}


	void updateCR_VI (String newValue, String predicate) {
		try {
			Statement stmt = dbCon.createStatement();
			stmt.executeUpdate(String.format ("UPDATE CR SET CR_VI = %s %s", newValue, (predicate==null)?"":"WHERE " + predicate)); 
			dbCon.commit();
			CRTable_DB.get().updateObservableCRList();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	    
}
