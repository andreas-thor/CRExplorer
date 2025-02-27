package cre.data.type.abs;

import java.util.HashMap;
import java.util.Map;

public interface Statistics {

	public class IntRange {
		
		public static final int MISSING = -1;
		public static final int NONE = 0;
		
		final private int min;
		final private int max;
		
		public IntRange() {
			this (NONE, NONE);
		}

		public IntRange(int value) {
			this (value, value);
		}

		public IntRange(long min, long max) {
			this ((int)min, (int)max);
		}
		
		public IntRange(int[] values) {
			this(values[0], values[1]);
		}
		
		public IntRange(int min, int max) {
			this.min = min;
			this.max = max;
		}
		
		
		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}
		
		public int getSize() {
			return max-min+1;
		}

		public int[] toArray() {
			return new int[]{min, max};
		}

		@Override
		public String toString() {
			return "[" + this.min + "," + this.max + "]";
		}
	}
	

	
	
	
	public long getNumberOfCRs ();
	
	public long getNumberOfPubs ();

	public long getNumberOfPubs (boolean includePubsWithoutCRs);
	
	public IntRange getMaxRangePY ();

	public int getNumberOfDistinctPY ();

	public IntRange getMaxRangeNCR ();

	public IntRange getMaxRangeRPY ();

	public IntRange getMaxRangeRPY (boolean visibleOnly);

	public int getNumberOfDistinctRPY ();
	
	public int getNumberOfCRsByVisibility (boolean visible);
	
	
	/**
	 * Count / Remove public abstractations based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public long getNumberOfCRsByNCR (IntRange range);

	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	public long getNumberOfCRsByPercentYear (String comp, double threshold);

	/**
	 * Count / Remove publications based on year range (from <= x <= to)	
	 * Property "removed" is set for cascading deletions (e.g., Citing Publications) 
	 * @param from
	 * @param to
	 */
	
	public long getNumberOfCRsByRPY (IntRange range);

	

	public long getNumberOfPubsByCitingYear (IntRange range);

	public int getNumberOfCRsWithoutRPY ();


	default Map<String, Object> toMap() {

		HashMap<String, Object> result = new HashMap<>();
		result.put("NumberOfCRs", getNumberOfCRs());
		result.put("NumberOfPubs(includePubsWithoutCRs=true)", getNumberOfPubs (true)); 
		result.put("NumberOfPubs(includePubsWithoutCRs=false)", getNumberOfPubs (false)); 
		result.put("MaxRangePY", getMaxRangePY ()); 
		result.put("NumberOfDistinctPY", getNumberOfDistinctPY ());
		result.put("MaxRangeNCR", getMaxRangeNCR ()); 
		result.put("MaxRangeRPY", getMaxRangeRPY ()); 
		result.put("NumberOfDistinctRPY", getNumberOfDistinctRPY ()); 
		return result;

	}
	
}
