package cre.data.type.abs;

import java.util.List;

import cre.data.type.abs.Statistics.IntRange;

public interface Filter {

	public enum CumulativeFrequencyIndicator {
		CP_IN("CP_IN"),
		CP_EX("CP_EX"),
		CPIMP_IN("CPIMP_In"),
		CPIMP_EX("CPIMP_EX");

		private final String label;

		CumulativeFrequencyIndicator(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public enum ComparisonOperator {
		LT("<") {
			@Override public boolean test(double value, double threshold) { return value < threshold; }
		},
		LTE("<=") {
			@Override public boolean test(double value, double threshold) { return value <= threshold; }
		},
		EQ("=") {
			@Override public boolean test(double value, double threshold) { return value == threshold; }
		},
		GTE(">=") {
			@Override public boolean test(double value, double threshold) { return value >= threshold; }
		},
		GT(">") {
			@Override public boolean test(double value, double threshold) { return value > threshold; }
		};

		private final String symbol;

		ComparisonOperator(String symbol) {
			this.symbol = symbol;
		}

		public abstract boolean test(double value, double threshold);

		@Override
		public String toString() {
			return symbol;
		}
	}
    
    
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public abstract void filterByYear (IntRange range);

	/**
	 * Filter cited references by a cumulative frequency indicator. Indicator
	 * values and the threshold use the internal range from 0.0 to 1.0.
	 */
	public abstract void filterByCumulativeFrequency(CumulativeFrequencyIndicator indicator,
			ComparisonOperator operator, double threshold);
	
	public abstract void filterByCluster (List<Integer> sel);
	
	public abstract void setShowNull (boolean showNull);
	
	public abstract void showAll();
	

    
}
