package cre.data.type.abs.sim;

public abstract class StringComparator {
    
    public static enum SimAlgorithm { LEV, COS, JACC }
    public static enum CosineJaccardMode { WORD, CHAR }

    public abstract double compare(String a, String b);

    public abstract SimAlgorithm getAlgorithm();

    public static StringComparator get (SimAlgorithm algorithm, CosineJaccardMode mode, int number) {

        switch (algorithm) {
            case SimAlgorithm.LEV: return new LevenshteinHelper();
            case SimAlgorithm.COS: return new CosineHelper(mode, number);
            case SimAlgorithm.JACC: return new JaccardHelper(mode, number);
            default: return null;
        }
        
    }
}
