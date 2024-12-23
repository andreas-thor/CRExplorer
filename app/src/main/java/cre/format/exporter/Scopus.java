package cre.format.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.opencsv.CSVWriter;

import cre.CRELogger;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRType;
import cre.ui.statusbar.StatusBar;


public class Scopus   {

	/**
	 * 
	 * @param file_name
	 * @param includePubsWithoutCRs
	 * @param filter
	 * @param comp IS IGNORED
	 * @throws IOException
	 */
	public static void save (OutputStream out, boolean includePubsWithoutCRs, Predicate<CRType> filter, Comparator<CRType> comp) throws IOException {
		
		final AtomicLong debug_countNR = new AtomicLong(0);

		/* TODO: Filter not supported yet ... nun drin? */
		StatusBar.get().initProgressbar(CRTable.get().getStatistics().getNumberOfPubs());
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(out, "UTF-8"));
		
		csv.writeNext(new String[] {"Authors","Title","Year","Source title","Volume","Issue","Art. No.","Page start","Page end","Page count","Cited by","DOI","Link","Affiliations","Authors with affiliations","Abstract","Author Keywords","References","Document Type","Source","EID"});
		
		CRTable.get().getPub(includePubsWithoutCRs, true).forEach(pub -> {
			ArrayList<String> row = new ArrayList<String>();
			
			row.add ((pub.getAU().count() == 0) ? "" :
				pub.getAU().map ( a -> {
					String[] split = a.split(", ", 2);
					String res = (split.length==2) ? split[0] + ", " + split[1].replaceAll("([A-Z])", "$1.") : a; 
					return res;
				}).collect (Collectors.joining(", "))
			);

			row.add (pub.getTI() == null ? "" : pub.getTI());
			row.add (pub.getPY() == null ? "" : pub.getPY().toString());
			row.add (pub.getSO() == null ? "" : pub.getSO());
			row.add (pub.getVL() == null ? "" : pub.getVL());
			row.add (pub.getIS() == null ? "" : pub.getIS());
			row.add (pub.getAR() == null ? "" : pub.getAR());
			row.add (pub.getBP() == null ? "" : pub.getBP().toString());
			row.add (pub.getEP() == null ? "" : pub.getEP().toString());
			row.add (pub.getPG() == null ? "" : pub.getPG().toString());
			row.add (pub.getTC() == null ? "" : pub.getTC().toString());
			row.add (pub.getDI() == null ? "" : pub.getDI());
			row.add (pub.getLI() == null ? "" : pub.getLI());

			row.add (pub.getAA().count() == 0 ? "" : pub.getAA().collect(Collectors.joining("; ")));
			
			row.add ((pub.getC1().count() == 0) ? "" :
				pub.getC1().map(it -> {
					String[] split = it[0].split(", ", 2);
					String res = (split.length==2) ? (split[0] + ", " + split[1].replaceAll("([A-Z])", "$1.") + ", " + it[1]) : (it[0] + ", " + it[1]);
					return res;
				}).collect(Collectors.joining("; "))
			);
				
			row.add (pub.getAB() == null ? "" : pub.getAB());
			row.add (pub.getDE() == null ? "" : pub.getDE());

			row.add (pub.getCR().sorted(comp)
				.filter(cr -> filter.test(cr))	
				.map ( cr -> (cr.getFormatType()==CRType.FORMATTYPE.SCOPUS) ? cr.getCR() : generateCRString (cr))
				.map ( crString -> crString.replaceAll(";", ","))	// ; is String join separator 
				.peek (cr -> debug_countNR.incrementAndGet())
				.collect (Collectors.joining ("; ")));

			row.add (pub.getDT() == null ? "" : pub.getDT());
			row.add (pub.getFS() == null ? "" : pub.getFS());
			row.add (pub.getUT() == null ? "" : pub.getUT());
					
			csv.writeNext ((String[]) row.toArray(new String[row.size()]));
		
			StatusBar.get().incProgressbar();
		});
					
			
		csv.close();

		StatusBar.get().setValue ("Saving Scopus file done");

		CRELogger.get().logInfo(String.format("debug_countNR: %d", debug_countNR.get()));
	}
	
	// FIXME: wieder zurück auf private
	public static String generateCRString (CRType<?> cr) {
		/* generate CR string in Scopus format */
		String res = "";
		if (cr.getAU_A() == null) {
			if (cr.getAU_L() != null) res += cr.getAU_L() + ", " + cr.getAU_F().replaceAll("([A-Z])", "$1."); 
		} else {
			res += cr.getAU_A().replaceAll(";", ",");
		}
		res += ",";
		if (cr.getTI() != null)	res += cr.getTI();
		if (cr.getRPY() != null) { 
			res += " (" + cr.getRPY() + ") ";

		} else {
			res = res;
		}
		
		if (cr.getJ_N() != null) {
			res += cr.getJ_N().replaceAll(",", " ").replaceAll(";", " ");
		} else {
			if (cr.getJ_S() != null) {
				res += cr.getJ_S().replaceAll(",", " ").replaceAll(";", " ");
			} else {
				if (cr.getJ() != null) {
					res += cr.getJ().replaceAll(",", " ").replaceAll(";", " ");
				}
			}
		}

		if (cr.getVOL() != null) res += ", " + cr.getVOL();
		if (cr.getPAG() != null) res += ", pp." + cr.getPAG();
		if (cr.getDOI() != null) res += ", DOI " + cr.getDOI();

		return res;
	}
	
} 

