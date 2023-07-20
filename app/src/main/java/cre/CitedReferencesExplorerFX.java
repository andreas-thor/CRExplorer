package cre;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import cre.store.db.CRTable_DB;


public class CitedReferencesExplorerFX extends Application {

	public static Stage stage;
	public static Application app;
	
	public static final String manual_url = "https://andreas-thor.github.io/CRExplorer/manual.pdf";
	public static final String url = "http://www.crexplorer.net";
	
	public static String title = "CRExplorer";
	public static String loadOnOpen = null;

	public static void main(String[] args) {

		if ((args.length > 1) && (args[0].equals("-open"))) {
			loadOnOpen = args[1];
		}

		setDatabaseParams(args);
		if (CRTable.type == TABLE_IMPL_TYPES.DB) {
			title += String.format(" (DB=%s)", CRTable_DB.url); 
		}

		launch(args);
	}


	public static void setDatabaseParams (String[] args) {
		for (String arg: args) {
			if (arg.toLowerCase().startsWith("-db")) {
				CRTable.type = TABLE_IMPL_TYPES.DB;
				String[] split = arg.split("=");
				if (split.length==2) CRTable_DB.url=split[1];
			}
			if (arg.equalsIgnoreCase("-nocreate")) {
				CRTable_DB.createSchemaOnStartup = false;
			}
		}
	}

	public static void updateTitle () {
		// we include the version info into the Window title 
		// version.txt is created / updated during the build process by gradle
		try {
			CitedReferencesExplorerFX.title = String.format("%s (Build Version %s)", 
				CitedReferencesExplorerFX.title, 	
				new BufferedReader(new InputStreamReader(CitedReferencesExplorerFX.class.getResourceAsStream("/version.txt"))).lines().collect(Collectors.joining(""))
			);
		} catch (Exception e) {
			System.out.println("aaaaa");
		}
	}

	
	public void start(Stage stage) throws Exception {

		updateTitle();

		Locale.setDefault(Locale.US);
		Platform.setImplicitExit(true);

		setUserAgentStylesheet(STYLESHEET_MODENA); 

		CitedReferencesExplorerFX.stage = stage;
		CitedReferencesExplorerFX.app = this;
		
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/CRE32.png")));
		stage.setTitle(CitedReferencesExplorerFX.title);

		Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
		Scene scene = new Scene(root); 
		stage.setScene(scene);
		stage.show();
	}

}
