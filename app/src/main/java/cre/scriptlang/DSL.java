package cre.scriptlang;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;

import javax.naming.OperationNotSupportedException;


import cre.data.CRStatsInfo;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRType;
import cre.data.type.abs.Clustering.ClusteringType;
import cre.data.type.abs.Statistics.IntRange;
import cre.format.exporter.ExportFormat;
import cre.format.importer.ImportFormat;
import cre.ui.statusbar.StatusBar;
import cre.ui.statusbar.StatusBarText;
import groovy.lang.Closure;
import groovy.lang.Script;



public class DSL extends Script {

	public static StatusBarText status;
	private static Scanner s;

	public DSL() {
		status = new StatusBarText();
		s = new Scanner(System.in);
		StatusBar.get().setUI(status);
	}

	public static void init (String type) {
		if (type != null) {
			if (type.equalsIgnoreCase("MM")) CRTable.type = CRTable.TABLE_IMPL_TYPES.MM;
			if (type.equalsIgnoreCase("DB")) CRTable.type = CRTable.TABLE_IMPL_TYPES.DB;
		}
		CRTable.get().init();
	}


	public static void progress(boolean b) {
		status.setShowProgress(b);
	}

	public static void info() {
		StatusBar.get().updateInfo();
	}

	public static String input() {
		return input(null);
	}

	public static String input(String msg) {
		System.out.print("\n");
		System.out.print(msg == null ? "" : msg);
		String result = s.nextLine();
		return result;
	}


	public static List<Map<String, Object>> getCRById (Map<String, Object> map) throws Exception {
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);
		Integer[] ids = DSL_Helper.getIds(params);
		return Arrays.stream(ids).map(id -> CRTable.get().getCRById(id).toMap()).toList();
	}

	public static Map<String, Object> getStatistics () throws Exception {
		return CRTable.get().getStatistics().toMap();
	}


	public static void openFile(Map<String, Object> map) throws Exception {
		
		List<File> files = DSL_Helper.getFiles (DSL_Helper.makeParamsUpperCase(map));
		if (files.size() != 1) {
			throw new Exception (String.format("openFile: requires one file (%d specified)", files.size()));
		}
		cre.format.importer.CRE.load(files.get(0));
		
	}

	
	public static void analyzeFile(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);
		DSL_Helper.getImportFormat(params).analyze(DSL_Helper.getFiles (params));
		System.out.println(CRStatsInfo.get().toString());
	}

	
	public static void importFile(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);
		analyzeAndLoadFile(DSL_Helper.getImportFormat(params), DSL_Helper.getFiles (params), params);
	}

	
	
	public static void importSearch(Map<String, Object> map) throws Exception {
		
		throw new OperationNotSupportedException();
		/*
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);
		List<File> files = Crossref.download(
			"", 
			DSL_Helper.getDOI (params), 
			(String) params.getOrDefault("ISSN", ""), 
			DSL_Helper.getRange(params.get("PY"), new IntRange(-1,  -1)));

		analyzeAndLoadFile(ImportFormat.CROSSREF, DSL_Helper.getFiles (params), params);
		*/
	}

	
	private static void analyzeAndLoadFile (ImportFormat fileFormat, List<File> files, Map<String, Object> params) throws Exception {
		fileFormat.analyze(files);
		fileFormat.load(
				files, 
				DSL_Helper.getRange(params.get("RPY"), CRStatsInfo.get().getRangeRPY()), 
				DSL_Helper.getWithoutYear(params.get("RPY")), 
				DSL_Helper.getRange(params.get("PY"), CRStatsInfo.get().getRangePY()),
				DSL_Helper.getWithoutYear(params.get("PY")), 
				((Integer) params.getOrDefault("MAXCR", 0)).longValue(),  
				DSL_Helper.getSampling(params));
	}
	
	
	public static void saveFile(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);

		// set file
		if (params.get("FILE")==null) {
			throw new Exception ("saveFile: missing parameter file");
		}
		File file = new File ((String) params.get("FILE"));

		ExportFormat.CRE.save(file, true);
		
	}

	public static void exportFile(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);
		
		// set file
		if (params.get("FILE")==null) {
			throw new Exception ("exportFile: missing parameter file");
		}
		File file = new File (params.get("FILE").toString());

		// set RPY filter
		IntRange range = DSL_Helper.getRange(params.get("RPY"), null); 
		Predicate<CRType> filter = (range==null) ? cr -> true : cr -> (cr.getRPY() != null) && (cr.getRPY()>=range.getMin()) && (cr.getRPY()<=range.getMax());
		
		// overwrite with user-defined filter if defined
		if ((params.get("FILTER") != null)) {
			Closure<Boolean> a = (Closure<Boolean>)params.get("FILTER");
			filter = cr -> a.call(CitedReference.createFromCRType(cr));
			// filter = cr -> ((Predicate<CitedReference>) params.get("FILTER")).test(CitedReference.createFromCRType(cr)) ;
		}
		
		// includePubsWithoutCRs
		boolean includePubsWithoutCRs = (boolean) params.getOrDefault("W/O_CR", true);

		// build comparator based on sort parameter
		Comparator<CRType> compCRType = null;
		if (params.keySet().contains("SORT")) {

			// building a CitedReferences comparator
			Comparator<CitedReference> compCitedReference = null; 
			String[] values = (params.get("SORT") instanceof String) ? new String[] { (String) params.get("SORT") } : ((ArrayList<String>) params.get("SORT")).stream().toArray (String[]::new);
			
			for (String s: values) {		// list of order by properties
				String[] split = s.trim().split("\\s+");
				String prop = split[0];
				boolean reversed = (split.length>1) && (split[1].toUpperCase().equals("DESC"));		
				
				Comparator<CitedReference> compProp = CitedReference.getComparatorByProperty(prop);
				if (compProp == null) throw new Exception ("exportFile: Unknown sort property " + prop);
				compProp = reversed ? compProp.reversed() : compProp;
				compCitedReference = (compCitedReference == null) ? compProp : compCitedReference.thenComparing (compProp);
			}
			
			
			Comparator<CitedReference> compCR = compCitedReference;
			// build the "real" comparator
			compCRType = new Comparator<CRType>() {
				@Override
				public int compare(CRType o1, CRType o2) {
					return compCR.compare(CitedReference.createFromCRType(o1), CitedReference.createFromCRType(o2));
				}
			};
		}
		
		DSL_Helper.getExportFormat(params).save (file, includePubsWithoutCRs, filter, compCRType);
	}

	public static void removeCR(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);

		IntRange range = DSL_Helper.getRange(params.get("N_CR"), null); 
		if (range != null) {
			CRTable.get().removeCRByN_CR(range);
			return;
		}

		range = DSL_Helper.getRange(params.get("RPY"), null); 
		if (range != null) {
			CRTable.get().removeCRByYear(range);
			if (DSL_Helper.getWithoutYear(params.get("RPY"))) {
				CRTable.get().removeCRWithoutYear();
			}
			return;
		}

		return;
		// throw new Exception ("removeCR: Missing parameter (must have N_CR or RPY)");
		
	}

	public static void retainPub(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);

		IntRange range = DSL_Helper.getRange(params.get("PY"), null); 
		if (range != null) {
			CRTable.get().retainPubByCitingYear(range);
			return;
		} 
		
		throw new Exception ("retainPub: Missing parameter PY");
	}

	public static void cluster(Map<String, Object> map) {
	
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);

		double threshold = Double.valueOf(params.getOrDefault ("THRESHOLD", 0.8).toString()).doubleValue();
		boolean useVol = (boolean) params.getOrDefault ("VOLUME", false);
		boolean usePag = (boolean) params.getOrDefault ("PAGE", false);
		boolean useDOI = (boolean) params.getOrDefault ("DOI", false);
		boolean nullEqualsNull = (boolean) params.getOrDefault ("MISSING_EQUAL", false);

		CRTable.get().getClustering().generateInitialClustering();
		CRTable.get().getClustering().updateClustering(ClusteringType.REFRESH, null, threshold, useVol, usePag, useDOI, nullEqualsNull);	
	}

	public static void merge() {
		CRTable.get().merge();
	}

	public static void set(Map<String, Object> map) throws Exception {
		
		Map<String, Object> params = DSL_Helper.makeParamsUpperCase(map);

		if (params.get("N_PCT_RANGE") != null) {

			try {
				CRTable.get().setNpctRange(Integer.valueOf(params.get("N_PCT_RANGE").toString()).intValue());
				CRTable.get().updateData();
			} catch (Exception e) {
				throw new Exception("Wrong value for set parameter N_PCT_RANGE: " + params.get("N_PCT_RANGE"));
			}
			params.remove("N_PCT_RANGE");

		}

		if (params.get("MEDIAN_RANGE") != null) {

			try {
				CRTable.get().getChartData().setMedianRange(Integer.valueOf(params.get("MEDIAN_RANGE").toString()).intValue());
			} catch (Exception e) {
				throw new Exception("Wrong value for set parameter MEDIAN_RANGE: " + params.get("MEDIAN_RANGE"));
			}
			params.remove("MEDIAN_RANGE");
		}


		/* there should be no remaining parameter */
		for (String unknownParam: params.keySet()) {
			throw new Exception("Unknown set parameter: " + unknownParam);
		}		
		
		
	}

	public Class<?> use(String filename) {
		return null; // DSL_UseClass.use(filename);
	}

	@Override
	public Object run() {
		// TODO Auto-generated method stub
		return null;
	}

}
