package cre.ui.dialog;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import cre.CitedReferencesExplorerFX;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class About extends Alert {

	
	public About() {
		
		super(AlertType.INFORMATION);
		
		setTitle("Info");
		setHeaderText("About " + CitedReferencesExplorerFX.title);
		setResizable(true);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 20, 20, 20));

		grid.add(new Label("Software Development:"), 0, 0);
		grid.add(new Label("Andreas Thor <andreas.thor@htwk-leipzig.de>"), 1, 0);
		grid.add(new Label("Content Development:"), 0, 1);
		grid.add(new Label("Lutz Bornmann and Werner Marx"), 1, 1);
		grid.add(new Label("with further support of:"), 0, 2);
		grid.add(new Label("Robin Haunschild, Loet Leydesdorff, and Rüdiger Mutz "), 1, 2);
		
		Hyperlink hp = new Hyperlink("Project website: crexplorer.net");
		hp.setOnAction(e -> {
			
		// TODO set hyperlink
			// 	HostServicesFactory.getInstance(CitedReferencesExplorer.app).showDocument(CitedReferencesExplorer.url);
		});
		grid.add(hp, 0, 3);

	
		
		// FIXME: Global seeting
		Path cachePath = Paths.get(System.getProperty("java.io.tmpdir")).resolve("CRExplorerDownload");
		try { Files.createDirectory(cachePath); } catch (Exception e2) { }
		
		grid.add(new Label("Temp Directory:"), 0, 4);
		
		TextField tf = new TextField(cachePath.toString());
		tf.setMaxWidth(250);
		tf.setEditable(false);
		grid.add(tf,  1,  4);
		
		
		
		getDialogPane().setContent(grid);
			
	}
	

	
}
