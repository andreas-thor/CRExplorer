package cre.format.importer;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.Clustering;
import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;
import cre.ui.statusbar.StatusBar;


public class CSV {

	static ArrayList<ArrayList<Integer>> pubs;
	static int currentCRID;

	public static void load (List<File> files) throws OutOfMemoryError, RuntimeException {
		try {
			CRTable.get().init();
			CRTable.get().getLoader().onBeforeLoad();
			StatusBar.get().initProgressbar(files.stream().mapToLong(f -> f.length()).sum(), "Loading CSV files ...");

			pubs = new ArrayList<>();
			currentCRID = 0;
			for (File f: files) load(f);

			for (ArrayList<Integer> crIds: pubs) {
				PubType_MM p = new PubType_MM();
				p.setPY(3000);
				CRTable.get().getLoader().onNewPub(p, crIds);
			}

			CRTable.get().getLoader().onAfterLoad();
			CRTable.get().updateData();
			CRTable.get().getClustering().updateClustering(Clustering.ClusteringType.INIT, null, Clustering.min_threshold, false, false, false, false);
	
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}


	private static void load (File file) throws Exception {


		// see https://geekprompt.github.io/Properly-handling-backshlash-while-using-openCSV/
		// CSVReader and CSVWriter use different default escape characters ... so we have to adjust the reading part
		RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
		CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(new FileReader(file)).withCSVParser(rfc4180Parser);
		com.opencsv.CSVReader csv = csvReaderBuilder.build();

		String[] header = csv.readNext();
		if ((header != null) && (header.length>0)) {

			// column index of N_CR
			int idxNCR = Arrays.asList(header).indexOf("N_CR");

			String[] line;
			while ((line = csv.readNext()) != null) {
				StatusBar.get().incProgressbar(Arrays.stream(line).mapToInt(s -> s.length()).sum());
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



		}

		csv.close();

	}


    public static CRType_MM parseCR (String[] header, String[] line) {

        CRType_MM cr = new CRType_MM();
		cr.setID(++currentCRID);	// we ignore the ID in the file and provide new ids
        for (int i=0; i<header.length; i++) {
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
