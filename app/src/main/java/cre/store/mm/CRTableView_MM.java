package cre.store.mm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cre.ui.CRTableView;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;

public class CRTableView_MM extends CRTableView<CRType_MM> {


	private CRTable_MM crTable;

	public CRTableView_MM(CRTable_MM crTable) {
		
		super();
		this.crTable = crTable;
	}

	@Override
	public void updateTableViewData () {

		// save sort order ...
		List<TableColumn<CRType_MM, ?>> oldSort = new ArrayList<TableColumn<CRType_MM, ?>>(getSortOrder());

		// ... clear all rows ...
		getItems().clear();
		getSortOrder().clear();
			
		// ... update rows ...
		// I don't know why but we need to create an new instance here
		setItems(FXCollections.observableArrayList(crTable.getCR().filter(cr -> cr.getVI()).collect(Collectors.toList())));

		// ... reset old sort order
		getSortOrder().addAll(oldSort);
	}


	@Override
    public int getFirstRowByYear(int year) {
        
		Optional<CRType_MM> first = getItems().stream().filter(cr -> (cr.getRPY()!=null) && (cr.getRPY().intValue() == year)).findFirst();
		return first.isPresent() ? getItems().indexOf(first.get()) : -1;
    }



}
