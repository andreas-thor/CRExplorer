package cre.data.type.extern;

import java.util.function.Function;

import cre.data.type.abs.CRType;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class CRType_ColumnView {

	// Column Information
	public static enum ColGroup { CR, INDICATOR, CLUSTER, SEARCH }

	public static enum ColDataType { INT, DOUBLE, STRING /*, CRCLUSTER*/ }

	public static enum CRColumn {
			
			ID 	("ID", "ID", "CR_ID", ColGroup.CR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getID())),
			CR 	("CR", "Cited Reference", "CR_CR", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getCR())),
			RPY ("RPY", "Reference Publication Year", "CR_RPY", ColGroup.CR, ColDataType.INT, cr -> new SimpleObjectProperty<Integer>(cr.getRPY())),
			N_CR ("N_CR", "Number of Cited References", "CR_N_CR", ColGroup.CR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_CR())),
			PERC_YR ("PERC_YR", "Percent in Year", "CR_PERC_YR", ColGroup.INDICATOR, ColDataType.DOUBLE, cr -> new SimpleDoubleProperty(cr.getPERC_YR())),
			PERC_ALL ("PERC_ALL", "Percent over all Years", "CR_PERC_ALL", ColGroup.INDICATOR, ColDataType.DOUBLE, cr -> new SimpleDoubleProperty(cr.getPERC_ALL())),
			AU ("AU", "Author", "CR_AU", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getAU())),
			AU_L ("AU_L", "Last Name", "CR_AU_L", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getAU_L())),
			AU_F ("AU_F", "First Name Initial", "CR_AU_F", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getAU_F())),
			AU_A ("AU_A", "Authors", "CR_AU_A", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getAU_A())),
			TI ("TI", "Title", "CR_TI", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getTI())),
			J ("J", "Source", "CR_J", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getJ())),
			J_N ("J_N", "Source Title", "CR_J_N", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getJ_N())),
			J_S ("J_S", "Title short", "CR_J_S", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getJ_S())),
			VOL ("VOL", "Volume", "CR_VOL", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty (cr.getVOL())),
			PAG ("PAG", "Page", "CR_PAG", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getPAG())),
			DOI ("DOI", "DOI", "CR_DOI", ColGroup.CR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getDOI())),
			CID2 ("CID2", "ClusterID", "(CR_ClusterId1, CR_ClusterId2)", ColGroup.CLUSTER, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getClusterId())),
			CID_S ("CID_S", "ClusterSize", "CR_ClusterSize", ColGroup.CLUSTER, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getClusterSize())),
			
			N_PYEARS ("N_PYEARS", "Number of Citing Years", "CR_N_PYEARS", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PYEARS())),
			PYEAR_PERC ("PERC_PYEARS", "Percentage of Citing Years", "CR_PYEAR_PERC", ColGroup.INDICATOR, ColDataType.DOUBLE, cr -> new SimpleDoubleProperty(cr.getPYEAR_PERC())),
			
			N_PCT50 ("N_TOP50", "Top 50% Cited Reference", "CR_N_PCT_P50", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P50))),
			N_PCT75 ("N_TOP25", "Top 25% Cited Reference", "CR_N_PCT_P75", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P75))),
			N_PCT90 ("N_TOP10", "Top 10% Cited Reference", "CR_N_PCT_P90", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P90))),
			N_PCT99 ("N_TOP1", "Top 1% Cited Reference", "CR_N_PCT_P99", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P99))),
			N_PCT999 ("N_TOP0_1", "Top 0.1% Cited Reference", "CR_N_PCT_P999", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT(CRType.PERCENTAGE.P999))),
			
			N_PCT50_AboveAverage ("N_TOP50+", "Top 50% Cited Reference & Above Average", "CR_N_PCT_AboveAverage_P50", ColGroup.INDICATOR, ColDataType.INT,  cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P50))),
			N_PCT75_AboveAverage ("N_TOP25+", "Top 25% Cited Reference & Above Average", "CR_N_PCT_AboveAverage_P75", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P75))),
			N_PCT90_AboveAverage ("N_TOP10+", "Top 10% Cited Reference & Above Average", "CR_N_PCT_AboveAverage_P90", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P90))),
			N_PCT99_AboveAverage ("N_TOP1+", "Top 1% Cited Reference & Above Average", "CR_N_PCT_AboveAverage_P99", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P99))),
			N_PCT999_AboveAverage ("N_TOP0_1+", "Top 0.1% Cited Reference & Above Average", "CR_N_PCT_AboveAverage_P999", ColGroup.INDICATOR, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getN_PCT_AboveAverage(CRType.PERCENTAGE.P999))),
			
			SEQUENCE  ("SEQUENCE", "Sequence", "CR_SEQUENCE", ColGroup.INDICATOR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getSEQUENCE())),
			TYPE  ("TYPE", "Type", "CR_TYPE", ColGroup.INDICATOR, ColDataType.STRING, cr -> new SimpleStringProperty(cr.getTYPE())),
			SEARCH_SCORE  ("SEARCH_SCORE", "Score from Search Process", null, ColGroup.SEARCH, ColDataType.INT, cr -> new SimpleIntegerProperty(cr.getSEARCH_SCORE()));
			
			public String id;
			public String title;
			public String sqlName;
			public ColGroup group;	
			public ColDataType type;
			public Function<CRType<?>, ObservableValue<?>> prop;
			
			CRColumn(String id, String title, String sqlName, ColGroup group, ColDataType type, Function<CRType<?>, ObservableValue<?>> prop) {
				this.id = id;
				this.title = title;
				this.sqlName = sqlName;
				this.group = group;
				this.type = type;
				this.prop = prop;
			}
		}

}
