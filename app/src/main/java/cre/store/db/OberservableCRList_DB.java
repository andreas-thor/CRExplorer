package cre.store.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cre.CRELogger;
import javafx.collections.ObservableList;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;

public class OberservableCRList_DB implements ObservableList<CRType_DB> {

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
            // H2
            // int i = dbCon.createStatement().executeUpdate(String.format("""
            //     UPDATE CR SET CR_SORT_ORDER = NULL;
            //     MERGE INTO CR (CR_ID, CR_SORT_ORDER)
            //     SELECT CR_ID, (ROW_NUMBER() OVER (ORDER BY %s))-1
            //     FROM CR
            //     WHERE CR_VI;
            //     """, this.sortOrder));
            
            // PostgreSQL
            // TODO: Auslagern in SQL
            Statement stmt = dbCon.createStatement();
            int i = stmt.executeUpdate(String.format("""
                WITH CRWITHROW AS (
                    SELECT CR_ID, (ROW_NUMBER() OVER (ORDER BY %s))-1 AS R
                    FROM CR
                    WHERE CR_VI
                )
                UPDATE CR 
                SET CR_SORT_ORDER = CRWITHROW.R
                FROM CRWITHROW
                WHERE CR.CR_ID = CRWITHROW.CR_ID
                """, this.sortOrder));

            dbCon.commit();
            stmt.close();

            CRELogger.get().logInfo(String.format("setSortOrder update: %d", i));
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
            CRELogger.get().logInfo(String.format("Update Cache (start=%d, length=%d)", this.cache_Start, this.cache.length));

    }



    @Override
    public CRType_DB get(int index) {
        
        // System.out.println(String.format("get(%d)", index));

        if ((this.cache_Start>=0) && (index>=this.cache_Start) && (index<this.cache_Start+CACHE_SIZE)) {
            int cachePos = index-this.cache_Start;
            if (cachePos>=this.cache.length) {
                CRELogger.get().logInfo("Wäre ein Index out of Bounds");
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
            CRELogger.get().logInfo(String.format("size()=%d", this.size));

        }
        // System.out.println(String.format("size()=%d", this.size));
        return this.size;
    }








    @Override
    public void addListener(ListChangeListener<? super CRType_DB> listener) {
        // TODO Funktion wird aufgerufen --> HIER MUSS WAS REIN???
        System.out.println("addListener");
    }

    @Override
    public void removeListener(ListChangeListener<? super CRType_DB> listener) {
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
    public boolean add(CRType_DB arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public void add(int arg0, CRType_DB arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public boolean addAll(Collection<? extends CRType_DB> arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'addAll'");
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends CRType_DB> arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'addAll'");
    }

    @Override
    public boolean contains(Object arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'contains'");
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'containsAll'");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    @Override
    public Iterator<CRType_DB> iterator() {
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

    @Override
    public int lastIndexOf(Object arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'lastIndexOf'");
    }

    @Override
    public ListIterator<CRType_DB> listIterator() {
        throw new UnsupportedOperationException("Unimplemented method 'listIterator'");
    }

    @Override
    public ListIterator<CRType_DB> listIterator(int arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'listIterator'");
    }

    @Override
    public boolean remove(Object arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public CRType_DB remove(int arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
    }

    @Override
    public CRType_DB set(int arg0, CRType_DB arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'set'");
    }

    @Override
    public List<CRType_DB> subList(int arg0, int arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'subList'");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Unimplemented method 'toArray'");
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'toArray'");
    }

    @Override
    public void addListener(InvalidationListener listener) {
        throw new UnsupportedOperationException("Unimplemented method 'addListener'");
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        throw new UnsupportedOperationException("Unimplemented method 'removeListener'");
    }

    @Override
    public boolean addAll(CRType_DB... elements) {
        throw new UnsupportedOperationException("Unimplemented method 'addAll'");
    }

    @Override
    public boolean setAll(CRType_DB... elements) {
        throw new UnsupportedOperationException("Unimplemented method 'setAll'");
    }

    @Override
    public boolean setAll(Collection<? extends CRType_DB> col) {
        throw new UnsupportedOperationException("Unimplemented method 'setAll'");
    }

    @Override
    public boolean removeAll(CRType_DB... elements) {
        throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
    }

    @Override
    public boolean retainAll(CRType_DB... elements) {
        throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
    }

    @Override
    public void remove(int from, int to) {
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }



    
}
