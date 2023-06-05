package cre.store.mm;

import java.util.Optional;

import cre.ui.CRTableView;

public class CRTableView_MM extends CRTableView<CRType_MM> {


	@Override
    public int getFirstRowByYear(int year) {
        
		Optional<CRType_MM> first = getItems().stream().filter(cr -> (cr.getRPY()!=null) && (cr.getRPY().intValue() == year)).findFirst();
		return first.isPresent() ? getItems().indexOf(first.get()) : -1;
    }



}
