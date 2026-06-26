package cre;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRTable.CRTypes;
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import cre.data.type.abs.CRType.PERCENTAGE;
import cre.store.db.CRTable_DB;
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

	private void loadReferenceDistribution() {
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

		CRTable.get().onAfterImport();
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
