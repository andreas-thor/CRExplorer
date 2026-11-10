package cre.ui.dialog;

import cre.data.type.abs.Filter.ComparisonOperator;
import cre.data.type.abs.Filter.CumulativeFrequencyIndicator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class CumulativeFrequencyThreshold extends Dialog<CumulativeFrequencyThreshold.Selection> {

	public static class Selection {
		private final CumulativeFrequencyIndicator indicator;
		private final ComparisonOperator operator;
		private final double threshold;

		public Selection(CumulativeFrequencyIndicator indicator, ComparisonOperator operator, double threshold) {
			this.indicator = indicator;
			this.operator = operator;
			this.threshold = threshold;
		}

		public CumulativeFrequencyIndicator getIndicator() { return indicator; }
		public ComparisonOperator getOperator() { return operator; }
		public double getThreshold() { return threshold; }
	}

	private final ComboBox<CumulativeFrequencyIndicator> indicator;
	private final ComboBox<ComparisonOperator> operator;
	private final TextField threshold;

	public CumulativeFrequencyThreshold() {
		this("Filter Cited References", "Filter by Cumulative Frequencies in Percentages");
	}

	public CumulativeFrequencyThreshold(String title, String header) {
		setTitle(title);
		setHeaderText(header);
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		indicator = new ComboBox<>(FXCollections.observableArrayList(CumulativeFrequencyIndicator.values()));
		indicator.setValue(CumulativeFrequencyIndicator.CP_IN);
		operator = new ComboBox<>(FXCollections.observableArrayList(ComparisonOperator.values()));
		operator.setValue(ComparisonOperator.GTE);
		threshold = new TextField("0.0");
		threshold.setPrefColumnCount(8);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.addRow(0, new Label("Indicator"), new Label("Comparison"), new Label("Threshold (%)"));
		grid.addRow(1, indicator, operator, threshold);
		getDialogPane().setContent(grid);

		setOnShown(event -> {
			Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
			okButton.addEventFilter(ActionEvent.ACTION, action -> {
				if (!isThresholdValid()) {
					action.consume();
					Alert alert = new Alert(AlertType.ERROR);
					alert.initOwner(getDialogPane().getScene().getWindow());
					alert.setTitle("Error");
					alert.setHeaderText("Invalid Threshold");
					alert.setContentText("Please enter a number between 0 and 100.");
					alert.showAndWait();
				}
			});
			Platform.runLater(() -> threshold.requestFocus());
		});

		setResultConverter(button -> button == ButtonType.OK
				? new Selection(indicator.getValue(), operator.getValue(), parseThreshold() / 100d)
				: null);
	}

	private boolean isThresholdValid() {
		try {
			double value = parseThreshold();
			return Double.isFinite(value) && value >= 0d && value <= 100d;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private double parseThreshold() {
		return Double.parseDouble(threshold.getText().trim().replace(',', '.'));
	}
}
