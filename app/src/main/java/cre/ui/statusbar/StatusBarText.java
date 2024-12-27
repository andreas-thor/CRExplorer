package cre.ui.statusbar;

import java.util.Date;


public class StatusBarText implements StatusBarUI {

	private boolean showProgress = true;
	private String lastLabel = "";
	
	public void setShowProgress (boolean showProgress) {
		this.showProgress = showProgress;
		if (this.showProgress) System.out.print("\n");
	}
	
	public void printInfo (String info) {
		System.out.print("\n" + info);
	}
	
	
	public void print (String label, long percent, Date d) {

		if ((percent>0) && (!this.showProgress)) return;
		
		// we ignore the parameter Date d but show the current date (time) always
		StringBuffer out = new StringBuffer(String.format("%s: %s", (new Date()).toString(), label));
		if (percent > 0) {
			 out.append(String.format(" [%d%%]", percent));
		} 
		
		if (!lastLabel.equals(label)) System.out.print("\r\n");
		
		System.out.print(out.toString() + "\r");
		lastLabel = label;
	}

	
	

	
}
