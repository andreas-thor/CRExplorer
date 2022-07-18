package cre;


/**
 * This is just a helper class / launcher that launches the actual JavaFX-based application
 * It is needed for fat jar where the main class can not be derived from Application.
 */
public class CitedReferencesExplorer {
    
    public static void main(String[] args) {
        CitedReferencesExplorerFX.main(args);
    }
}
