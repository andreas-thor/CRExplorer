package cre.store.db;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cre.data.type.extern.CRType_ColumnView;
import cre.ui.CRTableView;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableColumn;

public class CRTableView_DB extends CRTableView<CRType_DB> {

	private CRTable_DB crTable;

	public CRTableView_DB(CRTable_DB crTable) {
		
		super();
		this.crTable = crTable;

		addEventHandler(SortEvent.ANY, event -> {

			System.out.println("HEY, Sorted???");
			System.out.println (((CRTableView_DB) event.getSource()).getSortOrder().size());

			this.crTable.getObservableCRList_DB().setSortOrder (
				((CRTableView_DB) event.getSource()).getSortOrder().stream()
					.map(o -> String.format("%s %s", ((CRType_ColumnView.CRColumn) o.getUserData()).sqlName,  o.getSortType() == TableColumn.SortType.DESCENDING ? " DESC" : ""))
					.collect( Collectors.joining(", ") )
			);

			refresh();
			event.consume();

		});
	}		


	@Override
	public void updateTableViewData () {
		
		// save sort order ...
		List<TableColumn<CRType_DB, ?>> oldSort = new ArrayList<TableColumn<CRType_DB, ?>>(getSortOrder());

		// ... update rows ...
		getItems().clear();
		getSortOrder().clear();
			
		
		// I don't know why but we need to create an new instance here
		setItems(crTable.createNewObservableCRList_DB());
		// crTable.getObservableCRList_DB().invalidateCache();

		// ... reset old sort order
		getSortOrder().addAll(oldSort);
	}



	@Override
    public int getFirstRowByYear(int year) {
        CRType_DB[] cr = this.crTable.getDBStore()
            .selectCR(String.format("WHERE CR_RPY = %d ORDER BY CR_SORT_ORDER LIMIT 1", year))
            .toArray(CRType_DB[]::new);
        return cr.length == 0 ? -1 : cr[0].getSortOrder();
    }



}
