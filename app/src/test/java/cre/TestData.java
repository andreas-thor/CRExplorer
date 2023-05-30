package cre;

import java.io.File;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.Statistics.IntRange;
import cre.format.exporter.ExportFormat;
import cre.format.importer.ImportFormat;
import cre.ui.dialog.Sampling;

/**
 * Helper Class to give access to TestData
 */

public class TestData {


    private final static String testDataDir = new File(TestData.class.getResource("/testdata/").getFile()).getAbsolutePath();

    /**
     * Get file from file name
     */
    public static Function<String, File> getTestFile = fileName -> 
        new File(TestData.class.getResource("/testdata/" + fileName).getFile());



    public static File getFile (String fileName) {
        return getFile(null, fileName);
    }

    public static File getFile (String dir, String fileName) {
        
        String fileDir = testDataDir + ((dir!=null) ? "/" + dir : "");
        if (!new File(fileDir).exists()) {
            new File(fileDir).mkdirs();
        }
        return new File(fileDir + "/" + fileName);
    }
     

    /**
     * Fully import all files (with no restrictions and no sampling)
     */
    public static final BiFunction<ImportFormat, File[], Consumer<Void>> getImportDataLoader = (format, files) -> 
        (empty) -> {
            try {
                format.load(Arrays.stream(files).collect(Collectors.toList()), new IntRange(IntRange.NONE, IntRange.NONE),
                        true, new IntRange(IntRange.NONE, IntRange.NONE), true, 0, Sampling.NONE);
            } catch (OutOfMemoryError | Exception e) {
                throw new RuntimeException(e);
            }
        };


    public static final BiFunction<ExportFormat, String, Consumer<Void>> getExportDataSaver = (format, fileName) ->
        (empty) -> {
            try {
                format.save(new File (fileName), true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

    /**
     * Fully load CRE file
     */
    public static final Function<String, Consumer<Void>> getCREDataLoader = (file) -> 
        (empty) -> {
            try {
                CRTable.get().getReader().load(getTestFile.apply(file));
            } catch (OutOfMemoryError | Exception e) {
                throw new RuntimeException(e);
            }
        };


    

}