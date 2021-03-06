package cre.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import cre.data.type.abs.Statistics;
import cre.data.type.abs.Statistics.IntRange;
import cre.ui.UISettings;
import cre.ui.UISettings.RangeType;

public class Range extends Dialog<IntRange> {

	
	public Range(String title, String header, RangeType r, IntRange maxRange) {
		super();

		IntRange range = UISettings.get().getRange(r);
		
		// initialize property if not set
		if ((range.getMin()==Statistics.IntRange.MISSING) && (range.getMax()==Statistics.IntRange.MISSING)) {
			UISettings.get().setRange(r, maxRange);
		}
		
		setTitle(title);
		setHeaderText(header);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		
		
		TextField[] tf = new TextField[] { new TextField(String.valueOf(range.getMin())), new TextField(String.valueOf(range.getMax())) }; 
		CheckBox[] cb = new CheckBox[] { new CheckBox("Minimum"), new CheckBox("Maximum") }; 

		cb[0].setOnAction((event) -> {
		    tf[0].setDisable(cb[0].isSelected());
		    tf[0].setText(cb[0].isSelected() ? String.valueOf(maxRange.getMin()) : tf[0].getText());
		});
		cb[1].setOnAction((event) -> {
		    tf[1].setDisable(cb[1].isSelected());
		    tf[1].setText(cb[1].isSelected() ? String.valueOf(maxRange.getMax()) : tf[1].getText());
		});
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));
		grid.addRow(0, new Label("From:"), tf[0], cb[0]);
		grid.addRow(1, new Label("To:"  ), tf[1], cb[1]);
		getDialogPane().setContent(grid);
		
		
		// Request focus on first field by default.
		Platform.runLater(() -> tf[0].requestFocus());

		setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		    	
		    	if (UISettings.get().setRange(r, tf[0].getText(), tf[1].getText()) == 0) {
		    		return UISettings.get().getRange(r);
		    	}
		        // INVALID range
		        Alert alert = new Alert(AlertType.ERROR);
		        alert.setTitle("Error");
		        alert.setHeaderText("Invalid Range");
		        alert.setContentText(String.format("The range from %s to %s is not valid!", tf[0].getText(), tf[1].getText()));
		        alert.showAndWait();
		        return null; 
		    }
		    return null;	// CANCEL
		});		
	}
	
	
}
