package cre.data.type.abs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.Test;

public class CumulativeImpactPercentageTest {

	private static final double DELTA = 0.000000000001d;

	@Test
	public void computesExampleDistribution() {
		double[][] result = CRTable.computeCumulativeImpactPercentages(new int[] { 5, 7, 18 });

		assertArrayEquals(new double[] { 1d, 25d / 30d, 18d / 30d }, result[0], DELTA);
		assertArrayEquals(new double[] { 25d / 30d, 18d / 30d, 0d }, result[1], DELTA);
	}

	@Test
	public void includesOrExcludesAllTiedImpact() {
		double[][] result = CRTable.computeCumulativeImpactPercentages(new int[] { 4, 4, 3 });

		assertArrayEquals(new double[] { 8d / 11d, 8d / 11d, 1d }, result[0], DELTA);
		assertArrayEquals(new double[] { 0d, 0d, 8d / 11d }, result[1], DELTA);
	}

	@Test
	public void returnsZeroWhenTotalImpactIsZero() {
		double[][] result = CRTable.computeCumulativeImpactPercentages(new int[] { 0, 0, 0 });

		assertArrayEquals(new double[] { 0d, 0d, 0d }, result[0], DELTA);
		assertArrayEquals(new double[] { 0d, 0d, 0d }, result[1], DELTA);
	}

	@Test
	public void usesLongForImpactSums() {
		int max = Integer.MAX_VALUE;
		long total = 2L * max + 1L;
		double[][] result = CRTable.computeCumulativeImpactPercentages(new int[] { max, max, 1 });

		assertArrayEquals(new double[] { 1d - 1d / total, 1d - 1d / total, 1d }, result[0], DELTA);
		assertArrayEquals(new double[] { 0d, 0d, 1d - 1d / total }, result[1], DELTA);
	}
}
