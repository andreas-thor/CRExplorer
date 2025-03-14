package cre;

import java.io.File;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRTable.CRTypes;
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import cre.data.type.abs.CRTable.ZValueSymbol;
import cre.data.type.abs.CRType.PERCENTAGE;

public class IndicatorsExample {

	/**
	 * This test checks if the indicators are computed correctly Test results are
	 * based on the paper Thor, A.; Bornmann L.; Marx, W.; Mutz, R.: Identifying
	 * single influential publications in a research field: new analysis
	 * opportunities of the CRExplorer. Scientometrics 116(1), 2018
	 * https://dbs.uni-leipzig.de/file/Thor2018_Article_IdentifyingSingleInfluentialPu.pdf
	 */

	private final static String DATAFOLDER = "/testdata";

	@Test
	public void checkIndicatorsExample() throws OutOfMemoryError, Exception {

		for (TABLE_IMPL_TYPES type : CRTable.TABLE_IMPL_TYPES.values()) {
			CRTable.type = type;

			CRTable.get().setNpctRange(0);
			cre.format.importer.CRE.load(new File(getClass().getResource(DATAFOLDER + "/CREDAT_Typen.cre").getFile()));

			Assertions.assertArrayEquals(CRTable.get().getCR(true).mapToInt(it -> it.getN_CR()).toArray(),
					new int[] { 73, 50, 81, 76 });
			Assertions.assertArrayEquals(CRTable.get().getCR(true).mapToInt(it -> it.getN_PYEARS()).toArray(),
					new int[] { 5, 6, 5, 6 });
			Assertions.assertArrayEquals(CRTable.get().getCR(true).mapToInt(it -> it.getN_PCT(PERCENTAGE.P50)).toArray(),
					new int[] { 3, 3, 2, 4 });
			Assertions.assertArrayEquals(CRTable.get().getCR(true).mapToInt(it -> it.getN_PCT(PERCENTAGE.P75)).toArray(),
					new int[] { 2, 0, 2, 2 });
			Assertions.assertArrayEquals(CRTable.get().getCR(true).mapToInt(it -> it.getN_PCT(PERCENTAGE.P90)).toArray(),
					new int[] { 2, 0, 2, 2 });

			Assertions.assertArrayEquals(CRTable.get().getCR(true).map(it -> it.getSEQUENCE()).toArray(String[]::new),
					new String[] {
							new String(new char[] { ZValueSymbol.MINUS.label, ZValueSymbol.MINUS.label,
									ZValueSymbol.MINUS.label, ZValueSymbol.ZERO.label, ZValueSymbol.PLUS.label,
									ZValueSymbol.PLUS.label }),
							new String(new char[] { ZValueSymbol.ZERO.label, ZValueSymbol.ZERO.label,
									ZValueSymbol.ZERO.label, ZValueSymbol.ZERO.label, ZValueSymbol.ZERO.label,
									ZValueSymbol.ZERO.label }),
							new String(new char[] { ZValueSymbol.PLUS.label, ZValueSymbol.PLUS.label,
									ZValueSymbol.MINUS.label, ZValueSymbol.ZERO.label, ZValueSymbol.MINUS.label,
									ZValueSymbol.MINUS.label }),
							new String(new char[] { ZValueSymbol.MINUS.label, ZValueSymbol.MINUS.label,
									ZValueSymbol.PLUS.label, ZValueSymbol.PLUS.label, ZValueSymbol.ZERO.label,
									ZValueSymbol.MINUS.label }) });

			Assertions.assertArrayEquals(CRTable.get().getCR(true).map(it -> it.getTYPE()).toArray(String[]::new),
					new String[] { CRTypes.SB.label, CRTypes.CP.label, CRTypes.HP.label, CRTypes.SB.label });
		}

	}

}
