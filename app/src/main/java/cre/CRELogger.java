package cre;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class CRELogger implements AutoCloseable {

    static final int DEFAULT_LOG_FILE_SIZE = 5 * 1024 * 1024;
    static final int DEFAULT_LOG_FILE_COUNT = 5;

    private final Logger logger;
    private final List<Handler> handlers;
    private final Path logDirectory;
    private final boolean fileLoggingEnabled;
    private final boolean consoleLoggingEnabled;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private static class Holder {
        private static final CRELogger INSTANCE = new CRELogger();
    }

    private CRELogger() {
        this(resolveLogDirectory(), DEFAULT_LOG_FILE_SIZE, DEFAULT_LOG_FILE_COUNT, true,
                resolveConsoleLoggingEnabled());
    }

    CRELogger(Path logDirectory, int logFileSize, int logFileCount, boolean registerShutdownHook) {
        this(logDirectory, logFileSize, logFileCount, registerShutdownHook, resolveConsoleLoggingEnabled());
    }

    CRELogger(Path logDirectory, int logFileSize, int logFileCount, boolean registerShutdownHook,
            boolean consoleLoggingRequested) {
        this(logDirectory, logFileSize, logFileCount, registerShutdownHook, consoleLoggingRequested,
                resolveLogLevel());
    }

    CRELogger(Path logDirectory, int logFileSize, int logFileCount, boolean registerShutdownHook,
            boolean consoleLoggingRequested, Level logLevel) {
        this.logger = Logger.getAnonymousLogger();
        this.logger.setUseParentHandlers(false);
        this.logger.setLevel(logLevel);
        this.logDirectory = logDirectory;

        List<Handler> configuredHandlers = new ArrayList<>();
        boolean fileHandlerConfigured = false;
        Exception fileHandlerError = null;
        try {
            configuredHandlers.add(createFileHandler(logDirectory, logFileSize, logFileCount));
            fileHandlerConfigured = true;
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            fileHandlerError = e;
        }

        boolean consoleHandlerConfigured = consoleLoggingRequested || !fileHandlerConfigured;
        if (consoleHandlerConfigured) {
            configuredHandlers.add(createConsoleHandler());
        }
        configuredHandlers.forEach(this.logger::addHandler);

        if (fileHandlerError != null) {
            this.logger.log(Level.WARNING,
                    "Could not initialize file logging in " + logDirectory + "; using console logging instead.",
                    fileHandlerError);
        }

        this.handlers = List.copyOf(configuredHandlers);
        this.fileLoggingEnabled = fileHandlerConfigured;
        this.consoleLoggingEnabled = consoleHandlerConfigured;

        if (registerShutdownHook) {
            try {
                Runtime.getRuntime().addShutdownHook(new Thread(this::close, "cre-logger-shutdown"));
            } catch (IllegalStateException | SecurityException e) {
                this.logger.log(Level.WARNING, "Could not register logger shutdown hook.", e);
            }
        }
    }

    private static FileHandler createFileHandler(Path logDirectory, int logFileSize, int logFileCount)
            throws IOException {
        Files.createDirectories(logDirectory);
        String pattern = logDirectory.resolve("CRE-%g.log").toString();
        FileHandler fileHandler = new FileHandler(pattern, logFileSize, logFileCount, true);
        configureHandler(fileHandler);
        return fileHandler;
    }

    private static ConsoleHandler createConsoleHandler() {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        configureHandler(consoleHandler);
        return consoleHandler;
    }

    private static void configureHandler(Handler handler) {
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        try {
            handler.setEncoding(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new IllegalStateException("UTF-8 logging is not supported.", e);
        }
    }

    private static Path resolveLogDirectory() {
        String configuredDirectory = System.getProperty("cre.log.dir");
        if (configuredDirectory != null && !configuredDirectory.isBlank()) {
            try {
                return Path.of(configuredDirectory);
            } catch (RuntimeException e) {
                System.err.println("Invalid cre.log.dir value; using the default log directory: " + e.getMessage());
            }
        }
        String userHome = System.getProperty("user.home");
        return userHome == null || userHome.isBlank()
                ? Path.of(".", "logs")
                : Path.of(userHome, ".crexplorer", "logs");
    }

    private static Level resolveLogLevel() {
        String configuredLevel = System.getProperty("cre.log.level", Level.INFO.getName());
        try {
            return Level.parse(configuredLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid cre.log.level value; using INFO: " + configuredLevel);
            return Level.INFO;
        }
    }

    private static boolean resolveConsoleLoggingEnabled() {
        return Boolean.parseBoolean(System.getProperty("cre.log.console", Boolean.TRUE.toString()));
    }

    public static CRELogger get() {
        return Holder.INSTANCE;
    }

    public Path getLogDirectory() {
        return logDirectory;
    }

    boolean isFileLoggingEnabled() {
        return fileLoggingEnabled;
    }

    boolean isConsoleLoggingEnabled() {
        return consoleLoggingEnabled;
    }

    public void logDebug(String msg) {
        logger.log(Level.FINE, msg);
    }

    public void logInfo(String msg) {
        logger.log(Level.INFO, msg);
    }

    public void logWarning(String msg) {
        logger.log(Level.WARNING, msg);
    }

    public void logWarning(String msg, Throwable error) {
        logger.log(Level.WARNING, msg, error);
    }

    public void logError(String msg) {
        logger.log(Level.SEVERE, msg);
    }

    public void logError(String msg, Throwable error) {
        logger.log(Level.SEVERE, msg, error);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            for (Handler handler : handlers) {
                logger.removeHandler(handler);
                handler.flush();
                handler.close();
            }
        }
    }
}
