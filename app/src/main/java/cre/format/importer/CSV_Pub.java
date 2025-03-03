package cre.format.importer;
 
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.opencsv.CSVReader;

import cre.CRELogger;
import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;

public class CSV_Pub extends ImportReader  {

	private CSVReader csv = null;
	private String[] line = null;
	private String[] header = null;
	
	private int debug_NCR = 0;
	
	@Override
	public void init(File file) throws IOException {
		csv = null;
		line = null;
		header = null;
		super.init(file);
	}
		
	@Override
	protected void computeNextEntry () throws IOException {
		
		entry = null;
		
		if (csv==null) {
			// read hearder 

			csv = new CSVReader(br);
			String[] firstLine = null;
			firstLine = csv.readNext();
			if (firstLine == null) return;
			header = Arrays.stream(firstLine).map(it ->  it.trim()).toArray(size -> new String[size]);
			if (header[0].startsWith("\uFEFF")) header[0] = header[0].substring(1);

			// read first line
			line = csv.readNext();
		}
		
		if (line == null) return;


		entry = parsePub(header, line);


		// line is first line of pub block
		entry.addCR(parseCR(header, line), true);
		
		while (true) {

			line = csv.readNext();
			if (line == null) break; // no more entries

			PubType_MM p = parsePub(header, line);
			if (p.getID() != entry.getID()) break;		// new pub begins

			entry.addCR(parseCR(header, line), true);
		}


	}
	

	public static PubType_MM parsePub (String[] header, String[] line) {

		PubType_MM pub = new PubType_MM();

		for (int i=0; i<header.length; i++) {
			// we take the first ID column
            if ((header[i].equalsIgnoreCase("ID")) && (pub.getID()==null))          try { pub.setID(Integer.valueOf(line[i])); } catch (NumberFormatException e) { }
            if (header[i].equalsIgnoreCase("PT"))          pub.setPT(line[i]);

            if (header[i].equalsIgnoreCase("AU"))          for (String l:line[i].split("; ")) pub.addAU(l);
            if (header[i].equalsIgnoreCase("AF"))          for (String l:line[i].split("; ")) pub.addAF(l);
            if (header[i].equalsIgnoreCase("C1"))          ; // TODO
            if (header[i].equalsIgnoreCase("EM"))          for (String l:line[i].split("; ")) pub.addEM(l);
            if (header[i].equalsIgnoreCase("AA"))          for (String l:line[i].split("; ")) pub.addAA(l);

			if (header[i].equalsIgnoreCase("TI"))          pub.setTI(line[i]);
			if (header[i].equalsIgnoreCase("PY"))          try { pub.setPY(Integer.valueOf(line[i])); } catch (NumberFormatException e) { }
			if (header[i].equalsIgnoreCase("SO"))          pub.setSO(line[i]);
			if (header[i].equalsIgnoreCase("VL"))          pub.setVL(line[i]);
			if (header[i].equalsIgnoreCase("IS"))          pub.setIS(line[i]);
			if (header[i].equalsIgnoreCase("AR"))          pub.setAR(line[i]);
			if (header[i].equalsIgnoreCase("BP"))          try { pub.setBP(Integer.valueOf(line[i])); } catch (NumberFormatException e) { }
			if (header[i].equalsIgnoreCase("EP"))          try { pub.setEP(Integer.valueOf(line[i])); } catch (NumberFormatException e) { }
			if (header[i].equalsIgnoreCase("PG"))          try { pub.setPG(Integer.valueOf(line[i])); } catch (NumberFormatException e) { }
			if (header[i].equalsIgnoreCase("TC"))          try { pub.setTC(Integer.valueOf(line[i])); } catch (NumberFormatException e) { }

			if (header[i].equalsIgnoreCase("DI"))          pub.setDI(line[i]);
			if (header[i].equalsIgnoreCase("LI"))          pub.setLI(line[i]);
			if (header[i].equalsIgnoreCase("AB"))          pub.setAB(line[i]);
			if (header[i].equalsIgnoreCase("DE"))          pub.setDE(line[i]);
			if (header[i].equalsIgnoreCase("DT"))          pub.setDT(line[i]);
			if (header[i].equalsIgnoreCase("FS"))          pub.setFS(line[i]);
			if (header[i].equalsIgnoreCase("UT"))          pub.setUT(line[i]);
        }

		return pub;

	}


	public static CRType_MM parseCR (String[] header, String[] line) {

        CRType_MM cr = new CRType_MM();
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


	
	@Override
	public void close() throws IOException {
		CRELogger.get().logInfo(String.format("debug_NCR: %d", debug_NCR));

		csv.close();
		super.close();
	}
	
	
	
	
	
} 

