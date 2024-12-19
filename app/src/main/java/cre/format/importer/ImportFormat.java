package cre.format.importer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import cre.Exceptions.AbortedException;
import cre.Exceptions.FileTooLargeException;
import cre.Exceptions.UnsupportedFileFormatException;
import cre.data.CRStatsInfo;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.Statistics.IntRange;
import cre.store.mm.CRCluster;
import cre.ui.dialog.Sampling;
import cre.ui.statusbar.StatusBar;

public enum ImportFormat {

	
	WOS("Web of Science", "txt", true, new WoS_txt(), null),

	SCOPUS("Scopus", "csv", true, new Scopus(), null),

	CROSSREF ("Crossref", "crossref", true, new Crossref(), null),
	
	CSV("CSV", "csv", true, null, cre.format.importer.CSV::load),

	CRE("CRE", "cre", false, null, cre.format.importer.CRE::load);


	
	private final String label;
	private final String fileExtension;
	private final boolean multipleFiles;
	private final ImportReader importReader;
	private final Consumer<List<File>> dataLoader;

	
	private ImportFormat(String label, String fileExtension, boolean multipleFiles, ImportReader importReader, Consumer<List<File>> dataLoader) {
		this.label = label;
		this.fileExtension = fileExtension;
		this.multipleFiles = multipleFiles;
		this.importReader = importReader;
		this.dataLoader = dataLoader;
	}

	
	public boolean isDataLoader() {
		return this.importReader == null;
	}

	public boolean isCREFormat() {
		return this == ImportFormat.CRE;
	}

	public boolean isMultipleFiles() {
		return this.multipleFiles;
	}

	public CRStatsInfo analyze(List<File> files) throws OutOfMemoryError, UnsupportedFileFormatException, FileTooLargeException, AbortedException, IOException {
		
		if (isDataLoader()) return null;		// Wo don't do a analysis for a data loader


		CRTable<?,?> crTab = CRTable.get(); 
		crTab.init();
		
		CRStatsInfo crStatsInfo = CRStatsInfo.get();
		crStatsInfo.init();

		int idx = 0;
		for (File file: files) {
			
			StatusBar.get().initProgressbar(file.length(), String.format("Analyzing %4$s file %1$d of %2$d (%3$s) ...", (++idx), files.size(), file.getName(), this.getLabel()));

			this.importReader.init(file);
			StreamSupport.stream(this.importReader.getIterable().spliterator(), false)
				.filter(pub -> pub != null)
				.forEach(pub -> {
					if (crTab.isAborted()) this.importReader.stop();
					StatusBar.get().incProgressbar(pub.getLength());
					crStatsInfo.updateStats(pub);
				});
			this.importReader.close();
				
		}
		
//		System.out.println(crStatsInfo);
		
		return crStatsInfo;
	}

	public void load(List<File> files, IntRange rpyRange, boolean importCRsWithoutYear, IntRange pyRange, boolean importPubsWithoutYear, long noMaxCRs, Sampling sampling) throws OutOfMemoryError, Exception {

		if (isDataLoader()) {
			this.dataLoader.accept(files);	
			return;
		}
		
		long ts1 = System.currentTimeMillis();
		long ms1 = Runtime.getRuntime().totalMemory();

		Random rand = new Random();
		CRTable<?, ?> crTab = CRTable.get(); 
		crTab.init();

//		final long noMaxCRs = UserSettings.get().getMaxCR();
//		final Sampling sampling = UserSettings.get().getSampling();
		
//		final int[] rpyRange = UserSettings.get().getRange(RangeType.ImportRPYRange);
//		boolean importCRsWithoutYear = UserSettings.get().getImportCRsWithoutYear();
		
//		final int[] pyRange = UserSettings.get().getRange(RangeType.ImportPYRange);
//		boolean importPubsWithoutYear = (sampling==Sampling.CLUSTER) ? false : UserSettings.get().getImportPubsWithoutYear();
		
		/* CLUSTER sampling --> pick a single PY at random; no pubs w/o year */
		final boolean importPubsWOYear = (sampling==Sampling.CLUSTER) ? false : importPubsWithoutYear;
		final IntRange pyRangeSampled  = (sampling==Sampling.CLUSTER) ? new IntRange(pyRange.getMin() + rand.nextInt(pyRange.getSize())) : pyRange;
		
			
			
		
		
		AtomicLong noAvailableCRs = new AtomicLong (CRStatsInfo.get().getNumberOfCRs (rpyRange, importCRsWithoutYear, pyRangeSampled, importPubsWithoutYear));
		AtomicLong noToImportCRs = new AtomicLong(Math.min(noMaxCRs, noAvailableCRs.get()));
		AtomicInteger currentOffset = new AtomicInteger(0);	
		AtomicInteger crId = new AtomicInteger(0);
		
		float ratio = 1.0f*noToImportCRs.get()/noAvailableCRs.get();
		
		
		
		int idx = 0;
		
		for (File file: files) {
			
			StatusBar.get().initProgressbar(file.length(), String.format("Loading %4$s file %1$d of %2$d (%3$s) ...", (++idx), files.size(), file.getName(), this.getLabel()));

			this.importReader.init(file);
			StreamSupport.stream(this.importReader.getIterable().spliterator(), false)
				.filter(pub -> pub != null)
				.forEach(pub -> {
					
					if (crTab.isAborted()) {
						this.importReader.stop();
					}

					StatusBar.get().incProgressbar(pub.getLength());
					
					
					boolean addPub = true;
					if (pub.getPY()==null) {
						addPub = importPubsWOYear; 
					} else {
						int py = pub.getPY().intValue();
						if ((pyRangeSampled.getMin() != IntRange.NONE) && (pyRangeSampled.getMin()>py)) addPub = false;
						if ((pyRangeSampled.getMax() != IntRange.NONE) && (pyRangeSampled.getMax()<py)) addPub = false;
					}
					
					if (addPub) {
						pub.removeCRByYear(rpyRange, importCRsWithoutYear, true);
						
						if ((pub.getSizeCR()>0) && (noMaxCRs>0)) {		// select CRs at random
							
							if (sampling==Sampling.RANDOM) {
								pub.removeCRByProbability(rand.nextFloat(), 0, noToImportCRs, noAvailableCRs, currentOffset);
							}
							if (sampling==Sampling.SYSTEMATIC) {
								pub.removeCRByProbability(ratio, sampling.offset, noToImportCRs, noAvailableCRs, currentOffset);
							}
							
						}
				
						// FIXME: getSizeCR()>0 or >=0 ? Or is this an option?
						if (pub.getSizeCR()>=0) {	
							
							
							pub.getCR().forEach(cr -> {
								cr.setID(crId.incrementAndGet());
								cr.setCluster(new CRCluster(cr));
							});
							
							crTab.addPub(pub);
							
//								if ((UserSettings.get().getMaxCR()>0) && (numberOfCRs.addAndGet(pub.getSizeCR()) >= UserSettings.get().getMaxCR())) {
//									this.importReader.stop(); 
//								}
						}
					}
					
					
				});
			this.importReader.close();
				
		}

//		System.out.println("noAvailableCRs=" + noAvailableCRs.get());
//		System.out.println("noToImportCRs=" + noToImportCRs.get());
		
		
		// Check for abort by user
		if (crTab.isAborted()) {
			crTab.init();
			StatusBar.get().setValue(String.format("Loading %1$s file%2$s aborted (due to user request)", this.getLabel(), files.size()>1 ? "s" : ""));
			throw new AbortedException();
		}
		
//		System.out.println("CRTable.get().getPub().count(true)=" + CRTable.get().getPub(true).count());
//		System.out.println("CRTable.get().getPub().count()=" + CRTable.get().getPub().count());
//		System.out.println("CRTable.get().getPub(true).flatMap(pub -> pub.getCR()).count()=" + CRTable.get().getPub(true).flatMap(pub -> pub.getCR()).count());
//		System.out.println("CRTable.get().getCR().count()=" + CRTable.get().getCR().count());
		

		long ts2 = System.currentTimeMillis();
		long ms2 = Runtime.getRuntime().totalMemory();

		System.out.println("Load time is " + ((ts2-ts1)/1000d) + " seconds");
		System.out.println("Load Memory usage " + ((ms2-ms1)/1024d/1024d) + " MBytes");
		
		crTab.updateData();

		long ts3 = System.currentTimeMillis();
		long ms3 = Runtime.getRuntime().totalMemory();

		System.out.println("Update time is " + ((ts3-ts2)/1000d) + " seconds");
		System.out.println("Update Memory usage " + ((ms3-ms2)/1024d/1024d) + " MBytes");

		
		StatusBar.get().setValue(String.format("Loading %1$s file%2$s done", this.getLabel(), files.size()>1 ? "s" : ""));
	}

	public String getLabel() {
		return label;
	}

	public String getFileExtension() {
		return fileExtension;
	}


};