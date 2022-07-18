package cre.store.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cre.data.type.abs.PubType;




public class PubType_DB extends PubType<CRType_DB> {

	final static CharSequence arraySeparator2 = "\t"; 
	
	final static Collector<? super String, ?, String> joinByNewLine = Collectors.joining("\n");
	final static Function<String, Stream<String>> splitByNewLine = s -> (s==null) ? Stream.empty() : Arrays.stream(s.split("\n"));
	
	public static void addToBatch (PreparedStatement pst, PubType<?> pub) throws SQLException {

		pst.clearParameters();
		
		pst.setInt 		( 1, pub.getID()); 
		pst.setString 	( 2, pub.getPT()); 
		
		if (pub.getAU().count()==0) { pst.setNull( 3, Types.VARCHAR); } else { pst.setString ( 3, pub.getAU().collect(joinByNewLine)); } 
		if (pub.getAF().count()==0) { pst.setNull( 4, Types.VARCHAR); } else { pst.setString ( 4, pub.getAF().collect(joinByNewLine)); } 
		if (pub.getC1().count()==0) { pst.setNull( 5, Types.VARCHAR); } else { pst.setString ( 5, pub.getC1().map(it -> String.join(arraySeparator2, it)).collect(joinByNewLine)); } 
		if (pub.getEM().count()==0) { pst.setNull( 6, Types.VARCHAR); } else { pst.setString ( 6, pub.getEM().collect(joinByNewLine)); } 
		if (pub.getAA().count()==0) { pst.setNull( 7, Types.VARCHAR); } else { pst.setString ( 7, pub.getAA().collect(joinByNewLine)); } 
		
		// if ((pub.getID()==161) || (pub.getID()==447) || (pub.getID()==488)) {
		// 	System.out.println("============== " + pub.getID());
		// 	System.out.println("EM count = " + pub.getEM().count());
		// 	System.out.println("AA count = " + pub.getAA().count());
		// }
		
		pst.setString 	( 8, pub.getTI()); 
		if (pub.getPY()==null) { pst.setNull( 9, Types.INTEGER); } else { pst.setInt ( 9, pub.getPY()); } 
		pst.setString 	(10, pub.getSO()); 
		pst.setString 	(11, pub.getVL()); 
		pst.setString 	(12, pub.getIS()); 
		pst.setString 	(13, pub.getAR()); 
		if (pub.getBP()==null) { pst.setNull(14, Types.INTEGER); } else { pst.setInt (14, pub.getBP()); } 
		if (pub.getEP()==null) { pst.setNull(15, Types.INTEGER); } else { pst.setInt (15, pub.getEP()); } 
		if (pub.getPG()==null) { pst.setNull(16, Types.INTEGER); } else { pst.setInt (16, pub.getPG()); } 
		if (pub.getTC()==null) { pst.setNull(17, Types.INTEGER); } else { pst.setInt (17, pub.getTC()); } 
		pst.setString 	(18, pub.getDI()); 
		pst.setString 	(19, pub.getLI()); 
		pst.setString 	(20, pub.getAB()); 
		pst.setString 	(21, pub.getDE()); 
		pst.setString 	(22, pub.getDT()); 
		pst.setString 	(23, pub.getFS()); 
		pst.setString 	(24, pub.getUT());
		pst.addBatch();
	}
	
	@Override
	public Stream<CRType_DB> getCR(boolean sortById) {
		String order = sortById ? "ORDER BY CR_ID" : "";
		return CRTable_DB.get().getDBStore().selectCR(String.format("WHERE CR_ID IN (SELECT CR_ID FROM PUB_CR WHERE PUB_ID = %d) %s", this.getID(), order));
	}

	@Override
	public int getSizeCR() {
		return CRTable_DB.get().getDBStore().getNumber(String.format("SELECT COUNT(*) FROM PUB_CR WHERE PUB_ID = %d", this.getID()));
	}



}
