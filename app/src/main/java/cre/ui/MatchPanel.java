package cre.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import cre.data.type.abs.Clustering;

public abstract class MatchPanel extends TitledPane {

	Button[] matchManual = new Button[3];	// buttons "same", "different", and "extract"
	Button matchUndo;						// UnDo button
	private CheckBox[] volPagDOI;					// checkboxes to use VOL, PAG, and DOI for clustering
	Slider threshold = new Slider(50,  100,  75);	// Slider for similariy threshold
	
	
	public abstract void onUpdateClustering(double threshold, boolean useClustering, boolean useVol, boolean usePag, boolean useDOI, boolean nullEqualsNull); 
	public abstract void onMatchManual(Clustering.ManualMatchType type, double threshold, boolean useVol, boolean usePag, boolean useDOI, boolean nullEqualsNull); 
	public abstract void onMatchUnDo (double threshold, boolean useVol, boolean usePag, boolean useDOI, boolean nullEqualsNull); 
	
	
	public MatchPanel() {
		super();

		// ensures that setVisible(false) hides entire panel
		managedProperty().bind(visibleProperty());
		setCollapsible(true);
		setText("Matching");

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 20, 10, 20));
		
		// Threshold slider
		threshold.setMajorTickUnit(10);
		threshold.setMinorTickCount(9);
		threshold.setShowTickLabels(true);
		threshold.setShowTickMarks(true);
		threshold.setSnapToTicks(true);
		threshold.setPrefWidth(300);
		threshold.valueProperty().addListener((ov, old_val, new_val) -> { if (!threshold.isValueChanging()) { updateClustering(); } });
		grid.add (threshold, 0, 0);

		
		// Volume CheckBoxes
		volPagDOI = new CheckBox[] { new CheckBox("Volume"), new CheckBox("Page"), new CheckBox("DOI"), new CheckBox("Treat missing values as equal") };	// was: NULL=NULL?
		GridPane g = new GridPane();
		g.setPrefWidth(200);
		for (int i=0; i<volPagDOI.length; i++) {
			volPagDOI[i].selectedProperty().addListener( (observable, oldValue, newValue) -> { updateClustering(); });
			// volPagDOI[i].setPrefWidth(300);
			g.add(volPagDOI[i], 0, i);
		}
		grid.add (g, 1, 0);
		GridPane.setMargin(g, new Insets(0, 0, 0, 30));
	
		// Match Buttons
		g = new GridPane();
		g.setPrefWidth(100);
		for (int i=0; i<Clustering.ManualMatchType.values().length; i++) {
			Clustering.ManualMatchType type = Clustering.ManualMatchType.values()[i];
			matchManual[i] = new Button (type.toString().substring(0, 1) + type.toString().substring(1).toLowerCase());	// "SAME" --> "Same"
			matchManual[i].setPrefSize(100, 25);
			matchManual[i].setOnAction(e -> { onMatchManual(type, 0.01d*threshold.getValue(), volPagDOI[0].isSelected(), volPagDOI[1].isSelected(), volPagDOI[2].isSelected(), volPagDOI[3].isSelected()); });
			g.add(matchManual[i], 0, i);

		}
		grid.add (g, 2, 0);
		GridPane.setMargin(g, new Insets(0, 0 , 0, 30));

		matchUndo = new Button("Undo");
		matchUndo.setPrefSize(100, 25);
		matchUndo.setOnAction(e -> { onMatchUnDo(0.01d*threshold.getValue(), volPagDOI[0].isSelected(), volPagDOI[1].isSelected(), volPagDOI[2].isSelected(), volPagDOI[3].isSelected()); });
		grid.add (matchUndo, 3, 0);
		GridPane.setMargin(matchUndo, new Insets(0, 0 , 0, 30));
		
		setContent(grid);
		
	}


	public void updateClustering() {
		onUpdateClustering(0.01d*threshold.getValue(), true, volPagDOI[0].isSelected(), volPagDOI[1].isSelected(), volPagDOI[2].isSelected(), volPagDOI[3].isSelected());
	}
	


	
}
