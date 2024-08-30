package cre.format.csv;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;

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

public class CSVReader {


	

	public static void load (File file) throws OutOfMemoryError, RuntimeException {
		
		try {
			CRTable.get().init();
			CRTable.get().getLoader().onBeforeLoad();

			long x = file.length();
			long offset = 0;
			StatusBar.get().initProgressbar(file.length(), "Loading CSV file ...");

			// see https://geekprompt.github.io/Properly-handling-backshlash-while-using-openCSV/
			// CSVReader and CSVWriter use different default escape characters ... so we have to adjust the reading part
			RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
			CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(new FileReader(file)).withCSVParser(rfc4180Parser);
			com.opencsv.CSVReader csv = csvReaderBuilder.build();
			

			ArrayList<ArrayList<Integer>> pubs = new ArrayList<>();

			String[] header = csv.readNext();
			if ((header != null) && (header.length>0)) {

				// column index of N_CR
				int idxNCR = Arrays.asList(header).indexOf("N_CR");

				String[] line;
				while ((line = csv.readNext()) != null) {
					offset += Arrays.stream(line).mapToInt(s -> s.length()).sum();
					StatusBar.get().updateProgressbar(offset);
					CRType_MM cr = parseCR(header, line);
					CRTable.get().getLoader().onNewCR(cr);


					try { 
						int ncr = Integer.parseInt(line[idxNCR]); 

						for (int i=pubs.size(); i<ncr; i++) {
							pubs.add(new ArrayList<Integer>());
						}
						for (int i=0; i<ncr; i++) {
							pubs.get(i).add(cr.getID());
						}
					} catch (NumberFormatException e) { }
				}

				for (ArrayList<Integer> crIds: pubs) {
					PubType_MM p = new PubType_MM();
					p.setPY(3000);
					CRTable.get().getLoader().onNewPub(p, crIds);
				}

			}


			csv.close();

                // entries[i] = 
                // entries[i].setPY(3000);
			CRTable.get().getLoader().onAfterLoad();
			CRTable.get().updateData();
			CRTable.get().getClustering().updateClustering(Clustering.ClusteringType.INIT, null, Clustering.min_threshold, false, false, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


    public static CRType_MM parseCR (String[] header, String[] line) {

        CRType_MM cr = new CRType_MM();
        for (int i=0; i<header.length; i++) {
			if (header[i].equalsIgnoreCase("ID"))          cr.setID(Integer.parseInt(line[i]));
            if (header[i].equalsIgnoreCase("CR"))          cr.setCR(line[i]);
            if (header[i].equalsIgnoreCase("RPY"))   try { cr.setRPY(Integer.parseInt(line[i])); } catch (NumberFormatException e) { }
            if (header[i].equalsIgnoreCase("AU"))          cr.setAU(line[i]);
            if (header[i].equalsIgnoreCase("AU_L"))        cr.setAU_L(line[i]);
            if (header[i].equalsIgnoreCase("AU_F"))        cr.setAU_F(line[i]);
            if (header[i].equalsIgnoreCase("AU_A"))        cr.setAU_A(line[i]);
            if (header[i].equalsIgnoreCase("TI"))          cr.setTI(line[i]);
            if (header[i].equalsIgnoreCase("J"))           cr.setJ(line[i]);
            if (header[i].equalsIgnoreCase("J_N"))         cr.setJ_N(line[i]);
            if (header[i].equalsIgnoreCase("J_S"))         cr.setJ_S(line[i]);
            if (header[i].equalsIgnoreCase("VOL"))         cr.setVOL(line[i]);
            if (header[i].equalsIgnoreCase("PAG"))         cr.setPAG(line[i]);
            if (header[i].equalsIgnoreCase("DOI"))         cr.setDOI(line[i]);

        }
        return cr;
    }


}
