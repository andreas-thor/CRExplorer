package cre.data.type.abs;

import cre.store.mm.CRType_MM;
import cre.store.mm.PubType_MM;

import java.util.List;

public interface Loader {

    public abstract void onBeforeLoad();
	public abstract void onAfterLoad();

	public abstract void onNewCR(CRType_MM cr);
	public abstract void onNewPub(PubType_MM pub, List<Integer> crIds);
	public abstract void onNewMatchPair(int crId1, int crId2, double sim, boolean isManual);
    
}
