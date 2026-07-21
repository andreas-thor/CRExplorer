package cre;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CRELoggerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void writesErrorsWithTheirStacktrace() throws Exception {
        Path logDirectory = temporaryFolder.newFolder("errors").toPath();
        CRELogger logger = new CRELogger(logDirectory, 1024 * 1024, 2, false, false);

        logger.logError("Indicator update failed", new IllegalStateException("test cause"));
        assertFalse(logger.isConsoleLoggingEnabled());
        logger.close();

        String log = Files.readString(logDirectory.resolve("CRE-0.log"), StandardCharsets.UTF_8);
        assertTrue(log.contains("Indicator update failed"));
        assertTrue(log.contains("java.lang.IllegalStateException: test cause"));
    }

    @Test
    public void rotatesLogFilesAtTheConfiguredLimit() throws Exception {
        Path logDirectory = temporaryFolder.newFolder("rotation").toPath();
        CRELogger logger = new CRELogger(logDirectory, 512, 3, false, false);

        String payload = "x".repeat(256);
        for (int i = 0; i < 50; i++) {
            logger.logInfo("message=" + i + " payload=" + payload);
        }
        logger.close();

        try (var files = Files.list(logDirectory)) {
            List<Path> logFiles = files.filter(path -> path.getFileName().toString().matches("CRE-\\d+\\.log"))
                    .toList();
            assertTrue(logFiles.size() > 1);
            assertTrue(logFiles.size() <= 3);
        }
    }

    @Test
    public void writesToConsoleInAdditionToTheLogFileWhenRequested() throws Exception {
        Path logDirectory = temporaryFolder.newFolder("console").toPath();
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        PrintStream previousError = System.err;
        try {
            System.setErr(new PrintStream(console, true, StandardCharsets.UTF_8));
            CRELogger logger = new CRELogger(logDirectory, 1024, 2, false, true, Level.INFO);

            logger.logError("visible in both destinations");
            assertTrue(logger.isFileLoggingEnabled());
            assertTrue(logger.isConsoleLoggingEnabled());
            logger.close();
        } finally {
            System.setErr(previousError);
        }

        assertTrue(console.toString(StandardCharsets.UTF_8).contains("visible in both destinations"));
        assertTrue(readLog(logDirectory).contains("visible in both destinations"));
    }

    @Test
    public void enablesConsoleLoggingByDefault() throws Exception {
        String previousValue = System.clearProperty("cre.log.console");
        try {
            Path logDirectory = temporaryFolder.newFolder("console-default").toPath();
            CRELogger logger = new CRELogger(logDirectory, 1024, 2, false);

            assertTrue(logger.isConsoleLoggingEnabled());
            logger.close();
        } finally {
            if (previousValue != null) {
                System.setProperty("cre.log.console", previousValue);
            }
        }
    }

    @Test
    public void fallsBackToConsoleIfTheLogDirectoryCannotBeCreated() throws Exception {
        Path regularFile = temporaryFolder.newFile("not-a-directory").toPath();
        CRELogger logger = new CRELogger(regularFile, 1024, 2, false, false);

        assertFalse(logger.isFileLoggingEnabled());
        assertTrue(logger.isConsoleLoggingEnabled());
        logger.close();
    }

    @Test
    public void filtersDebugMessagesAtInfoLevel() throws Exception {
        Path logDirectory = temporaryFolder.newFolder("info-level").toPath();
        CRELogger logger = new CRELogger(logDirectory, 1024 * 1024, 2, false, false, Level.INFO);

        logger.logDebug("hidden debug message");
        logger.logInfo("visible info message");
        logger.close();

        String log = readLog(logDirectory);
        assertFalse(log.contains("hidden debug message"));
        assertTrue(log.contains("visible info message"));
    }

    @Test
    public void writesDebugMessagesAtFineLevel() throws Exception {
        Path logDirectory = temporaryFolder.newFolder("fine-level").toPath();
        CRELogger logger = new CRELogger(logDirectory, 1024 * 1024, 2, false, false, Level.FINE);

        logger.logDebug("visible debug message");
        logger.close();

        assertTrue(readLog(logDirectory).contains("visible debug message"));
    }

    @Test
    public void suppressesConsoleOutputWhenDisabled() throws Exception {
        Path logDirectory = temporaryFolder.newFolder("no-console").toPath();
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        PrintStream previousError = System.err;
        try {
            System.setErr(new PrintStream(console, true, StandardCharsets.UTF_8));
            CRELogger logger = new CRELogger(logDirectory, 1024 * 1024, 2, false, false, Level.INFO);
            logger.logError("file only message");
            logger.close();
        } finally {
            System.setErr(previousError);
        }

        assertTrue(console.toString(StandardCharsets.UTF_8).isEmpty());
        assertTrue(readLog(logDirectory).contains("file only message"));
    }

    @Test
    public void writesConcurrentMessagesWithoutLosingEntries() throws Exception {
        Path logDirectory = temporaryFolder.newFolder("concurrent").toPath();
        CRELogger logger = new CRELogger(logDirectory, 10 * 1024 * 1024, 2, false, false, Level.INFO);
        ExecutorService executor = Executors.newFixedThreadPool(8);

        for (int i = 0; i < 200; i++) {
            int messageNumber = i;
            executor.submit(() -> logger.logInfo("concurrent-message-" + messageNumber));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        logger.close();

        String log = readLog(logDirectory);
        for (int i = 0; i < 200; i++) {
            assertTrue(log.contains("concurrent-message-" + i));
        }
    }

    @Test
    public void releasesTheLogFileWhenClosed() throws Exception {
        Path logDirectory = temporaryFolder.newFolder("close").toPath();
        CRELogger logger = new CRELogger(logDirectory, 1024 * 1024, 2, false, false, Level.INFO);
        logger.logInfo("before close");
        logger.close();

        Path renamedLog = logDirectory.resolve("closed.log");
        Files.move(logDirectory.resolve("CRE-0.log"), renamedLog);
        assertTrue(Files.exists(renamedLog));
    }

    private String readLog(Path logDirectory) throws Exception {
        return Files.readString(logDirectory.resolve("CRE-0.log"), StandardCharsets.UTF_8);
    }
}
