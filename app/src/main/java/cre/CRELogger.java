package cre;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CRELogger {

    private static CRELogger creLogger;
    private Logger logger;


    private CRELogger() {
        this.logger = Logger.getLogger("CRELogger");
        try {
            FileHandler fh = new FileHandler("CRE.log", true);
            fh.setFormatter(new SimpleFormatter());
            this.logger.addHandler(fh);
        } catch (Exception e) {
            
        }
    }

    public static CRELogger get() {
        if (creLogger == null) {
            creLogger = new CRELogger();
        }
        return creLogger;
    }


    public void logInfo (String msg) {
        this.logger.log(Level.INFO, msg);
    }

    public void logError(String msg) {
        this.logger.log(Level.OFF, msg);
    }

}
