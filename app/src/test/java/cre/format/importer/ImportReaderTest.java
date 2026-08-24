package cre.format.importer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import cre.store.mm.PubType_MM;

public class ImportReaderTest {

    @Test
    public void propagatesAnIOExceptionInsteadOfTreatingItAsEndOfInput() throws Exception {
        IOException readError = new IOException("simulated read failure");
        AtomicInteger reads = new AtomicInteger();
        ImportReader reader = new ImportReader() {
            @Override
            protected void computeNextEntry() throws IOException {
                if (reads.getAndIncrement() == 0) {
                    entry = new PubType_MM();
                } else {
                    throw readError;
                }
            }
        };

        reader.init(new ByteArrayInputStream(new byte[0]));
        UncheckedIOException thrown = assertThrows(UncheckedIOException.class, reader::next);

        assertSame(readError, thrown.getCause());
        assertFalse(reader.hasNext());
        reader.close();
    }
}
