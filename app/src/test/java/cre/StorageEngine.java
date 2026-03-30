package cre;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import cre.data.type.abs.Clustering;
import cre.data.type.abs.Statistics.IntRange;
import cre.format.exporter.ExportFormat;
import cre.format.importer.ImportFormat;
import cre.ui.UISettings;

public class StorageEngine {

	/**
	 * This test checks if the storage engines "MM" (main memory) and "DB"
	 * (database) are equivalent. To this end, several files are imported, some data
	 * manipulation is done (remove CRs, clustering, merging) and the resulting
	 * export files are checked if they are byte-wise equivalent.
	 */

	@Test
	public void test_DB_vs_MM() throws OutOfMemoryError, Exception {

		for (Consumer<Void> dataModifier: generateDataModifiers()) {

			checkForEqualOutputFiles_DB_vs_MM(
				TestData.getImportDataLoader.apply(ImportFormat.WOS, 
					Stream.of("savedrecs_JOI1.txt", "savedrecs_JOI2.txt").map(TestData::getFile).toArray(File[]::new)), 
				dataModifier);

			checkForEqualOutputFiles_DB_vs_MM(
				TestData.getImportDataLoader.apply(ImportFormat.SCOPUS, 
					Stream.of("scopus_export_csv_incl_citations_abstract_references.csv").map(TestData::getFile).toArray(File[]::new)), 
				dataModifier);
		
			// checkForEqualOutputFiles_DB_vs_MM(
			// 	getImportDataLoader.apply(ImportFormat.WOS, new String[] { "data_climate_100t.txt"}),
			// 	dataModifier);

			checkForEqualOutputFiles_DB_vs_MM(
				TestData.getCREDataLoader.apply("sciento_bearb.cre"), 
				dataModifier);

		};
	}


	/**
	 * Generates a list of data modifiers
	 * Each modifier may remove CRs by year, by N_NC, and may appy clustering and merging
	 */
	private List<Consumer<Void>> generateDataModifiers () {

		List<Consumer<Void>> dataModifiers = new ArrayList<>();

		for (IntRange removeCRByYear : new IntRange[] { null, new IntRange(10, 2013) }) {
			for (IntRange removeCRByN_CR : new IntRange[] { null, new IntRange(0, 10) }) {
				for (Double threshold : new Double[] { null, 0.5, 0.75, 0.9 }) {
					for (boolean merge : new boolean[] { false, true }) {

						if ((threshold == null) && merge) continue; // merge is only possible after clustering

						dataModifiers.add((x) -> {
							try {

								System.out.println(String.format("Data Modifier %s %s %.2f %b", removeCRByYear, removeCRByYear, threshold, merge));
								if (removeCRByYear != null) {
									CRTable.get().removeCRByYear(removeCRByYear);
								}
				
								if (removeCRByN_CR != null) {
									CRTable.get().removeCRByN_CR(removeCRByN_CR);
								}
				
								if (threshold != null) {
									CRTable.get().getClustering().generateInitialClustering();
									CRTable.get().getClustering().updateClustering(Clustering.ClusteringType.REFRESH, null, threshold, false, false, false, false);
								}
				
								if (merge) {
									CRTable.get().merge();
								}
							} catch (OutOfMemoryError | Exception e) {
								throw new RuntimeException(e);
							}
						});
					}
				}
			}
		}

		return dataModifiers;
	}

	

	private void checkForEqualOutputFiles_DB_vs_MM(Consumer<Void> dataLoader, Consumer<Void> dataModifier)	throws OutOfMemoryError, Exception {

		// create tmp subfolder in DATAFOLDER
		final String TEMPFOLDER = TestData.getTestFile.apply("").getAbsolutePath() + "/tmp";
		// new File(TEMPFOLDER).mkdirs();

		// get filename by format and storage engine
		final Function<TABLE_IMPL_TYPES, File> creFile = (type) -> 
			new File(String.format("%s/out_%s.cre", TEMPFOLDER, type.toString()));
		final BiFunction<TABLE_IMPL_TYPES, ExportFormat, File> exportFile = (type, outFormat) -> 
			new File(String.format("%s/out_%s_%s.%s", TEMPFOLDER, type.toString(), outFormat.toString(), outFormat.getFileExtension()));

		// generate all files
		for (TABLE_IMPL_TYPES type : CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
			dataLoader.accept(null);
			dataModifier.accept(null);

			for (ExportFormat outFormat : ExportFormat.values()) {
				outFormat.save(exportFile.apply(type, outFormat), UISettings.get().getIncludePubsWithoutCRs());
			}

			ExportFormat.CRE.save(creFile.apply(type), UISettings.get().getIncludePubsWithoutCRs());
		}

		// we are checking if the export output files are byte-wise equivalent
		for (ExportFormat outFormat : ExportFormat.values()) {
			assertTrue(FileUtils.contentEquals(
				exportFile.apply(TABLE_IMPL_TYPES.MM, outFormat),
				exportFile.apply(TABLE_IMPL_TYPES.DB, outFormat)));
		}

		// a cre file is a zip file --> we are pairwise checking the zipped json files
		for (String name : new String[] { "crdata", "pubdata", "crmatch" }) {

			ZipFile[] zip = new ZipFile[2];
			InputStream[] toCompare = new InputStream[2];
			for (TABLE_IMPL_TYPES type : CRTable.TABLE_IMPL_TYPES.values()) {
				zip[type.ordinal()] = new ZipFile(creFile.apply(type));
				toCompare[type.ordinal()] = zip[type.ordinal()]
						.getInputStream(zip[type.ordinal()].getEntry(name + ".json"));
			}
			System.out.println("CRE/" + name);
			assertTrue(IOUtils.contentEquals(toCompare[0], toCompare[1]));
			zip[0].close();
			zip[1].close();
		}

	}

}
