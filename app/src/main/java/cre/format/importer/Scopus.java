package cre.format.importer;
 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.opencsv.CSVReader;

import cre.CRELogger;
import cre.data.type.abs.CRType;
import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;

public class Scopus extends ImportReader  {

	static Pattern sScopus_matchEMail = Pattern.compile ("\\s(\\S+@\\w+(\\.\\w+)+)\\W*");
	
	static Pattern sScopus_matchBlock = Pattern.compile ("^([A-Y ]+)(\\: )(.*)");
	
	static List<Pattern> sScopus_matchPAG  = new ArrayList<Pattern>() {
		private static final long serialVersionUID = 1L;
	{
		add(Pattern.compile ("p\\. ([0-9]+)$"));
		add(Pattern.compile ("p\\. ([0-9]+)[\\.;,]"));
		add(Pattern.compile ("pp\\. ([0-9]+)\\-[0-9]+[\\.;,]"));
		add(Pattern.compile ("pp\\. ([0-9]+)\\-[0-9]+$"));
	}};
	
	static List<Pattern> sScopus_matchVOL = new ArrayList<Pattern>() {
		private static final long serialVersionUID = 1L;
	{
		add(Pattern.compile ("([0-9]+)"));
		add(Pattern.compile ("([0-9]+) \\([0-9]+\\)"));
		add(Pattern.compile ("([0-9]+) \\([0-9]+\\-[0-9]+\\)"));
	}};

	static Pattern sScopus_matchAuthor = Pattern.compile ("^([^,]+),([ A-Z\\-\\.]+\\.),(.*)");
	static Pattern sScopus_matchYearTitle = Pattern.compile ("(.*?)\\((\\d{4})\\)(.*)");
	static Pattern sScopus_matchDOI = Pattern.compile (".*((DOI)|(doi))\\:?=?(10\\.[^/]+/ *[^;,]+).*");
	static Pattern sScopus_matchDOIURL = Pattern.compile (".*http://dx\\.doi\\.org/([^\\s,]+).*");

	
	private Map<String, Integer> attribute2Index = null;
	private CSVReader csv = null;
	
	private int debug_NCR = 0;
	
	@Override
	public void init(File file) throws IOException {
		attribute2Index = null;
		csv = null;
		super.init(file);
	}
		
	@Override
	protected void computeNextEntry () throws IOException {
		
		entry = null;
		
		if (csv==null) {
			csv = new CSVReader(br);
			String[] line = null;
			line = csv.readNext();
			if (line == null) return;
			String[] attributes = Arrays.stream(line).map(it ->  it.trim()).toArray(size -> new String[size]);
			if (attributes[0].startsWith("\uFEFF")) attributes[0] = attributes[0].substring(1);
			attribute2Index = IntStream.range(0, attributes.length).mapToObj (i -> Integer.valueOf(i)).collect(Collectors.toMap(i -> attributes[i].toUpperCase(), i -> i));
		}
		
		
		String[] line = null;
		line = csv.readNext();
		if (line == null) return;
		entry = parsePub(line);
	}
	
	
	@Override
	public void close() throws IOException {
		CRELogger.get().logInfo(String.format("debug_NCR: %d", debug_NCR));

		csv.close();
		super.close();
	}
	
	
	private PubType_MM parsePub (String[] line) {
		
		PubType_MM pub = new PubType_MM(); // .parseScopus(it, attributes, yearRange);
		
		pub.setPT("J"); // TODO: what is the default Publication Type? (No value in scopus!)
				
		// Scopus Authors: Lastname1 I1., Lastname2 I2.I2. ...
		if ((attribute2Index.get("AUTHORS")!=null) && (line[attribute2Index.get("AUTHORS")]!=null)) {
			for (String name: line[attribute2Index.get("AUTHORS")].split("\\., ")) {
				name = name.replaceAll("\\.", ""); 
				int pos = name.lastIndexOf(" ");
				pub.addAU((pos>0) ? name.substring(0, pos) + "," + name.substring (pos) : name);
			}
		}
		
		pub.getAU().forEach (e -> pub.addAF(e));		// there are no full names in Scopus 
		
		// Authors with affiliations: "<lastname>, <initials with dots>, affiliation"
		if ((attribute2Index.get("AUTHORS WITH AFFILIATIONS") != null) && (line[attribute2Index.get("AUTHORS WITH AFFILIATIONS")] != null)) {
		
			for (String author: line[attribute2Index.get("AUTHORS WITH AFFILIATIONS")].split("; ")) {
				String[] split = author.split(", ", 3);
				if (split.length == 3) {
					pub.addC1(new String[] { (split[0]+", "+split[1].replaceAll("\\.", "")), split[2] });
				}
				
				// if (author.contains("@")) System.out.println ("@@@");
			
				Matcher Scopus_matchEMail = sScopus_matchEMail.matcher(author);
				if (Scopus_matchEMail.find()) {
					// System.out.println (Scopus_matchEMail.group(1));
					pub.addEM(Scopus_matchEMail.group(1));
				}
			}
		}
			
		if ((attribute2Index.get("AFFILIATIONS") != null) && (line[attribute2Index.get("AFFILIATIONS")] != null)) {
			for (String aff: line[attribute2Index.get("AFFILIATIONS")].split("; ")) pub.addAA(aff);
		}
				
		pub.setTI(attribute2Index.get("TITLE") != null ? line[attribute2Index.get("TITLE")] : null);
		try { pub.setPY(Integer.valueOf(line[attribute2Index.get("YEAR")])); } catch (Exception e) { }

		pub.setSO(attribute2Index.get("SOURCE TITLE") != null ? line[attribute2Index.get("SOURCE TITLE")] : null);
		pub.setVL(attribute2Index.get("VOLUME") != null ? line[attribute2Index.get("VOLUME")] : null);
		pub.setIS(attribute2Index.get("ISSUE") != null ? line[attribute2Index.get("ISSUE")] : null);
		pub.setAR(attribute2Index.get("ART. NO.") != null ? line[attribute2Index.get("ART. NO.")] : null);
		
		try { pub.setBP(Integer.valueOf(line[attribute2Index.get("PAGE START")])); } catch (Exception e) { }
		try { pub.setEP(Integer.valueOf(line[attribute2Index.get("PAGE END")])); } catch (Exception e) { }
		try { pub.setPG(Integer.valueOf(line[attribute2Index.get("PAGE COUNT")])); } catch (Exception e) { }
		try { pub.setTC(Integer.valueOf(line[attribute2Index.get("CITED BY")])); } catch (Exception e) { }
		
		/* parse list of CRs */
		if ((attribute2Index.get("REFERENCES") != null) && (line[attribute2Index.get("REFERENCES")] != null)) {
			for (String crString: line[attribute2Index.get("REFERENCES")].split(";")) {
//				pub.addCR (parseCR (crString), true);
				
				CRType_MM cr = parseCR(crString);
				if (cr != null) {
					debug_NCR++;
					pub.addCR(cr, true);  
				}				
			}
		}
		
		pub.setDI(attribute2Index.get("DOI") != null ? line[attribute2Index.get("DOI")] : null);
		pub.setLI(attribute2Index.get("LINK") != null ? line[attribute2Index.get("LINK")] : null);
		pub.setAB(attribute2Index.get("ABSTRACT") != null ? line[attribute2Index.get("ABSTRACT")] : null);
		pub.setDE(attribute2Index.get("AUTHOR KEYWORDS") != null ? line[attribute2Index.get("AUTHOR KEYWORDS")] : null);
		pub.setDT(attribute2Index.get("DOCUMENT TYPE") != null ? line[attribute2Index.get("DOCUMENT TYPE")] : null);
		pub.setFS(attribute2Index.get("SOURCE") != null ? line[attribute2Index.get("SOURCE")] : null);
		
		// System.out.println(pub.getTI());
		pub.setUT(attribute2Index.get("EID") != null ? line[attribute2Index.get("EID")] : null);
		
		return pub;
	}
	
	protected static CRType_MM parseCR (String line) {
		
		line = line.trim();
		if (line.length() == 0) { 
			CRELogger.get().logInfo("Hey, line is empty");
			return null;
		}
		
		CRType_MM res = new CRType_MM();
		res.setFormatType (CRType.FORMATTYPE.SCOPUS);
		res.setCR(line);
		
		// parse all authors (but save first author only in AU_L, AU_F, AU; all authors go to AU_A)
		boolean firstAuthor = true;
		Matcher Scopus_matchAuthor = sScopus_matchAuthor.matcher(line);
		while (Scopus_matchAuthor.matches()) {
			if (firstAuthor) {
				res.setAU_L(Scopus_matchAuthor.group(1));
				res.setAU_F((Scopus_matchAuthor.group(2).trim()).substring(0,  1));
				res.setAU(Scopus_matchAuthor.group(1) + "," + Scopus_matchAuthor.group(2));
				res.setAU_A(Scopus_matchAuthor.group(1) + "," + Scopus_matchAuthor.group(2));
			} else {
				res.setAU_A(res.getAU_A() + "; " + Scopus_matchAuthor.group(1) + "," + Scopus_matchAuthor.group(2));
			}
			firstAuthor = false;
			line = Scopus_matchAuthor.group(3).trim();
			Scopus_matchAuthor.reset(line);
		}
		
		// find publication year and title
		res.setJ_N("");
		res.setJ("");
		res.setTI("");
		Matcher Scopus_matchYearTitle = sScopus_matchYearTitle.matcher(line);
		if (Scopus_matchYearTitle.matches()) {
			if (Scopus_matchYearTitle.group(1).length() == 0) {
				try { res.setRPY(Integer.valueOf (Scopus_matchYearTitle.group(2))); } catch (NumberFormatException e) {}
				int pos = Scopus_matchYearTitle.group(3).indexOf(", ,");
				if (pos>=0) {
					res.setTI(Scopus_matchYearTitle.group(3).substring(0, pos));
					res.setJ_N("");
					res.setJ(Scopus_matchYearTitle.group(3).substring (pos+3).trim());
				} else {
					String[] crsplit = Scopus_matchYearTitle.group(3).split (",", 2);
					res.setJ_N(crsplit[0].trim());
					res.setJ(Scopus_matchYearTitle.group(3).trim());
				}
				
			} else {
				res.setTI(Scopus_matchYearTitle.group(1));
				try { res.setRPY(Integer.valueOf (Scopus_matchYearTitle.group(2))); } catch (NumberFormatException e) {}
				String[] crsplit = Scopus_matchYearTitle.group(3).split (",", 2);
				res.setJ_N(crsplit[0].trim());
				res.setJ(Scopus_matchYearTitle.group(3).trim());
			}
		} else {	// no year
			CRELogger.get().logInfo("NO YEAR MATCHES");	
			String[] crsplit = line.split (",", 2);
			res.setJ_N(crsplit[0].trim());
			res.setJ(line.trim());
			CRELogger.get().logInfo(line + "->" + res.getJ_N() + "/" + res.getJ());
		}


		// check if year available and in the given year range
//		if (res.RPY == null) return null;
		
//		if (res.getRPY() != null) {
//			if (((res.getRPY() < yearRange[0]) && (yearRange[0]>0)) || ((res.getRPY() > yearRange[1]) && (yearRange[1]>0))) return null;
//		} else {
//			if ((yearRange[0]>0) || (yearRange[1]>0)) return null;
//		}

		
		// process Journal names
		String[] split = res.getJ_N().split(" ");
		res.setJ_S((split.length==1) ? split[0] : Arrays.stream (split).reduce("",  (x,y) -> x + ((y.length()>0) ? y.substring(0,1) : "") ));

		
		Matcher Scopus_matchDOI = sScopus_matchDOI.matcher (res.getJ().replaceAll(" ",""));
		if (Scopus_matchDOI.matches()) {
			res.setDOI(Scopus_matchDOI.group(4));
		}
		
		Matcher Scopus_matchDOIURL = sScopus_matchDOIURL.matcher (res.getJ());
		if (Scopus_matchDOIURL.matches()) {
			res.setDOI(Scopus_matchDOIURL.group(1));
		}

		if ((res.getJ().toLowerCase().indexOf("doi")>=0) && (res.getDOI() == null)) {
			// TODO: J8 improve DOI identification
			System.out.println ("DOI could not been identified in: " + res.getJ());
		}
		
		
		for (String it: res.getJ().split (",")) {
			
			String s = it.trim();
			for (Pattern p: sScopus_matchPAG) {
				Matcher matchP = p.matcher(s.trim());
				if (matchP.matches()) res.setPAG(matchP.group(1));
			}
			
			for (Pattern p: sScopus_matchVOL) {
				Matcher matchV = p.matcher(s.trim());
				if (matchV.matches()) res.setVOL(matchV.group(1));
			}
		}
		
		return res;
	}
	
} 

