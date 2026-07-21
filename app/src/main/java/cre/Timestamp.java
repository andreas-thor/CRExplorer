package cre;

public class Timestamp {
    
    static long lastMilli = -1;
    static long lastMemory = -1;

    public static void ts(String label) {
     
        long currentMilli = System.currentTimeMillis();
		// long currentMemory = Runtime.getRuntime().totalMemory();

        String elapsed = lastMilli == -1
                ? ""
                : String.format(", elapsedSeconds=%.3f", (currentMilli-lastMilli)/1000d);
        CRELogger.get().logDebug(String.format("Timestamp: label=%s, epochMillis=%d%s", label, currentMilli, elapsed));

        lastMilli = currentMilli;
        // lastMemory = currentMemory;
    }
}
