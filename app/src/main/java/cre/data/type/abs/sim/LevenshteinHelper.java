package cre.data.type.abs.sim;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

public class LevenshteinHelper extends StringComparator {

    StringMetric l;
    

    public LevenshteinHelper() {
        this.l = StringMetrics.levenshtein();

    }

    @Override
    public double compare(String a, String b) {
        return this.l.compare(a, b);
    }

    @Override
    public SimAlgorithm getAlgorithm() {
        return SimAlgorithm.LEV;
    }
    
}
