
# Introduction 

The program CRExplorer can be used to analyse the cited references (CRs) data in a publication set retrieved from Web of Science (WoS) or other data sources. The program produces empirical results in a graphical or table format that can be included in papers or presentations ([Thor et al., 2016a](references.md#Thor2016a), [2016b](references.md#Thor2016b)). It is written in the Java programming language and, thus, runs on most hardware and operating system platforms. A Java variant with JavaFX (e.g., Oracle Java) is needed. The minimum required Java version is 21. The program can be used free of charge.


The program can be started from the command line. The user should go to the folder where the downloaded file ``crexplorer.jar`` is located and run:

```bash
java -jar crexplorer.jar
```


The program was primarily developed to identify those publications in fields, of topics, or by researchers which have been most frequently referenced. It is especially suitable to study the historical roots of fields, topics, or researchers by Reference Publication Year Spectroscopy (RPYS, e.g., [Barth et al., 2014](references.md#Barth2014); [Marx et al., 2014](references.md#Marx2014)). RPYS was introduced by [Marx et al., 2014](references.md#Marx2014) and “is based on the analysis of the frequency with which references are cited in the publications of a specific research field in terms of the publication years of these CRs. The origins show up in the form of more or less pronounced peaks mostly caused by individual publications that are cited particularly frequently” (p. 751). Many RPYS studies using CRExplorer have been published in recent years: A search in the WoS found more than 150 papers applying the method [date of search with `` 'ts = (RPYS or "reference* publication year* spectroscopy" or "cited reference" analysis" or CRExplorer)' ``: 12 March 2026].


As support for collecting the complete set of publications on a topic or on a specific field, the user can inspect the documents which are listed by members of groups in Mendeley (see www.mendeley.com). For example, there exists an altmetrics group listing most of the publications on this topic. CRExplorer reads, analyses, and edits the CRs of the collected publications which are previously retrieved from WoS, Scopus (Elsevier), or other sources. In order to analyze the CRs, the user can consult (1) a graph for identifying most frequently cited reference publication years (RPYs) and (2) a table of CRs which account for specific RPYs. Field-normalization of citation impact measurement is ensured in the analysis by the first step of the CRs analysis: the selection of the publication set on which citation impact is measured.


CRExplorer includes a disambiguation feature which clusters and merges variants of the same CR. This means that the program can also be used as a tool for preparing CR data for other programs, e.g., VOSviewer ([van Eck & Waltman, 2010](references.md#vanEck2010)) or metaknowledge ([McLevey & McIlroy-Young, 2017](references.md#McLevey2017)). For this purpose, the data are exported in WoS or Scopus format and imported in other programs for further processing. Furthermore, the data can be transferred from one format into another: Imports from Scopus can be exported as WoS files and imports from WoS can be exported as Scopus files ([Thor et al., 2016b](references.md#Thor2016b)).
