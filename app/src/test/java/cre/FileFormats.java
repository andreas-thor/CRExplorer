package cre;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRType;
import cre.data.type.abs.Statistics;
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import cre.format.exporter.ExportFormat;
import cre.format.exporter.Scopus;
import cre.format.importer.ImportFormat;

/**
 * FileFormats
 */
public class FileFormats {

    private long[] getTableStatistics() {
        Statistics s = CRTable.get().getStatistics();
        return new long[] { 
            s.getNumberOfCRs(),
            s.getNumberOfPubs(),
            s.getNumberOfPubs(true),
            s.getNumberOfPubs(false),
            s.getMaxRangePY().getMin(),
            s.getMaxRangePY().getMax(),
            s.getMaxRangePY().getSize(),
            s.getNumberOfDistinctPY(),
            s.getMaxRangeNCR().getMin(),
            s.getMaxRangeNCR().getMax(),
            s.getMaxRangeNCR().getSize(),
            s.getMaxRangeRPY().getMin(),
            s.getMaxRangeRPY().getMax(),
            s.getMaxRangeRPY().getSize(),
            s.getMaxRangeRPY(true).getMin(),
            s.getMaxRangeRPY(true).getMax(),
            s.getMaxRangeRPY(true).getSize(),
            s.getMaxRangeRPY(false).getMin(),
            s.getMaxRangeRPY(false).getMax(),
            s.getMaxRangeRPY(false).getSize(),
            s.getNumberOfDistinctRPY(),
            s.getNumberOfCRsByVisibility(true),
            s.getNumberOfCRsByVisibility(false),
            s.getNumberOfCRsWithoutRPY() 
        };
    }

    // final BiFunction<String, String, File> exportFile = (dir, filename) -> {

    @Test
    public void checkFileFormatEquivalence() throws IOException {

        // System.out.println("HHHHH");
        // return;
        
        CRTable.type = TABLE_IMPL_TYPES.MM;


        Map<ExportFormat, ImportFormat> formats = Map.of(
            // ExportFormat.WOS, ImportFormat.WOS,
                ExportFormat.SCOPUS, ImportFormat.SCOPUS
            );


        formats.forEach((exportFormat, importFormat) -> 
            exportAndImportAgain (
                TestData.getImportDataLoader.apply(ImportFormat.WOS, Stream.of("savedrecs_JOI1.txt" /*, "savedrecs_JOI2.txt"*/).map(TestData::getFile).toArray(File[]::new)),
                exportFormat, importFormat));
                

    }


    private void exportAndImportAgain (Consumer<Void> dataLoader, ExportFormat exportFormat, ImportFormat importFormat)  {

        System.out.println(String.format("exportAndImportAgain: %s %s", exportFormat.toString(), importFormat.toString()));
        dataLoader.accept(null);
        long[] stats_orig = getTableStatistics();


        Set<CRType> allCRs_orig = CRTable.get().getCR().collect (Collectors.toSet());
        System.out.println(String.format ("Sum N_CR orig %d" , allCRs_orig.stream().mapToInt(cr -> cr.getN_CR()).sum()));
        

        System.out.println(Arrays.toString(stats_orig));
        
        File tmp = TestData.getFile("tmp", "export.tmp");
        try {
            exportFormat.save(tmp, true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        TestData.getImportDataLoader.apply(importFormat, new File[] {tmp}).accept(null);
        
        long[] stats_copy = getTableStatistics();
        System.out.println(Arrays.toString(stats_copy));

        Set<CRType> allCRs_copy = CRTable.get().getCR()./*map(cr -> cr.getCR()).*/collect (Collectors.toSet());
        System.out.println(String.format ("Sum N_CR copy %d" , allCRs_copy.stream().mapToInt(cr -> cr.getN_CR()).sum()));


        // allCRs_orig.stream()
        //     .filter(crString -> !allCRs_copy.contains(crString))
        //     .forEach(crString -> System.out.println(crString));


        Set<String> allCRs_orig_String = allCRs_orig.stream().map(cr -> Scopus.generateCRString(cr).replaceAll(";", ",")).collect(Collectors.toSet());
        Set<String> allCRs_copy_String = allCRs_copy.stream().map(cr -> cr.getCR()).collect(Collectors.toSet());

        System.out.println(String.format("allCRs_orig size = %d", allCRs_orig.size()));
        System.out.println(String.format("allCRs_copy size = %d", allCRs_copy.size()));
        System.out.println(String.format("allCRs_orig_string size = %d", allCRs_orig_String.size()));
        System.out.println(String.format("allCRs_copy_string size = %d", allCRs_copy_String.size()));

        allCRs_orig.stream().forEach(cr1 -> {

            allCRs_orig.stream()
                .filter(cr2 -> cr1!=cr2)
                .filter(cr2 -> Scopus.generateCRString(cr1).replaceAll(";", ",").equals(Scopus.generateCRString(cr2).replaceAll(";", ",")))
                .forEach (cr2 -> 
                    System.out.println(String.format("%s\n   %s\n   %s\n   %s\n   %s", 
                        Scopus.generateCRString(cr1).replaceAll(";", ","), 
                        cr1.getCR(), 
                        cr2.getCR(),
                        Optional.ofNullable(cr1.getJ_N()).orElse(Optional.ofNullable(cr1.getJ_S()).orElse(cr1.getJ())),
                        Optional.ofNullable(cr2.getJ_N()).orElse(Optional.ofNullable(cr2.getJ_S()).orElse(cr2.getJ()))
                    )));

        });



        allCRs_orig_String.removeAll(allCRs_copy_String);
        System.out.println(String.format ("Missing size = %d", allCRs_orig_String.size()));
        allCRs_orig_String.stream().forEach(s -> System.out.println(s));

        assertArrayEquals(stats_orig, stats_copy);


    }

}