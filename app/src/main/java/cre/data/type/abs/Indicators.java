package cre.data.type.abs;

public interface Indicators {
    
	public enum CRTypes { SB("Sleeping beauty"), CP("Constant performer"), HP ("Hot paper"), LC("Life cycle");
	    public final String label;
	    private CRTypes(String label) {
	        this.label = label;
	    }
	}
	
	public enum ZValueSymbol { PLUS('+'), ZERO('0'), MINUS ('-');
	    public final char label;
	    private ZValueSymbol(char label) {
	        this.label = label;
	    }
	}

	public abstract void updateIndicators ();

}
