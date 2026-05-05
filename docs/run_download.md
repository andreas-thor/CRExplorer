---
pdf: false
---


# Running CRExplorer & System Requirements

CRExplorer is packaged as a Java Archive (JAR) and requires a system with Java 17 (or higher) support.

[Download Version 2.0](https://github.com/andreas-thor/CRExplorer/releases/download/v2.0/crexplorer.jar){ .md-button .md-button--primary }

On most systems a double click on the runnable JAR file will start CRExplorer. If you want to run the JAR file from command line please refer to the official Java documentation on [Running JAR-Packaged Software](https://docs.oracle.com/javase/tutorial/deployment/jar/run.html). Here you can also set the heap space size if you are processing large files.

If you want to employ CRExplorer's script language, you run your script with the following command line:

```bash 
java -cp crexplorer.jar cre.Script myscript.crs
```

where ``myscript.crs`` should be replaced by the user's script filename.




## Releases

CRExplorer version 1.9 was released on July 16, 2018. This version includes the following new features and improvements:

* **CrossRef**: CRExplorer opens one or several datasets from [CrossRef](https://www.crossref.org/). In addition, cited references data can be directly imported into the CRExplorer by using the search interface for CrossRef data.
* **Drawing several samples**: Since the introduction of the reference publication year spectroscopy (RPYS) method and the corresponding program CRExplorer, many studies have been published revealing the historical roots of topics, fields, and researchers. The application of the method was restricted up to now by the available memory of the computer used for running the CRExplorer. Thus, many users could not perform RPYS for broader research fields or topics. Now, the script language of the CRExplorer can be used to draw many samples from the full population dataset. First empirical explorations (see [The introduction of RPYS sampling using the example of climate change research](https://arxiv.org/abs/1807.04673)) have shown, that the RPYS based on many samples can lead to very similar results as the results based on the full population dataset.

CRExplorer version 1.8.2 was released on February 1, 2018. This version includes the following new features and improvements:

* **Sequence**: To reveal impact sequences over time for cited references, cited references are classified as on average ("0"), above average ("+"), and below average ("-") citation impact in citing years. The benchmark is the mean citation impact of all other cited references published in the same year. For example, the sequence [---+++000] means that the cited reference has been cited below average in the first three citing years, above average in the next three years, and on average in the last three citing years.
* **Types of sequences**: The sequences are used to identify specific types in terms of the symbols ("+", "-", "0"). The types are labelled as follows: sleeping beauty with low or no citation impact over a longer initial period and high citation impact later; constant performer with a constant and considerable amount of citation impact over time; hot paper with high citation impact directly after the publication and low citation impact later; life cycle with very different citation impact across the citing years.
* **Samples**: In many cases, the full dataset from Web of Science or Scopus (the population) cannot be completely imported in the program because the available memory on the computer is restricted. Thus, the user has the new option to reduce the dataset by loading only a sample from the population. Three samples can be drawn from the population: random, systematic, or cluster sample.
* **Script language**: CRExplorer provides a tailored script language that allows the application of the most important functions. This can be useful for recurring experiments using different data sets or for processing large volumes of data. Users can execute scripts from the command line. The use of the script language is explained in the handbook.

CRExplorer version 1.7.7 was released on June 30, 2017. This version includes the following new features and improvements:

* **Publications**: Users can specify (under "File" - "Settings" - "Import/Export") if CRExplorer should include publications that do not have any cited references.
* **Settings**: Several enhancements in the settings incl. chart layout options (font size, stroke size).

CRExplorer version 1.7.5 was released on May 31, 2017. This version includes the following new features and improvements:

* **Citing Publications**: Users can inspect the list of citing publications for selected cited references via "View" - "Citing Publications".
* **Searching**: Users can do keyword searches for cited references (including wildcards such as *).
* **Indicators**: Three indicators are included which show in how many citing years the cited publication (cited reference) belongs to the 50%, 25%, or 10% most cited publications – compared to all other cited publications (cited references) which have been appeared in the same cited year.
* **CSV output formats**: CRExplorer offers different CSV-based output formats (graph data, cited references and/or citing publications)
* **Copy + Paste**: Users can copy selected cited references to clipboard (Ctrl+C) and paste it in other programs (e.g., Excel).
* **New Chart layout**: Beside the standard chart (JFreeChart), CRExplorer now employs a new web-based, interactive chart type (HighCharts). Users can switch between the types in the "File" - "Settings" menu.
* **User Interface**: CRExplorer's GUI is now based on JavaFX.
* **Handbook**: A handbook is available which explains the elements and functions of the program.

CRExplorer version 1.6.8 was released on August 29, 2016. This version includes the following new features and improvements:

* **Co-Citation**: If the user marks at least one cited reference in the table and selects "Data" – "Retain Publications Citing Selected Cited References", all cited references which are co-cited with the marked cited reference in citing publications are kept (all other cited references are dropped). See [Which early works are cited most frequently in climate change research literature? A bibliometric approach based on Reference Publication Year Spectroscopy](http://arxiv.org/abs/1608.07960) for a first use case.
* **Performance**: Performance improvements (e.g., for data clustering) using multiple threads on multi-core machines.
* **Java 8**: CRExplorer requires a Java 8 run-time.

CRExplorer version 1.6.7 was released on July 5, 2016. This version includes the following new features and improvements (see [New features of CRExplorer for a detailed description](http://arxiv.org/abs/1607.01266)):

* **Scopus**: Using "File" – "Import" – "Scopus", CRExplorer reads files from Scopus. The file format "CSV" (including citations, abstracts and references) should be chosen in Scopus for downloading records.
* **Export facilities**: Using "File" – "Export" – "Scopus", CRExplorer exports files in the Scopus format. Using "File" – "Export" – "Web of Science", CRExplorer exports files in the Web of Science format. These files can be imported in other bibliometric programs (e.g. VOSviewer).
* **Space bar**: Select a specific cited reference in the cited references table, press the space bar, and all bibliographic details of the CR are shown.
* **Internal file format**: Using "File" – "Save" working files are saved in the internal file format "*.cre". The files include all data including matching results and manual matching corrections. The files can be opened by using "File" – "Open".
