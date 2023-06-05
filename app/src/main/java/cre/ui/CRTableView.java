package cre.ui;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

import cre.data.type.abs.CRType;
import cre.data.type.extern.CRType_ColumnView;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public abstract class CRTableView<C> extends TableView<C> {

	private TableColumn<C, ?>[] columns;


	abstract public int getFirstRowByYear(int year);

	public abstract void updateTableViewData ();


	@SuppressWarnings("unchecked")
	public CRTableView() {

		setMinHeight(100);
		setMinWidth(100);
		GridPane.setVgrow(this, Priority.ALWAYS);
		GridPane.setHgrow(this, Priority.ALWAYS);


		columns = Arrays.stream(CRType_ColumnView.CRColumn.values())
			.map(col -> {
				TableColumn<CRType<?>, ? extends Serializable> tabCol = null;
				switch (col.type) {
					case INT:
						tabCol = new TableColumn<CRType<?>, Number>(col.id);
						((TableColumn<CRType<?>, Number>) tabCol).setCellValueFactory(cellData -> (ObservableValue<Number>) col.prop.apply (cellData.getValue()));
					 	break;
					case DOUBLE:
						tabCol = new TableColumn<CRType<?>, Number>(col.id);
						((TableColumn<CRType<?>, Number>) tabCol).setCellValueFactory(cellData -> (ObservableValue<Number>) col.prop.apply (cellData.getValue()));
						((TableColumn<CRType<?>, Number>) tabCol).setCellFactory(column ->
							new TableCell<CRType<?>, Number>() {
								@Override
								protected void updateItem(Number value , boolean empty) {
									super.updateItem(value, empty);
									setText (((value == null) || empty) ? null : UISettings.get().getFormat().format(value.doubleValue()));
								}
							}
						);
					 	break;
					case STRING:
						tabCol = new TableColumn<CRType<?>, String>(col.id);
						((TableColumn<CRType<?>, String>) tabCol).setCellValueFactory(cellData -> (ObservableValue<String>) col.prop.apply (cellData.getValue()));
						((TableColumn<CRType<?>, String>) tabCol).setComparator((o1, o2) -> {
							if (o1==null) return 1;
							if (o2==null) return -1;
							return o1.compareToIgnoreCase(o2);
						});
					default: assert false;
				}
				tabCol.setUserData(col);
				tabCol.visibleProperty().bindBidirectional(UISettings.get().getColumnVisibleProperty(col));
				return tabCol;
			}).toArray(TableColumn[]::new);



		getColumns().addAll(columns);
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

/*


		// if we use a Storage Engine with custom sorting (e.g., db) --> we forward the sort operation to it (otherwise, the TableView performs sorting)

		
		if  (((L) getItems()).hasCustomSorting()) {

			addEventHandler(SortEvent.ANY, event -> {

				System.out.println("HEY, Sorted???");
				System.out.println (((CRTableView<C,L>) event.getSource()).getSortOrder().size());

				((ObservableCRList<?>) getItems()).setSortOrder (
					((CRTableView<C,L>) event.getSource()).getSortOrder().stream()
						.map(o -> String.format("%s %s", ((CRType_ColumnView.CRColumn) o.getUserData()).sqlName,  o.getSortType() == TableColumn.SortType.DESCENDING ? " DESC" : ""))
						.collect( Collectors.joining(", ") )
				);

				refresh();
				event.consume();

			});
		}

*/		
	}





	public void orderBySearchResult () {

		/* remove SEARCH_SCORE as order criteria */
		for (int i=getSortOrder().size()-1; i>=0; i--) {
			if (getSortOrder().get(i).getText().equals(CRType_ColumnView.CRColumn.SEARCH_SCORE.id)) {
				getSortOrder().remove(i);
			}
		}

		/* sort by search first; remains other (if existing) search criteria */
		columns[CRType_ColumnView.CRColumn.SEARCH_SCORE.ordinal()].setSortType(TableColumn.SortType.DESCENDING);
		getSortOrder().add(0, columns[CRType_ColumnView.CRColumn.SEARCH_SCORE.ordinal()]);
		sort();

		Optional<C> first = getItems().stream().findFirst();
		if (first.isPresent()) {
			getSelectionModel().clearSelection();
			getSelectionModel().select(first.get());
			scrollTo(first.get());
		}
	}






	public void orderByYearAndSelect (int year) {
		/* sort by year ASC, n_cr desc */
		columns[CRType_ColumnView.CRColumn.RPY.ordinal()].setSortType(TableColumn.SortType.ASCENDING);
		columns[CRType_ColumnView.CRColumn.N_CR.ordinal()].setSortType(TableColumn.SortType.DESCENDING);
		getSortOrder().setAll(
			columns[CRType_ColumnView.CRColumn.RPY.ordinal()],
			columns[CRType_ColumnView.CRColumn.N_CR.ordinal()]);
		
		sort();

		/* go to first row of specified year */
		int rowIndex = getFirstRowByYear (year);
		if (rowIndex >= 0) {
			scrollTo(rowIndex);
			getSelectionModel().clearSelection();
			getSelectionModel().select(rowIndex);
			requestFocus();
		}
	}

}
