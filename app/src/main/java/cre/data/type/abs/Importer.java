package cre.data.type.abs;

import cre.store.mm.PubType_MM;

public interface Importer {
    
    /**
	 * Methods for importing data files, i.e., publication lists
	 */

	public abstract void onBeforeImport ();
	public abstract void addPub (PubType_MM pub);		
	public abstract void onAfterImport ();

}
