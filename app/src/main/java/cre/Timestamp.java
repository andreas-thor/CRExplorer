package cre;

import java.time.LocalTime;

public class Timestamp {
    
    static long lastMilli = -1;
    static long lastMemory = -1;

    public static void ts(String label) {
     
        long currentMilli = System.currentTimeMillis();
		// long currentMemory = Runtime.getRuntime().totalMemory();

        System.out.println("==================================");
        System.out.println(label);

        LocalTime currentTime = LocalTime.now();
		System.out.println("Aktuelle Uhrzeit       : " + currentTime);		
        // System.out.println("Aktueller Speicher (MB): " + (currentMemory/1024d/1024d));

        
        if (lastMilli != -1) {
            System.out.println("Verstrichene Zeit in Sekunden: " + ((currentMilli-lastMilli)/1000d) );
            // System.out.println("Verbrauchter Speicher in MB  : " + ((currentMemory-lastMemory)/1024d/1024d));
        }

        lastMilli = currentMilli;
        // lastMemory = currentMemory;
    }
}
