package cre.store.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cre.data.type.abs.CRType;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class OberservableCRList_DB<E> implements ObservableList<CRType<?>> {

    private Connection dbCon;
    private DB_Store dbStore;
    private Statistics_DB statistics;
    
    private static final int CACHE_SIZE = 1000;
    private int cache_Start;
    private CRType_DB[] cache;
    private int size;

    private String sortOrder = "CR_ID";
    

    public OberservableCRList_DB (Connection dbCon, DB_Store dbStore, Statistics_DB statistics) {
		this.dbCon = dbCon;
        this.dbStore = dbStore;
        this.statistics = statistics;
        this.invalidateCache();
	}


    public void setSortOrder (String sortOrder) {
        // default sort order is by Id
        if ((sortOrder==null) || (sortOrder.trim().length()==0)) {
            sortOrder = "CR_ID";
        }
        this.sortOrder = sortOrder;
        invalidateCache();
    }

    public void invalidateCache() {
        this.cache_Start = -1;
        this.cache = new CRType_DB[0];
        this.size = -1;

        try {
            int i = dbCon.createStatement().executeUpdate(String.format("""
                UPDATE CR SET CR_SORT_ORDER = NULL;
                MERGE INTO CR (CR_ID, CR_SORT_ORDER)
                SELECT CR_ID, (ROW_NUMBER() OVER (ORDER BY %s))-1
                FROM CR
                WHERE CR_VI;
                """, this.sortOrder));
            dbCon.commit();

            System.out.println(String.format("setSortOrder update: %d", i));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateCache(int center) {
        this.cache_Start = Math.max(0, center - CACHE_SIZE/2);
        this.cache = this.dbStore
            .selectCR(String.format("WHERE CR_SORT_ORDER >= %d AND CR_SORT_ORDER < %d ORDER BY CR_SORT_ORDER", this.cache_Start, this.cache_Start+CACHE_SIZE))
            // .peek(x -> System.out.println(x.getID()))
            .toArray(CRType_DB[]::new);
        System.out.println(String.format("Update Cache (start=%d, length=%d)", this.cache_Start, this.cache.length));

    }



    @Override
    public CRType<?> get(int index) {
        
        // System.out.println(String.format("get(%d)", index));

        if ((this.cache_Start>=0) && (index>=this.cache_Start) && (index<this.cache_Start+CACHE_SIZE)) {
            int cachePos = index-this.cache_Start;
            if (cachePos>=this.cache.length) {
                System.out.println("Wäre ein Index out of Bounds");
                return null;
            }
            return this.cache[index-this.cache_Start];
        } else {
            updateCache(index);
            return get(index);
        }
    }



    @Override
    public int size() {
        if (this.size == -1) {
            this.size = statistics.getNumberOfCRsByVisibility(true);
            System.out.println(String.format("size()=%d", this.size));

        }
        // System.out.println(String.format("size()=%d", this.size));
        return this.size;
    }




    @Override
    public void addListener(ListChangeListener<? super CRType<?>> listener) {
        // TODO Funktion wird aufgerufen --> HIER MUSS WAS REIN???
        System.out.println("addListener");
    }

    @Override
    public void removeListener(ListChangeListener<? super CRType<?>> listener) {
        // TODO Funktion wird aufgerufen --> HIER MUSS WAS REIN???
    }

    @Override
    public void clear() {
        // TODO Wird aufgerufen ... was machen wir hier???
    }


    @Override
    public int indexOf(Object arg0) {
        return -1;  // we don't need this .. if
    }


    @Override
    public boolean add(CRType<?> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public void add(int arg0, CRType<?> arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public boolean addAll(Collection<? extends CRType<?>> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAll'");
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends CRType<?>> arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAll'");
    }



    @Override
    public boolean contains(Object arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contains'");
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsAll'");
    }

 



    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    @Override
    public Iterator<CRType<?>> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

    @Override
    public int lastIndexOf(Object arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lastIndexOf'");
    }

    @Override
    public ListIterator<CRType<?>> listIterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listIterator'");
    }

    @Override
    public ListIterator<CRType<?>> listIterator(int arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listIterator'");
    }

    @Override
    public boolean remove(Object arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public CRType<?> remove(int arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
    }

    @Override
    public CRType<?> set(int arg0, CRType<?> arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'set'");
    }



    @Override
    public List<CRType<?>> subList(int arg0, int arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'subList'");
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toArray'");
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toArray'");
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
    public boolean addAll(CRType<?>... elements) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAll'");
    }

    @Override
    public boolean setAll(CRType<?>... elements) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setAll'");
    }

    @Override
    public boolean setAll(Collection<? extends CRType<?>> col) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setAll'");
    }

    @Override
    public boolean removeAll(CRType<?>... elements) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
    }

    @Override
    public boolean retainAll(CRType<?>... elements) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
    }

    @Override
    public void remove(int from, int to) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }
    
}
