package cre.store.mm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

import cre.ui.CRTableView;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

public class CRTableView_MM extends CRTableView<CRType_MM> {

	private CRTable_MM crTable;

	class TableItems implements ObservableList<CRType_MM> {

		@Override
		public CRType_MM get(int index) {
			// TODO Auto-generated method stub
			System.out.println("get " + index);
			return crTable.getCR().skip(index).findFirst().get();
			// throw new UnsupportedOperationException("Unimplemented method 'get'");
		}


		@Override
		public void clear() {
			// TODO Auto-generated method stub
			System.out.println("Unimplemented method 'clear'");
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return crTable.getStatistics().getNumberOfCRsByVisibility(true);
			// throw new UnsupportedOperationException("Unimplemented method 'size'");
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
		}

		@Override
		public boolean contains(Object o) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'contains'");
		}

		@Override
		public Iterator<CRType_MM> iterator() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'iterator'");
		}

		@Override
		public Object[] toArray() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'toArray'");
		}

		@Override
		public <T> T[] toArray(T[] a) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'toArray'");
		}

		@Override
		public boolean add(CRType_MM e) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public boolean remove(Object o) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'remove'");
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'containsAll'");
		}

		@Override
		public boolean addAll(Collection<? extends CRType_MM> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addAll'");
		}

		@Override
		public boolean addAll(int index, Collection<? extends CRType_MM> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addAll'");
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
		}



		@Override
		public CRType_MM set(int index, CRType_MM element) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'set'");
		}

		@Override
		public void add(int index, CRType_MM element) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public CRType_MM remove(int index) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'remove'");
		}

		@Override
		public int indexOf(Object o) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'indexOf'");
		}

		@Override
		public int lastIndexOf(Object o) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'lastIndexOf'");
		}

		@Override
		public ListIterator<CRType_MM> listIterator() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'listIterator'");
		}

		@Override
		public ListIterator<CRType_MM> listIterator(int index) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'listIterator'");
		}

		@Override
		public List<CRType_MM> subList(int fromIndex, int toIndex) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'subList'");
		}

		@Override
		public void addListener(InvalidationListener listener) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addListener'");
		}

		@Override
		public void removeListener(InvalidationListener listener) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'removeListener'");
		}

		@Override
		public void addListener(ListChangeListener<? super CRType_MM> listener) {
			// TODO Auto-generated method stub
			System.out.println("Unimplemented method 'addListener'");
		}

		@Override
		public void removeListener(ListChangeListener<? super CRType_MM> listener) {
			// TODO Auto-generated method stub
			System.out.println("Unimplemented method 'removeListener'");
		}

		@Override
		public boolean addAll(CRType_MM... elements) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addAll'");
		}

		@Override
		public boolean setAll(CRType_MM... elements) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setAll'");
		}

		@Override
		public boolean setAll(Collection<? extends CRType_MM> col) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setAll'");
		}

		@Override
		public boolean removeAll(CRType_MM... elements) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
		}

		@Override
		public boolean retainAll(CRType_MM... elements) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
		}

		@Override
		public void remove(int from, int to) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'remove'");
		}

	}



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

		// TableItems items = new TableItems();
		// setItems(items);

		// ... reset old sort order
		getSortOrder().addAll(oldSort);
	}


	@Override
    public int getFirstRowByYear(int year) {
        
		Optional<CRType_MM> first = getItems().stream().filter(cr -> (cr.getRPY()!=null) && (cr.getRPY().intValue() == year)).findFirst();
		return first.isPresent() ? getItems().indexOf(first.get()) : -1;
    }



}
