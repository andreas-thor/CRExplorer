package cre;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRTable.CRTypes;
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import cre.data.type.abs.CRType;
import cre.data.type.abs.CRType.PERCENTAGE;
import cre.data.type.abs.Filter.ComparisonOperator;
import cre.data.type.abs.Filter.CumulativeFrequencyIndicator;
import cre.store.db.CRTable_DB;
import cre.store.db.OberservableCRList_DB;
import cre.store.mm.CRCluster;
import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;

public class CRIndicatorRegressionTest {

	private static final double DELTA = 0.000000001d;

	@Test
	public void indicatorsRemainStableForSmallReferenceDistribution() throws Exception {
		for (TABLE_IMPL_TYPES type : CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
			if (type == TABLE_IMPL_TYPES.DB) {
				CRTable_DB.url = "jdbc:sqlite::memory:";
				CRTable_DB.createSchemaOnStartup = true;
			}

			CRTable.get().setNpctRange(0);
			loadReferenceDistribution();

			assertArrayEquals(new int[] { 4, 4, 3 }, CRTable.get().getCR(true).mapToInt(cr -> cr.getN_CR()).toArray());
			assertArrayEquals(new int[] { 2, 3, 2 }, CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PYEARS()).toArray());

			assertArrayEquals(new double[] { 4d / 11d, 4d / 11d, 3d / 11d },
					CRTable.get().getCR(true).mapToDouble(cr -> cr.getPERC_YR()).toArray(), DELTA);
			assertArrayEquals(new double[] { 4d / 11d, 4d / 11d, 3d / 11d },
					CRTable.get().getCR(true).mapToDouble(cr -> cr.getPERC_ALL()).toArray(), DELTA);
			assertArrayEquals(new double[] { 2d / 3d, 1d, 2d / 3d },
					CRTable.get().getCR(true).mapToDouble(cr -> cr.getPYEAR_PERC()).toArray(), DELTA);

			assertArrayEquals(new double[] { 1d, 1d, 1d / 3d },
					CRTable.get().getCR(true).mapToDouble(cr -> cr.getCP_IN()).toArray(), DELTA);
			assertArrayEquals(new double[] { 1d / 3d, 1d / 3d, 0d },
					CRTable.get().getCR(true).mapToDouble(cr -> cr.getCP_EX()).toArray(), DELTA);
			assertArrayEquals(new double[] { 8d / 11d, 8d / 11d, 1d },
					CRTable.get().getCR(true).mapToDouble(cr -> cr.getCPIMP_IN()).toArray(), DELTA);
			assertArrayEquals(new double[] { 0d, 0d, 8d / 11d },
					CRTable.get().getCR(true).mapToDouble(cr -> cr.getCPIMP_EX()).toArray(), DELTA);

			assertArrayEquals(new int[] { 1, 3, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT(PERCENTAGE.P50)).toArray());
			assertArrayEquals(new int[] { 1, 1, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT(PERCENTAGE.P75)).toArray());
			assertArrayEquals(new int[] { 1, 1, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT(PERCENTAGE.P90)).toArray());
			assertArrayEquals(new int[] { 1, 1, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT(PERCENTAGE.P99)).toArray());
			assertArrayEquals(new int[] { 1, 1, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT(PERCENTAGE.P999)).toArray());

			assertArrayEquals(new int[] { 1, 0, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT_AboveAverage(PERCENTAGE.P50)).toArray());
			assertArrayEquals(new int[] { 1, 0, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT_AboveAverage(PERCENTAGE.P75)).toArray());
			assertArrayEquals(new int[] { 1, 0, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT_AboveAverage(PERCENTAGE.P90)).toArray());
			assertArrayEquals(new int[] { 1, 0, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT_AboveAverage(PERCENTAGE.P99)).toArray());
			assertArrayEquals(new int[] { 1, 0, 1 },
					CRTable.get().getCR(true).mapToInt(cr -> cr.getN_PCT_AboveAverage(PERCENTAGE.P999)).toArray());

			assertArrayEquals(new String[] { "+0-", "000", "-0+" },
					CRTable.get().getCR(true).map(cr -> cr.getSEQUENCE()).toArray(String[]::new));
			assertArrayEquals(new String[] { "", CRTypes.CP.label, "" },
					CRTable.get().getCR(true).map(cr -> cr.getTYPE()).toArray(String[]::new));
		}
	}

	@Test
	public void cumulativeFrequencyFilterUpdatesVisibilityForBothStorageEngines() throws Exception {
		for (TABLE_IMPL_TYPES type : CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
			if (type == TABLE_IMPL_TYPES.DB) {
				CRTable_DB.url = "jdbc:sqlite::memory:";
				CRTable_DB.createSchemaOnStartup = true;
			}

			CRTable.get().setNpctRange(0);
			loadReferenceDistribution();

			assertVisibility(CumulativeFrequencyIndicator.CP_IN, ComparisonOperator.LT, 0.5,
					new Boolean[] { false, false, true });
			assertVisibility(CumulativeFrequencyIndicator.CP_EX, ComparisonOperator.GTE, 1d / 3d,
					new Boolean[] { true, true, false });
			assertVisibility(CumulativeFrequencyIndicator.CPIMP_IN, ComparisonOperator.GT, 0.8,
					new Boolean[] { false, false, true });
			assertVisibility(CumulativeFrequencyIndicator.CPIMP_EX, ComparisonOperator.LTE, 0.5,
					new Boolean[] { true, true, false });
			assertVisibility(CumulativeFrequencyIndicator.CP_IN, ComparisonOperator.EQ, 1d,
					new Boolean[] { true, true, false });
		}
	}

	@Test
	public void complementaryFiltersIncludeReferencesWithoutYearForBothStorageEngines() throws Exception {
		for (TABLE_IMPL_TYPES type : CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;
			if (type == TABLE_IMPL_TYPES.DB) {
				CRTable_DB.url = "jdbc:sqlite::memory:";
				CRTable_DB.createSchemaOnStartup = true;
			}

			CRTable.get().setNpctRange(0);
			loadReferenceDistribution(true);
			long total = CRTable.get().getNumberOfCRs();

			CRTable.get().filterByCumulativeFrequency(
					CumulativeFrequencyIndicator.CPIMP_EX, ComparisonOperator.GT, 0.5);
			long greaterThan = CRTable.get().getNumberOfCRsByVisibility(true);

			CRTable.get().filterByCumulativeFrequency(
					CumulativeFrequencyIndicator.CPIMP_EX, ComparisonOperator.LTE, 0.5);
			long lessThanOrEqual = CRTable.get().getNumberOfCRsByVisibility(true);

			assertEquals(total, greaterThan + lessThanOrEqual);
		}
	}

	private void assertVisibility(CumulativeFrequencyIndicator indicator, ComparisonOperator operator,
			double threshold, Boolean[] expected) {
		CRTable.get().filterByCumulativeFrequency(indicator, operator, threshold);
		assertArrayEquals(expected, CRTable.get().getCR(true).map(cr -> cr.getVI()).toArray(Boolean[]::new));

		if (CRTable.type == TABLE_IMPL_TYPES.DB) {
			OberservableCRList_DB visibleRows = CRTable_DB.get().createNewObservableCRList_DB();
			long expectedSize = java.util.Arrays.stream(expected).filter(Boolean::booleanValue).count();
			assertEquals(expectedSize, visibleRows.size());
			for (int i = 0; i < visibleRows.size(); i++) {
				CRType<?> cr = visibleRows.get(i);
				assertTrue(cr.getVI());
				assertTrue(operator.test(getIndicatorValue(cr, indicator), threshold));
			}
		}
	}

	private double getIndicatorValue(CRType<?> cr, CumulativeFrequencyIndicator indicator) {
		switch (indicator) {
			case CP_IN: return cr.getCP_IN();
			case CP_EX: return cr.getCP_EX();
			case CPIMP_IN: return cr.getCPIMP_IN();
			case CPIMP_EX: return cr.getCPIMP_EX();
			default: throw new IllegalArgumentException("Unsupported cumulative frequency indicator: " + indicator);
		}
	}

	private void loadReferenceDistribution() {
		loadReferenceDistribution(false);
	}

	private void loadReferenceDistribution(boolean includeReferenceWithoutYear) {
		AtomicInteger crId = new AtomicInteger(0);
		CRTable.get().init();
		CRTable.get().onBeforeImport();

		addCitingPublication(2000, "A", crId);
		addCitingPublication(2000, "A", crId);
		addCitingPublication(2000, "A", crId);
		addCitingPublication(2000, "B", crId);

		addCitingPublication(2001, "A", crId);
		addCitingPublication(2001, "B", crId);
		addCitingPublication(2001, "B", crId);
		addCitingPublication(2001, "C", crId);

		addCitingPublication(2002, "B", crId);
		addCitingPublication(2002, "C", crId);
		addCitingPublication(2002, "C", crId);
		if (includeReferenceWithoutYear) {
			addCitingPublicationWithoutReferenceYear(2002, "D", crId);
		}

		CRTable.get().onAfterImport();
	}

	private void addCitingPublicationWithoutReferenceYear(int py, String citedReference, AtomicInteger crId) {
		PubType_MM pub = new PubType_MM();
		pub.setPY(py);
		CRType_MM cr = new CRType_MM();
		cr.setID(crId.incrementAndGet());
		cr.setCR(citedReference);
		cr.setCluster(new CRCluster(cr));
		pub.addCR(cr, true);
		CRTable.get().addPub(pub);
	}

	private void addCitingPublication(int py, String citedReference, AtomicInteger crId) {
		PubType_MM pub = new PubType_MM();
		pub.setPY(py);
		pub.addCR(createCR(citedReference, crId.incrementAndGet()), true);
		CRTable.get().addPub(pub);
	}

	private CRType_MM createCR(String citedReference, int id) {
		CRType_MM cr = new CRType_MM();
		cr.setID(id);
		cr.setCR(citedReference);
		cr.setRPY(2000);
		cr.setCluster(new CRCluster(cr));
		return cr;
	}
}
