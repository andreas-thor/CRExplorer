package cre.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cre.CRELogger;
import cre.CitedReferencesExplorerFX;
import cre.Exceptions.AbortedException;
import cre.Exceptions.BadResponseCodeException;
import cre.Exceptions.FileTooLargeException;
import cre.Exceptions.UnsupportedFileFormatException;
import cre.data.CRStatsInfo;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRType;
import cre.data.type.abs.Clustering;
import cre.data.type.abs.PubType;
import cre.data.type.abs.Statistics.IntRange;
import cre.format.exporter.ExportFormat;
import cre.format.importer.Crossref;
import cre.format.importer.ImportFormat;
import cre.ui.UISettings.RangeType;
import cre.ui.chart.CRChart;
import cre.ui.chart.CRChart_HighCharts;
import cre.ui.chart.CRChart_JFreeChart;
import cre.ui.dialog.About;
import cre.ui.dialog.CRInfo;
import cre.ui.dialog.CRPubInfo;
import cre.ui.dialog.ConfirmAlert;
import cre.ui.dialog.DownloadCrossref;
import cre.ui.dialog.DownloadCrossrefData;
import cre.ui.dialog.ExceptionStacktrace;
import cre.ui.dialog.ImportStats;
import cre.ui.dialog.Info;
import cre.ui.dialog.Range;
import cre.ui.dialog.Search;
import cre.ui.dialog.Settings;
import cre.ui.dialog.TextInput;
import cre.ui.dialog.Threshold;
import cre.ui.dialog.Wait;
import cre.ui.statusbar.StatusBar;
import cre.ui.statusbar.StatusBarFX;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.WindowEvent;

public class MainController {

	CRTable<? extends CRType<?>, ? extends PubType<?>> crTable;
	CRChart crChart[] = new CRChart[2];

	@FXML
	Label lab;
	@FXML
	GridPane mainPane;
	@FXML
	ScrollPane scrollTab;

	@FXML
	GridPane chartPane;
	@FXML
	GridPane tablePane;
	@FXML
	GridPane statPane;

	private CRTableView<? extends CRType<?>> tableView;
	private MatchPanel matchView;
	private Thread t;
	private File creFile;

	@FXML
	MenuItem noOfVisibleCRs;
	@FXML
	CheckMenuItem showWOYear;

	@FXML
	public void initialize() throws OutOfMemoryError, Exception {

		CitedReferencesExplorerFX.stage.setWidth(UISettings.get().getWindowWidth());
		CitedReferencesExplorerFX.stage.setHeight(UISettings.get().getWindowHeight());
		CitedReferencesExplorerFX.stage.setX(UISettings.get().getWindowX());
		CitedReferencesExplorerFX.stage.setY(UISettings.get().getWindowY());
		
		creFile = null;
		crTable = CRTable.get();
		crTable.setNpctRange(UISettings.get().getNPCTRange());
		
		crTable.getChartData().setMedianRange(UISettings.get().getMedianRange());
		
		StatusBarFX stat = new StatusBarFX();
		statPane.add(stat, 0, 0);

		StatusBar.get().setUI(stat);
		StatusBar.get().setOnUpdateInfo(x -> {
			noOfVisibleCRs.setDisable(CRTable.get().getStatistics().getNumberOfCRs() == CRTable.get().getStatistics().getNumberOfCRsByVisibility(true));
			noOfVisibleCRs.setText(String.format("Show all Cited References (currently %d of %d)", CRTable.get().getStatistics().getNumberOfCRsByVisibility(true), CRTable.get().getStatistics().getNumberOfCRs()));
		});

		tableView = crTable.getTableView(); // new CRTableView<CRType<?>>();
		tablePane.add(tableView, 0, 1);
		tableView.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DELETE) {
				OnMenuDataRemoveSelected();
			}
			if (e.getCode() == KeyCode.SPACE) {
				List<Integer> sel = getSelectedCRs();
				if (sel.size() == 1) {
					new CRInfo(crTable.getCRById(sel.get(0))).showAndWait();
				}
			}
		});

		matchView = new MatchPanel() {

			@Override
			public void onUpdateClustering(double threshold, boolean useClustering, boolean useVol, boolean usePag, boolean useDOI, boolean nullEqualsNull) {
				if (t != null)
					t.interrupt();
				t = new Thread(() -> {
					crTable.getClustering().updateClustering(Clustering.ClusteringType.REFRESH, null, threshold, useVol, usePag, useDOI, nullEqualsNull);
					tableView.updateTableViewData();
					refreshTableValues();
				});
				t.start();
			}

			@Override
			public void onMatchManual(Clustering.ManualMatchType type, double threshold, boolean useVol, boolean usePag, boolean useDOI, boolean nullEqualsNull) {

				List<Integer> toMatch = getSelectedCRs();
				if ((toMatch.size() == 0) || ((toMatch.size() == 1) && (type != Clustering.ManualMatchType.EXTRACT))) {
					new ConfirmAlert("Error during clustering!", true, new String[] { "Too few Cited References selected!" }).showAndWait();
				} else {
					if ((toMatch.size() > 5) && (type != Clustering.ManualMatchType.EXTRACT)) {
						new ConfirmAlert("Error during clustering!", true, new String[] { "Too many Cited References selected (at most 5)!" }).showAndWait();
					} else {
						crTable.getClustering().addManuMatching(toMatch, type, threshold, useVol, usePag, useDOI, nullEqualsNull);
						
						tableView.updateTableViewData();
						refreshTableValues();
					}
				}
			}

			@Override
			public void onMatchUnDo(double threshold, boolean useVol, boolean usePag, boolean useDOI, boolean nullEqualsNull) {
				crTable.getClustering().undoManuMatching(threshold, useVol, usePag, useDOI, nullEqualsNull);
				tableView.updateTableViewData();
				refreshTableValues();
			}

		};
		matchView.setVisible(false);
		tablePane.add(matchView, 0, 0);

		crChart = new CRChart[] { new CRChart_JFreeChart() {
			@Override
			protected void onSelectYear(int year) {
				tableView.orderByYearAndSelect(year);
			}

			@Override
			protected void onYearRangeFilter(double min, double max) {
				filterByRPY(new IntRange ((int) Math.ceil(min), (int) Math.floor(max)), true);
			}
		}, new CRChart_HighCharts() {
			@Override
			protected void onSelectYear(int year) {
				tableView.orderByYearAndSelect(year);
			}

			@Override
			protected void onYearRangeFilter(double min, double max) {
				filterByRPY (new IntRange ((int) Math.ceil(min), (int) Math.floor(max)), true);
			}
		} };
		for (int i = 0; i < crChart.length; i++) {
			chartPane.add(crChart[i].getNode(), 0, 0);
			crChart[i].setVisible(UISettings.get().getChartEngine() == i);
		}

		// save user settings when exit
		CitedReferencesExplorerFX.stage.setOnCloseRequest(event -> {

			event.consume();

			if (CRTable.get().getStatistics().getNumberOfCRs() == 0) {

				UISettings.get().saveUserPrefs(CitedReferencesExplorerFX.stage.getWidth(), CitedReferencesExplorerFX.stage.getHeight(), CitedReferencesExplorerFX.stage.getX(), CitedReferencesExplorerFX.stage.getY());
				CitedReferencesExplorerFX.stage.close();
				return;
			}

			Alert exit = new Alert(AlertType.CONFIRMATION);
			exit.setTitle("Warning");
			exit.setHeaderText("Save before exit?");
			exit.getDialogPane().getButtonTypes().clear();
			exit.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

			exit.showAndWait().ifPresent(button -> {

				if (button == ButtonType.CANCEL)
					return;

				if (button == ButtonType.YES) {
					try {
						if (!exportFile(ExportFormat.CRE, false))
							return;
					} catch (Exception e) {
						Platform.runLater(() -> {
							new ExceptionStacktrace("Error during file export!", e).showAndWait();
						});
						return;
					}
				}

				UISettings.get().saveUserPrefs(CitedReferencesExplorerFX.stage.getWidth(), CitedReferencesExplorerFX.stage.getHeight(), CitedReferencesExplorerFX.stage.getX(), CitedReferencesExplorerFX.stage.getY());
				CitedReferencesExplorerFX.stage.close();
			});
		});

		if (CitedReferencesExplorerFX.loadOnOpen != null) {
			importFiles(ImportFormat.CRE, Arrays.asList(new File (CitedReferencesExplorerFX.loadOnOpen)));
			// openFile(new File (CitedReferencesExplorerFX.loadOnOpen), CREReader::load);
		}

	}

	private void filterByRPY(IntRange range, boolean fromChart) {
		UISettings.get().setRange(RangeType.FilterByRPYRange, range);
		crTable.filterByYear(range);
		updateTableCRList(fromChart);
	}

	private void updateTableCRList() {
		updateTableCRList(false);
	}

	private void updateTableCRList(boolean triggeredByChartZoom) {
		Platform.runLater(() -> {




			// // save sort order ...
			// List<TableColumn<CRType<?>, ?>> oldSort = new ArrayList<TableColumn<CRType<?>, ?>>();
			// tableView.getSortOrder().forEach(it -> oldSort.add((TableColumn<CRType<?>, ?>) it));

			// // ... update rows ...
			// tableView.getItems().clear();
			// tableView.getSortOrder().clear();
			// // FIXME: filter (cr -> false). um zu verhindern, dass was angezeigt wird
			
			
		
			// ObservableList<? extends CRType<?>> a = crTable.getObservableCRList();
			tableView.updateTableViewData();

			// // ... reset old sort order
			// for (TableColumn<CRType<?>, ?> x : oldSort) {
			// 	tableView.getSortOrder().add(x);
			// }

			// set Domain Range for charts
			if (!triggeredByChartZoom) {
				Stream.of(crChart).forEach(it -> {
					if (it.isVisible()) {
						it.updateData(CRTable.get().getChartData());
						it.setDomainRange(CRTable.get().getStatistics().getMaxRangeRPY(true));
					}
				});
			}

			refreshTableValues();

		});
	}

	private void refreshTableValues() {
		Platform.runLater(() -> {
			CRELogger.get().logInfo("SortOrderSize = " + tableView.getSortOrder().size());

			if (tableView.getSortOrder().size() > 0) {
				tableView.sort();
			}
			tableView.getColumns().get(0).setVisible(false);
			tableView.getColumns().get(0).setVisible(true);
			

			StatusBar.get().updateInfo();
		});
	}

	/*
	 * http://stackoverflow.com/questions/36353518/javafx-table-view-multiple-
	 * selection-sometimes-skips-one-of-the-items/38207246#38207246 Apparently it's
	 * a bug, already fixed for version 9 (and also 8u112, if I understand
	 * correctly): https://bugs.openjdk.java.net/browse/JDK-8144501 A workaround for
	 * now is to use getSelectedIndices(), then get the items corresponding to these
	 * instances from table.getItems() return
	 * tableView.getSelectionModel().getSelectedIndices().stream().map(idx ->
	 * tableView.getItems().get(idx)).collect(Collectors.toList()); ==> GEHT AUCH
	 * NICHT
	 */
	private List<Integer> getSelectedCRs() {
		return tableView.getSelectionModel().getSelectedItems().stream()
			.filter(cr -> cr != null)
			.map(cr -> cr.getID())
			.collect(Collectors.toList());
	}


	private boolean analyzeFiles(ImportFormat source, List<File> files) {

		final AtomicBoolean result = new AtomicBoolean();

		Wait wait = new Wait();
		Service<CRStatsInfo> serv = new Service<CRStatsInfo>() {

			@Override
			protected Task<CRStatsInfo> createTask() {
				return new Task<CRStatsInfo>() {

					@Override
					protected CRStatsInfo call() throws Exception {
						return source.analyze(files);
					}
				};
			}
		};

		
		
		serv.setOnSucceeded((WorkerStateEvent t) -> {
			updateTableCRList();
			result.set(new ImportStats(serv.getValue()).showAndWait().isPresent());
			wait.close();
		});

		serv.setOnFailed((WorkerStateEvent t) -> {
			Throwable e = t.getSource().getException();
			if (e instanceof UnsupportedFileFormatException) {
				new ConfirmAlert("Error during file analysis!", true, new String[] { "Unsupported File Format." }).showAndWait();
			} else if (e instanceof AbortedException) {
				new ConfirmAlert("Error during file analysis!", true, new String[] { "File analysis aborted." }).showAndWait();
			} else if (e instanceof OutOfMemoryError) {
				crTable.init();
				new ConfirmAlert("Error during file import!", true, new String[] { "Out of Memory Error." }).showAndWait();
			} else {
				new ExceptionStacktrace("Error during file analysis!", e).showAndWait();
			}
			wait.close();
		});

		serv.start();

		wait.showAndWait().ifPresent(cancel -> {
			crTable.setAborted(cancel);
		});

		return result.get();

	}


	private void updateTitle() {
		CitedReferencesExplorerFX.stage.setTitle(CitedReferencesExplorerFX.title + ((creFile == null) ? "" : " - " + creFile.getAbsolutePath()));		
	}

	public void importFiles(ImportFormat source, List<File> files) {

		Wait wait = new Wait();
		Service<Void> serv = new Service<Void>() {

			@Override
			protected Task<Void> createTask() {
				return new Task<Void>() {

					@Override
					protected Void call() throws Exception {

						tableView.getItems().clear(); // free space (references to CR instances)
						creFile = source.isCREFormat() ? files.get(0) : null;
						source.load(
							files, 
							UISettings.get().getRange(RangeType.ImportRPYRange),
							UISettings.get().getImportCRsWithoutYear(),
							UISettings.get().getRange(RangeType.ImportPYRange), 
							UISettings.get().getImportPubsWithoutYear(),
							UISettings.get().getMaxCR(),
							UISettings.get().getSampling()
						);
						
						// show match panel if applicable
						matchView.setVisible((crTable.getClustering().getNumberOfMatches(true) + crTable.getClustering().getNumberOfMatches(false)) > 0);
						matchView.updateClustering();

						return null;
					}
				};
			}
		};

		serv.setOnSucceeded((WorkerStateEvent t) -> {
			OnMenuViewInfo();
			updateTableCRList();
			wait.close();
			updateTitle();
		});

		serv.setOnFailed((WorkerStateEvent t) -> {
			Throwable e = t.getSource().getException();
			if (e instanceof FileTooLargeException) {
				new ConfirmAlert("Error during file import!", true,
						new String[] {
								String.format("You try to import too many cited references. Import was aborted after loading %d Cited References and %d Citing Publications. You can change the maximum number in the File > Settings > Import menu. ",
										((FileTooLargeException) e).numberOfCRs, ((FileTooLargeException) e).numberOfPubs) }).showAndWait();
			} else if (e instanceof UnsupportedFileFormatException) {
				new ConfirmAlert("Error during file import!", true, new String[] { "Unsupported File Format." }).showAndWait();
			} else if (e instanceof AbortedException) {
				new ConfirmAlert("Error during file import!", true, new String[] { "File Import aborted." }).showAndWait();
			} else if (e instanceof OutOfMemoryError) {
				crTable.init();
				new ConfirmAlert("Error during file import!", true, new String[] { "Out of Memory Error." }).showAndWait();
			} else {
				new ExceptionStacktrace("Error during file import!", e).showAndWait();
			}
			wait.close();
		});

		serv.start();

		wait.showAndWait().ifPresent(cancel -> {
			crTable.setAborted(cancel);
		});
	}

	private void importFiles(ImportFormat source) throws IOException {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(String.format("Import %s", source.getLabel()));
		fileChooser.setInitialDirectory(UISettings.get().getLastFileDir());
		fileChooser.getExtensionFilters().add(new ExtensionFilter(source.getLabel(), Arrays.asList(new String[] { "*." + source.getFileExtension()})));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", Arrays.asList(new String[] { "*.*" })));

		List<File> files = source.isMultipleFiles() ? fileChooser.showOpenMultipleDialog(CitedReferencesExplorerFX.stage) : Arrays.asList(fileChooser.showOpenDialog(CitedReferencesExplorerFX.stage));
		if ((files == null) || (files.size()==0)) return;

		UISettings.get().setLastFileDir(files.get(0).getParentFile()); // save last directory to be uses as initial directory

		if (source.isDataLoader()) {
			importFiles(source, files);
		} else {
			if (analyzeFiles(source, files)) {
				importFiles(source, files);
			} else {
				StatusBar.get().setValue("Import aborted by user");
			}
		}
	}	
	
	private void downloadCrossref() {

		Optional<DownloadCrossrefData> dialogResult = new DownloadCrossref().showAndWait();

		if (!dialogResult.isPresent()) {
			return;
		}
		
		Wait wait = new Wait();
		Service<List<File>> serv = new Service<List<File>>() {
			@Override
			protected Task<List<File>> createTask() {
				return new Task<List<File>>() {
					@Override
					protected List<File> call() throws Exception {
						CRTable.get().setAborted(false);
						return Crossref.download(dialogResult.get().getTitle(), dialogResult.get().getDOI(), dialogResult.get().getISSN(), dialogResult.get().getRange());
					}
				};
			}
		};

		serv.setOnSucceeded((WorkerStateEvent t) -> {
			
			
			if ((serv.getValue() != null) && (serv.getValue().size() > 0)) {
				creFile = null;
				if (analyzeFiles(ImportFormat.CROSSREF, serv.getValue())) {
					importFiles(ImportFormat.CROSSREF, serv.getValue());
				} else {
					StatusBar.get().setValue("Import aborted by user");
				}
			} else {
				StatusBar.get().setValue("Download aborted by user");
			}
			
			wait.close();
			
		});

		serv.setOnFailed((WorkerStateEvent t) -> {
			Throwable e = t.getSource().getException();
			if (e instanceof IOException) {
				new ExceptionStacktrace("Error during Crossref download!", e).showAndWait();
			} else if (e instanceof BadResponseCodeException) {
				new ExceptionStacktrace("Could not get data from Crossref (" + ((BadResponseCodeException)e).code + ")", e).showAndWait();
			} else {
				new ExceptionStacktrace("Exception", e).showAndWait();
			}
			wait.close();
		});

		serv.start();

		wait.showAndWait().ifPresent(cancel -> {
			crTable.setAborted(cancel);
		});
		
		

	}

	
	private boolean exportFile(ExportFormat source, boolean saveAs) throws IOException {

		File selFile;
		if ((source == ExportFormat.CRE) && (creFile != null) && (!saveAs)) {
			selFile = creFile;
		} else {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(String.format("Export %s", source.getLabel()));
			fileChooser.setInitialDirectory(UISettings.get().getLastFileDir());
			fileChooser.getExtensionFilters().add(new ExtensionFilter(source.getLabel(), Arrays.asList(new String[] { "*." + source.getFileExtension()})));
			fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", Arrays.asList(new String[] { "*.*" })));
			selFile = fileChooser.showSaveDialog(CitedReferencesExplorerFX.stage);
		}

		if (selFile == null) return false;

		UISettings.get().setLastFileDir(selFile.getParentFile());	// save last directory to be used as initial directory
		
		Service<Void> serv = new Service<Void>() {
			@Override
			protected Task<Void> createTask() {
				return new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						source.save(selFile, UISettings.get().getIncludePubsWithoutCRs());
						if (source == ExportFormat.CRE) creFile = selFile;
						return null;
					}
				};
			}
		};

		serv.setOnSucceeded((WorkerStateEvent t) -> {
			updateTitle();
		});

		serv.setOnFailed((WorkerStateEvent t) -> {
			new ExceptionStacktrace("Error during file export!", t.getSource().getException()).showAndWait();
		});

		serv.start();
		return true;
	}

	
	/*
	 * FILE Menu
	 */

	@FXML
	public void OnMenuFileOpen() throws OutOfMemoryError, Exception {
		importFiles(ImportFormat.CRE);

	}

	@FXML
	public void OnMenuFileImportWoS(ActionEvent event) throws IOException {
		importFiles(ImportFormat.WOS);
	}

	@FXML
	public void OnMenuFileImportScopus(ActionEvent event) throws IOException {
		importFiles(ImportFormat.SCOPUS);
	}

	@FXML
	public void OnMenuFileImportCrossrefFile(ActionEvent event) throws IOException {
		importFiles(ImportFormat.CROSSREF);
	}

	@FXML
	public void OnMenuFileImportCrossrefSearch(ActionEvent event) throws IOException {
		downloadCrossref();
	}

	@FXML
	public void OnMenuFileImportCSV(ActionEvent event) throws OutOfMemoryError, Exception {
		importFiles(ImportFormat.CSV);
	}

	@FXML
	public void OnMenuFileImportCSVAll() throws IOException {
		importFiles(ImportFormat.CSVPUB);
	}	

	@FXML
	public void OnMenuFileSave() throws IOException {
		exportFile(ExportFormat.CRE, false);
		// saveFile(false);
	}

	@FXML
	public void OnMenuFileSaveAs() throws IOException {
		exportFile(ExportFormat.CRE, true);
		// saveFile(true);
	}

	@FXML
	public void OnMenuFileExportWoS() throws IOException {
		exportFile(ExportFormat.WOS, true);
	}

	@FXML
	public void OnMenuFileExportScopus() throws IOException {
		exportFile(ExportFormat.SCOPUS, true);
	}

	@FXML
	public void OnMenuFileExportCSVGraph() throws IOException {
		exportFile(ExportFormat.CSV_GRAPH, true);
	}

	@FXML
	public void OnMenuFileExportCSVCR() throws IOException {
		exportFile(ExportFormat.CSV_CR, true);
	}

	@FXML
	public void OnMenuFileExportCSVPub() throws IOException {
		exportFile(ExportFormat.CSV_PUB, true);
	}

	@FXML
	public void OnMenuFileExportCSVAll() throws IOException {
		exportFile(ExportFormat.CSV_CR_PUB, true);
	}

	@FXML
	public void OnMenuFileSettings() throws IOException {

		int old_NPCTRange = UISettings.get().getNPCTRange();
		int old_MedianRange = UISettings.get().getMedianRange();
		new Settings().showAndWait().ifPresent(noOfErrors -> {

			if (old_NPCTRange != UISettings.get().getNPCTRange()) {
				crTable.setNpctRange(UISettings.get().getNPCTRange());
				crTable.updateData();
			} 
			if (old_MedianRange != UISettings.get().getMedianRange()) {
				crTable.getChartData().setMedianRange(UISettings.get().getMedianRange());
			}

			for (int i = 0; i < crChart.length; i++) {
				if (UISettings.get().getChartEngine() == i) {
					crChart[i].setFontSize();
					crChart[i].setVisible(true);
				} else {
					crChart[i].setVisible(false);
				}

			}
			updateTableCRList();
			// TODO: Apply settings changes
		});
	}

	@FXML
	public void OnMenuFileExit() {
		CitedReferencesExplorerFX.stage.fireEvent(new WindowEvent(CitedReferencesExplorerFX.stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	/*
	 * VIEW Menu
	 */

	@FXML
	public void OnMenuViewInfo() {
		new Info().showAndWait();
	}

	@FXML
	public void OnMenuViewCR() {
		List<Integer> sel = getSelectedCRs();
		if (sel.size() == 1) {
			new CRInfo(crTable.getCRById(sel.get(0))).showAndWait();
		} else {
			new ConfirmAlert("Remove Cited References", true, new String[] { String.format("%s Cited References selected.", (sel.size() == 0) ? "No" : "Too many") }).showAndWait();
		}
	}

	@FXML
	public void OnMenuViewPub() {
		List<Integer> sel = getSelectedCRs();
		if (sel.size() == 1) {
			new CRPubInfo(crTable.getCRById(sel.get(0))).showAndWait();
		} else {
			new ConfirmAlert("Remove Cited References", true, new String[] { String.format("%s Cited References selected.", (sel.size() == 0) ? "No" : "Too many") }).showAndWait();
		}
	}

	@FXML
	public void OnMenuViewShowCRsWOYears(ActionEvent event) {
		crTable.setShowNull(((CheckMenuItem) event.getSource()).isSelected());
		updateTableCRList();
	}

	@FXML
	public void OnMenuViewFilterByRPY(ActionEvent event) {
		new Range("Filter Cited References", "Select Range of Cited References Years", UISettings.RangeType.FilterByRPYRange, CRTable.get().getStatistics().getMaxRangeRPY()).showAndWait().ifPresent(range -> {
			filterByRPY(range, false);
		});
	}

	@FXML
	public void OnMenuViewShowCluster() {
		List<Integer> sel = getSelectedCRs();
		if (sel.size() > 0) {
			crTable.filterByCluster(sel);
			updateTableCRList();
		} else {
			new ConfirmAlert("Filter Cited References by Cluster", true, new String[] { "No Cited References selected." }).showAndWait();
		}
	}

	@FXML
	public void OnMenuViewSearch() {

		try {

			new Search().showAndWait().ifPresent(query -> {

				try {
					/* REMOVED Lucene Search due to module problems
					CRSearch.get().search(query);
					*/

					tableView.orderBySearchResult();

					// Arrays.stream(id).forEach(it -> System.out.println(it));
					// List<CRType> show = crTable.getCR().filter(cr -> IntStream.of(id).anyMatch(it
					// -> cr.getID() == it)).collect(Collectors.toList());
					//
					// System.out.println("Size = " + show.size());

					// crTable.filterByCR(null); // show);
					updateTableCRList();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@FXML
	public void OnMenuViewShowAll() {
		showWOYear.setSelected(true);
		crTable.showAll();
		updateTableCRList();
	}

	@FXML
	public void OnMenuViewResetChart() {

		for (int i = 0; i < crChart.length; i++) {
			if (crChart[i].isVisible()) {
				crChart[i].autoRange();
			}
		}
	}

	/*
	 * DATA Menu
	 */

	@FXML
	public void OnMenuDataRemoveSelected() {

		List<Integer> toDelete = getSelectedCRs();
		int n = toDelete.size();
		new ConfirmAlert("Remove Cited References", n == 0, new String[] { "No Cited References selected.", String.format("Would you like to remove all %d selected Cited References?", n) }).showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.YES) {
				crTable.removeCR(toDelete);
				updateTableCRList();
			}
		});
	}

	@FXML
	public void OnMenuDataCopySelected() {

		// StringBuffer buf = new StringBuffer();


		String s = tableView.getSelectionModel().getSelectedIndices().stream()
			.map (index -> 
				tableView.getColumns().filtered(c -> c.isVisible()).stream()
					.map(col -> String.format("\"%s\"", col.getCellData(index)))
					.collect(Collectors.joining("\t")))
			.collect(Collectors.joining("\n"));

	

		// tableView.getSelectionModel().getSelectedItems().stream().forEach((CRType<?> cr) -> {
		// 	tableView.getColumns().filtered(c -> c.isVisible()).forEach(c -> {
		// 		buf.append("\"");


		// 		// buf.append(c.getCellData(cr));
		// 		buf.append("\"\t");
		// 	});
		// 	buf.append("\n");
		// });

		final ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(s /*buf.toString()*/);
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	}

	@FXML
	public void OnMenuDataRemovewoYears() {

		int n = CRTable.get().getStatistics().getNumberOfCRsWithoutRPY();
		new ConfirmAlert("Remove Cited References", n == 0, new String[] { "No Cited References w/o Year.", String.format("Would you like to remove all %d Cited References w/o Year?", n) }).showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.YES) {
				crTable.removeCRWithoutYear();
				updateTableCRList();
			}
		});
	}

	@FXML
	public void OnMenuDataRemoveByRPY() {

		final String header = "Remove Cited References";
		new Range(header, "Select Range of Cited References Years", UISettings.RangeType.RemoveByRPYRange, CRTable.get().getStatistics().getMaxRangeRPY()).showAndWait().ifPresent(range -> {
			long n = CRTable.get().getStatistics().getNumberOfCRsByRPY(range);
			new ConfirmAlert(header, n == 0, new String[] { String.format("No Cited References with Cited Reference Year between %d and %d.", range.getMin(), range.getMax()),
					String.format("Would you like to remove all %d Cited References with Cited Reference Year between %d and %d?", n, range.getMin(), range.getMax()) }).showAndWait().ifPresent(btn -> {
						if (btn == ButtonType.YES) {
							crTable.removeCRByYear(range);
							updateTableCRList();
						}
					});
		});
	}

	@FXML
	public void OnMenuDataRemoveByNCR() {

		final String header = "Remove Cited References";
		new Range(header, "Select Number of Cited References", UISettings.RangeType.RemoveByNCRRange, CRTable.get().getStatistics().getMaxRangeNCR()).showAndWait().ifPresent(range -> {
			long n = CRTable.get().getStatistics().getNumberOfCRsByNCR(range);
			new ConfirmAlert(header, n == 0, new String[] { String.format("No Cited References with Number of Cited References between %d and %d.", range.getMin(), range.getMax()),
					String.format("Would you like to remove all %d Cited References with Number of Cited References between %d and %d?", n, range.getMin(), range.getMax()) }).showAndWait().ifPresent(btn -> {
						if (btn == ButtonType.YES) {
							crTable.removeCRByN_CR(range);
							updateTableCRList();
						}
					});
		});
	}

	@FXML
	public void OnMenuDataRemoveByPERC_YEAR() {

		final String header = "Remove Cited References";
		new Threshold(header, "Select Threshold for Percent in Year", "<", 0.0).showAndWait().ifPresent(cond -> {
			String comp = cond.getKey();
			double threshold = cond.getValue().doubleValue();
			long n = CRTable.get().getStatistics().getNumberOfCRsByPercentYear(comp, threshold);
			new ConfirmAlert(header, n == 0, new String[] { String.format("No Cited References with Percent in Year %s %.1f%%.", comp, 100 * threshold),
					String.format("Would you like to remove all %d Cited References with Percent in Year %s %.1f%%?", n, comp, 100 * threshold) }).showAndWait().ifPresent(btn -> {
						if (btn == ButtonType.YES) {
							crTable.removeCRByPERC_YR(comp, threshold);
							updateTableCRList();
						}
					});
		});
	}

	@FXML
	public void OnMenuDataRetainCRById() {

		new TextInput("Retain Cited References By Id", "Specify list if CR Ids").showAndWait().ifPresent(list -> {
			if (list != null) {
				List<Integer> toRetain = Arrays.stream(list.split("\\D")).map(Integer::valueOf).collect (Collectors.toList());
				crTable.retainCR(toRetain);
			}
		});
	}

	@FXML
	public void OnMenuDataRetainSelected() {

		List<Integer> toRetain = getSelectedCRs();
		int n = toRetain.size();
		new ConfirmAlert("Remove Publications", n == 0, new String[] { "No Cited References selected.", String.format("Would you like to remove all citing publications that do not cite any of the selected %d Cited References?", n) }).showAndWait()
				.ifPresent(btn -> {
					if (btn == ButtonType.YES) {
						crTable.removePubByCR(toRetain);
						updateTableCRList();
					}
				});
	}

	@FXML
	public void OnMenuDataRetainByRPY() {

		new Range("Retain Publications", "Select Range of Citing Publication Years", UISettings.RangeType.RetainByPYRange, CRTable.get().getStatistics().getMaxRangePY()).showAndWait().ifPresent(range -> {
			long n = CRTable.get().getStatistics().getNumberOfPubs() - CRTable.get().getStatistics().getNumberOfPubsByCitingYear(range);
			new ConfirmAlert("Remove Publications", n == 0, new String[] { String.format("All Citing Publication Years are between between %d and %d.", range.getMin(), range.getMax()),
					String.format("Would you like to remove all %d citing publications with publication year lower than %d or higher than %d?", n, range.getMin(), range.getMax()) }).showAndWait().ifPresent(btn -> {
						if (btn == ButtonType.YES) {
							crTable.retainPubByCitingYear(range);
							updateTableCRList();
						}
					});
		});
	}

	/*
	 * Standardization Menu
	 */

	@FXML
	public void OnMenuStdCluster() {

		new Thread(() -> {
			crTable.getClustering().generateInitialClustering();
			matchView.setVisible(true);
			tablePane.requestLayout();
			matchView.updateClustering();
			updateTableCRList();

		}).start();
	}

	@FXML
	public void OnMenuStdMerge() {

		long n = CRTable.get().getStatistics().getNumberOfCRs() - CRTable.get().getClustering().getNumberOfClusters();
		new ConfirmAlert("Merge clusters", n == 0, new String[] { "No Clusters to merge.", String.format("Merging will aggregate %d Cited References! Are you sure?", n) }).showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.YES) {
				new Thread(() -> {
					crTable.merge();
					matchView.setVisible(false);
					updateTableCRList();
					// tablePane.requestLayout();
				}).start();
			}
		});
	}

	/*
	 * Help Menu
	 */

	@FXML
	public void OnMenuHelpManual() {
		// TODO: hyper link
		// HostServicesFactory.getInstance(CitedReferencesExplorer.app).showDocument(CitedReferencesExplorer.manual_url);
	}

	@FXML
	public void OnMenuHelpAbout() {
		new About().showAndWait();
	}

}
