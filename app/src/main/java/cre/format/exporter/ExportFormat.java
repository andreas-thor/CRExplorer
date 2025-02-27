package cre.format.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

import cre.data.type.abs.CRType;
import cre.ui.statusbar.StatusBar;

public enum ExportFormat {

	CRE("CRE", "cre", cre.format.exporter.CRE::save),
	
	WOS("Web of Science", "txt", WoS::save),

	SCOPUS("Scopus", "csv", Scopus::save),

	CSV_CR("Cited References", "csv", CSV::saveCR), 
	
	CSV_PUB("Citing Publications", "csv", CSV::savePub), 

	CSV_CR_PUB("Cited References + Citing Publications", "csv", CSV::saveCRPub), 

	CSV_GRAPH("CRE Graph", "csv", CSV::saveGraph);
	
	
	

	public interface Export {
	   void save(OutputStream out, boolean includePubsWithoutCRs, Predicate<CRType> filter, Comparator<CRType> comp) throws IOException, RuntimeException;
	}
	
	private final String label;
	private final String fileExtension;
	private final Export exportSave;

	
	ExportFormat(String label, String fileExtension, Export exportSave) {
		this.label = label;
		this.fileExtension = fileExtension;
		this.exportSave = exportSave;
	}

	public void save (File file, boolean includePubsWithoutCRs) throws IOException {
		this.save (file, includePubsWithoutCRs, null, null);
	}
	
	
	public void save (File file, boolean includePubsWithoutCRs, Predicate<CRType> filter, Comparator<CRType> comp) throws IOException {
		
		StatusBar.get().setValue(String.format ("Saving %2$s file %1$s ...", file.getName(), this.getLabel()));
		
		this.exportSave.save(new FileOutputStream(file), includePubsWithoutCRs, Optional.ofNullable(filter).orElse(it -> true), Optional.ofNullable(comp).orElse(CRType::compareTo));
		StatusBar.get().setValue(String.format ("Saving %2$s file %1$s done", file.getName(), this.getLabel()));

	}
	
		


	public String getLabel() {
		return label;
	}

	public String getFileExtension() {
		return fileExtension;
	}


};