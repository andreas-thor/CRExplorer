package cre.format.cre;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.json.Json;
import javax.json.stream.JsonParser;

import cre.Exceptions.AbortedException;
import cre.Exceptions.FileTooLargeException;
import cre.Exceptions.UnsupportedFileFormatException;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.Clustering;
import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;
import cre.ui.statusbar.StatusBar;

/**
 * Generic reader for CRE files
 * CRE files are zip files that contain three json files
 * - crdata.json: array of CRs
 * - pubdata.json: array of Pubs
 * - crmatch.json (optional): matching pairs of CRs (both, manually and automatically generated)
 * Reader calls cal-back functions (see abstract methods)
 */

public class CREReader {

	// public abstract void onBeforeLoad();
	// public abstract void onAfterLoad();

	// public abstract void onNewCR(CRType_MM cr);
	// public abstract void onNewPub(PubType_MM pub, List<Integer> crIds);
	// public abstract void onNewMatchPair(int crId1, int crId2, double sim, boolean isManual);
	
	
	/**
	 * if loadMutlipleFiles == TRUE: 
	 * 	load is executed multiple times (i.e., multiple CRE files are loaded and unified)
	 *  Ids (both, for CRs and Pubs) are re-adjusted
	 * @param file
	 * @param loadMutlipleFiles	true, if  -->
	 * @throws OutOfMemoryError
	 * @throws Exception 
	 */
	public static void load (File file) throws OutOfMemoryError, RuntimeException {
		
		try {
			CRTable.get().init();

			CRTable.get().getLoader().onBeforeLoad();

			ZipEntry entry = null;
			ZipFile zipFile = new ZipFile(file);
			
			entry = zipFile.getEntry("crdata.json");
			if (entry != null) {
				StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file crdata ...");
				loadCRData(zipFile.getInputStream(entry));
				StatusBar.get().setValue("Loading CRE file crdata done");
			} else {
				zipFile.close();
				throw new Exception ("Could not find crdata.");
			}
			
			
			entry = zipFile.getEntry("pubdata.json");
			if (entry != null) {
				StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file pubdata ...");
				loadPubData(zipFile.getInputStream(entry));
				StatusBar.get().setValue("Loading CRE file pubdata done");
			} else {
				zipFile.close();
				throw new Exception ("Could not find pubdata.");
			}			
			
			// crmatch data is optional
			entry = zipFile.getEntry("crmatch.json");
			if (entry != null) {
				StatusBar.get().initProgressbar(entry.getSize(), "Loading CRE file crmatch ...");
				loadCRMatchData(zipFile.getInputStream(entry));
				StatusBar.get().setValue("Loading CRE file crmatch done");
			}
				
			zipFile.close();

			CRTable.get().getLoader().onAfterLoad();

			CRTable.get().updateData();
			CRTable.get().getClustering().updateClustering(Clustering.ClusteringType.INIT, null, Clustering.min_threshold, false, false, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}



	/**
	 * 
	 * @param in
	 * @param checkForDuplicates
	 * @return Map: internal CRID => CR
	 * @throws UnsupportedFileFormatException
	 * @throws FileTooLargeException
	 * @throws AbortedException
	 * @throws OutOfMemoryError
	 * @throws IOException
	 */
	private static void loadCRData (InputStream in) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError, IOException {
		
		JsonParser parser = Json.createParser(in);
		// System.out.println(parser.hasNext());
		CRType_MM cr = null;
		String key = "";
		while (!CRTable.get().isAborted() && parser.hasNext()) {
			
			switch (parser.next()) {
			case START_OBJECT: 	cr = new CRType_MM(); break; 
			case END_OBJECT: 	
				CRTable.get().getLoader().onNewCR(cr);
				break;
			case KEY_NAME:		key = parser.getString(); break;
			case VALUE_STRING: 
				switch (key) {
				case "CR": 		cr.setCR(parser.getString()); break;
				case "AU": 		cr.setAU(parser.getString()); break;
				case "AU_F": 	cr.setAU_F(parser.getString()); break;
				case "AU_L": 	cr.setAU_L(parser.getString()); break;
				case "AU_A": 	cr.setAU_A(parser.getString()); break;
				case "TI": 		cr.setTI(parser.getString()); break;
				case "J": 		cr.setJ(parser.getString()); break;
				case "J_N": 	cr.setJ_N(parser.getString()); break;
				case "J_S": 	cr.setJ_S(parser.getString()); break;
				case "PAG": 	cr.setPAG(parser.getString()); break;
				case "VOL": 	cr.setVOL(parser.getString()); break;
				case "DOI": 	cr.setDOI(parser.getString()); break;
				case "CID2": 	/* cr.setCID2(parser.getString()); */ break; 
				default: System.out.println("CRDATA.json >> Unknow Key with String Value: " + key); 
				}
				break;
			case VALUE_NUMBER:
				switch (key) {
				case "ID": 		cr.setID(parser.getInt()); break;
				case "N_CR": 	/*cr.setN_CR(parser.getInt());*/ break;
				case "RPY": 	cr.setRPY(parser.getInt()); break;
				case "CID_S": 	/*cr.setCID_S(parser.getInt()); */ break;
				case "VI": 		cr.setVI(parser.getInt()==1); break;
				case "CO": 		cr.setCO(parser.getInt()); break;
				case "type": 	cr.setFormatType (parser.getInt()); break;	// LEGACY: Type were enumerated 1,2,...; 0 = Unknown
				default: System.out.println("CRDATA.json >> Unknow Key with Number Value: " + key); 
				}
				break;
			default:break;  
			}
		
			StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
		}		
	}

	
	

	private static void loadPubData (InputStream in) {
		
		JsonParser parser = Json.createParser(in);
		PubType_MM pub = null;
		List<String> C1List = null;
		
		List<Integer> crIds = null; 
		
		int arrayLevel = 0;
		String key = "";
		while (!CRTable.get().isAborted() && parser.hasNext()) {
			
			switch (parser.next()) {
			case START_OBJECT:
				pub = new PubType_MM();
				crIds = new ArrayList<Integer>();
				break; 
			case END_OBJECT: 	
				CRTable.get().getLoader().onNewPub (pub, crIds);
				break;
			case KEY_NAME:		
				key = parser.getString(); 
				break;
			case START_ARRAY:	
				arrayLevel++;
				switch (arrayLevel) {
				case 2:
					switch (key) {
					case "AU":	break;
					case "AF":	break;
					case "C1":	break;	
					case "EM":	break;
					case "AA":	break;
					}
					break;
				case 3:			C1List = new ArrayList<String>(); break;
				}
				break;
			case END_ARRAY: 
				if ((arrayLevel == 3) && (C1List != null)) pub.addC1((String[]) C1List.toArray(new String[C1List.size()])); 
				arrayLevel--;
				break;
			case VALUE_STRING: 
				switch (arrayLevel) {
				case 1:
					switch (key) {
					case "PT": 	pub.setPT(parser.getString()); break;
					case "TI": 	pub.setTI(parser.getString()); break;
					case "SO": 	pub.setSO(parser.getString()); break;
					case "VL": 	pub.setVL(parser.getString()); break;
					case "IS": 	pub.setIS(parser.getString()); break;
					case "AR": 	pub.setAR(parser.getString()); break;
					case "DI": 	pub.setDI(parser.getString()); break;
					case "LI": 	pub.setLI(parser.getString()); break;
					case "AB": 	pub.setAB(parser.getString()); break;
					case "DE": 	pub.setDE(parser.getString()); break;
					case "DT": 	pub.setDT(parser.getString()); break;
					case "FS": 	pub.setFS(parser.getString()); break;
					case "UT": 	pub.setUT(parser.getString()); break;
					default: System.out.println("PUBDATA.json >> Unknow Key with String Value: " + key); 
					}
					break;
					
				case 2:
					switch (key) {
					case "AU":	pub.addAU(parser.getString()); break;
					case "AF":	pub.addAF(parser.getString()); break;
					case "EM":	pub.addEM(parser.getString()); break;
					case "AA":	pub.addAA(parser.getString()); break;
					default: System.out.println("PUBDATA.json >> Unknow Key with String Value: " + key); 
					}
					break;
				case 3: 
					C1List.add(parser.getString()); break;
				}
				break;
			case VALUE_NUMBER:
				switch (key) {
				case "PY": 	pub.setPY(parser.getInt()); break;
				case "BP": 	pub.setBP(parser.getInt()); break;
				case "EP": 	pub.setEP(parser.getInt()); break;
				case "PG": 	pub.setPG(parser.getInt()); break;
				case "TC": 	pub.setTC(parser.getInt()); break;
				case "CRLISTID":	
					crIds.add(parser.getInt());	
					break;
				default: System.out.println("PUBDATA.json >> Unknow Key with Number Value: " + key); 
				}
				break;
			default:
				System.out.println("DEFAULT");

				break;
			}
			StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
		}		
	}
	
	
	



	private static void loadCRMatchData (InputStream in) {

		JsonParser parser = Json.createParser(in);
		
		boolean isManual = false;
		int id1 = 0, id2 = 0;
		int level = 0;
		
		while (!CRTable.get().isAborted() && parser.hasNext()) {
			switch (parser.next()) {
			case START_OBJECT: 	level++; break; 
			case END_OBJECT: 	level--; break;
			case KEY_NAME:		
				String key = parser.getString();
				switch (level) {
				case 1: isManual = key.equalsIgnoreCase("MATCH_MANU"); break;
				case 2: id1 = Integer.valueOf(key); break;
				case 3: id2 = Integer.valueOf(key); break;
				}
				break;
			case VALUE_NUMBER:
				CRTable.get().getLoader().onNewMatchPair (id1, id2, parser.getBigDecimal().doubleValue(), isManual);
				break;
			default:break;  
			}
			StatusBar.get().updateProgressbar(parser.getLocation().getStreamOffset());
		}
		
	}

}
