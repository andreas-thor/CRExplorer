package cre.format.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.opencsv.CSVReader;

import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;

public class CSV extends ImportReader {

    private int countComputeNextEntryCall;
    // private CRType_MM[] crs;
    private Integer[] crsNCR;
    private PubType_MM[] entries;

    @Override
	public void init(InputStream is) throws IOException {
        this.countComputeNextEntryCall = 0;
        super.init(is);
    }

    @Override
    protected void computeNextEntry() throws IOException {

        if (countComputeNextEntryCall == 0) {

            CSVReader csvReader = new CSVReader(br);
            List<String[]> data = csvReader.readAll();
            csvReader.close();

            if (data.size()==0) {
                entry = null;
                return;
            }

            String[] header = data.remove(0);
            final int idxNCR = Arrays.asList(header).indexOf("N_CR");

            // crs = data.stream().map(line -> parseCR(header, line)).toArray(CRType_MM[]::new);
            crsNCR = data.stream().map(line -> Integer.valueOf(line[idxNCR])).toArray(Integer[]::new);
          
            int ncrMax = Arrays.asList(crsNCR).stream().max(Integer::compare).get().intValue();
            // we create fake pubs and add all CRs to these pubs
            // first pub will have all crs; second pub will have all crs with n_cr>=2 etc.
            entries = new PubType_MM[ncrMax];
            for (int i=0; i<ncrMax; i++) {
                entries[i] = new PubType_MM();
                for (int k=0; k<crsNCR.length; k++) {
                    if (i<crsNCR[k]) {
                        entries[i].addCR(parseCR(header, data.get(k)), true);
                    }
                }
            }
        }

        entry = (countComputeNextEntryCall < entries.length) ? entries[countComputeNextEntryCall] : null;
        countComputeNextEntryCall++;

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
}
