# Options panel

## File


### Open

CRExplorer uses an internal file format "*.cre", which can be used as a "working format" and for the exchange of working files. The file contains all data including changes by the user. Using Microsoft Windows, one can double click on any *.cre file and thus run CRExplorer automatically. Note that the available memory on the computer decreases the more datasets have been opened and without terminating CRExplorer in-between. Thus, memory is not freed when a new dataset is loaded.

### Import

**Web of Science**: CRExplorer opens one or several datasets from Web of Science (WoS). The datasets should be downloaded using the option **Save to Other File Formats**. As **Record Content** select **Full Record and Cited References** and as File Format select **Other Reference Software**. The records have to be searched in the WoS Core Collection in order to be able to save full records including the cited references (CRs).

**Scopus**: CRExplorer opens one or several datasets from Scopus. The file format **CSV** (including citations, abstracts, and references) should be chosen for downloading records.

Before loading the WoS or Scopus data into CRExplorer, the program analyses the file(s) which the user would like to import. When the analyses are finished, some basic information on the full dataset (we would like to call it “population”) are presented to the user in a window (in fields with grey background): number of non-distinct CRs, minimum/maximum reference publication year (RPY), number of (citing) publications, minimum/maximum publication year (of the citing publication), number of CRs without RPYs, and number of (citing) publications without publication year.

**Crossref file**: CRExplorer opens one or several datasets from Crossref (see [https://www.crossref.org/]()). In addition, cited references data can be directly imported into CRExplorer by using the search interface for Crossref data (**Crossref search**). The search can be run based on (1) a list of DOIs provided by the user (whitespace-separated) or (2) an ISSN (e.g., 0138-9130 for the journal *Scientometrics*). The range of RPYs can be set to restrict the set of imported data.

**CSV-file**: Datasets in csv format can be imported. The format is the same as for the csv export. The same column header names must be used. However, not all columns need to be provided in the csv-file to be imported: At least, the columns **CR**, **RPY**, and **N_CR** should be provided. If no column **N_CR** is provided, the program assumes that each provided reference occurs once. The column **ID** is ignored (even if it is provided) and a new unique ID will be assigned. 


If the imported dataset from any data source (e.g., WoS) contains a lot of CRs from many publications, the full dataset cannot be completely imported because of restrictions by the available memory on the computer of the user. There are basically two options to deal with the problem: (1) It is possible to run the program in a database mode:

``java -jar crexplorer.jar -db`` 

In this mode, the processed data is stored in a local database on the computer of the user (instead of the main memory). CRExplorer consumes much less main memory then but needs longer time for certain operations. It is possible to control the location of the database file:

``java -jar crexplorer.jar -db=DB_URL``

DB_URL is to be used as documented at [https://jdbc.postgresql.org/documentation/use](). An example DB_URL is ``jdbc:postgresql://localhost:5432/db_name?user=db_user``. Here, ``db_name`` and ``db_user`` are the database name and database user. Further information can be found at [https://github.com/andreas-thor/CRExplorer/blob/main/README.md]()

(2) The user has the option to reduce the dataset by loading only a sample from the population of publications. The sample can be defined by importing CRs from specific RPYs and/or citing publication years. The range of the desired years can be included in the fields with the white background in the window. Additionally, the publications/CRs without year/RPYs can be excluded (by unmarking the corresponding ticks in the window).

Another possibility is to draw one of the following three types of samples from the population (see the examples and suggestions in [Haunschild et al., 2020](references.md#Haunschild2020)):

* **Random Sampling**: The number of CRs included by the user in the field with white background are randomly selected. For example, if the user wants to import 100 CRs out of 400 overall CRs, CRExplorer randomly selects 25% of all CRs.
* **Systematic Sampling**: The number of CRs filled in by the user is used to select the sample uniformly distributed over the list of all CRs of the citing publications. For example, if the user wants to import 100 CRs out of 400 overall CRs, CRExplorer systematically selects 25% of the list of all CRs by picking the 1st, 5th, 9th, and so on CR.
* **Cluster Sampling**: From the years which are in-between the minimum and maximum publication years (see above) one citing year is randomly selected. Then, all CRs in the papers published in this year are selected as sample and imported. The results of [Bornmann and Mutz (2015)](references.md#Bornmann2015) reveal that the restriction on all CRs from a recent citing year leads to very similar results as the consideration of all CRs from several citing years in references analysis. Note that the selection of one citing year in CRExplorer implicates that citation distributions over many citing years cannot be analyzed (see section 3.2).

Of course, the three sampling options can be combined with the selection of citing and/or cited years. Furthermore, many samples instead of one sample can be drawn from the population to optimize the RPYS results ([Haunschild et al., 2020](references.md#Haunschild2020)).

CRExplorer recommends a sample size to the user based on the available memory on the computer. This number is shown after **Estimated maximal Number which can be loaded in Memory**. If the user wants to draw a random or systematic sample, the user can decide which number of CRs is tried to be imported. This number can be filled in the corresponding white field in the window. In case of selecting the cluster sample option, the user cannot enter any number. The program undertakes the corresponding selections. If the user tries to import a number of CRs (sample or population), which is too large for the available memory on the computer (independent of the selected sampling strategy), an error message appears and the import is stopped.

The analysis of a sample instead of the population is only an opportunity to handle the restrictions, which are given by the computer of the user. It is always the best way in the cited references analysis to analyze the population (by running CRExplorer with the database option). If the user, however, decides to work with a sample instead of the population, inference statistics can be applied to draw conclusions from the sample to the population. For using inference statistics, the user can download the sample data as csv-file from CRExplorer (see section 4.1.5). Using statistics programs, such as Stata (see [www.stata.com]()) or R (see [www.r-project.org]()), the user can apply inference statics to the exported (random) sample ([Cumming, 2012](references.md#Cumming2012)). In the SSC Archive, the user finds the Stata command “plotrpys” which produces monochrome and colored graphs including 95% confidence intervals. These graphs can be used in connection with sample data (see section 4.1.5).

The number of CRs which are shown to the user in the info window after the analyses of the WoS files (or files from another source) refers to non-distinctive counting. Thus, CRs have been multiply counted, if CRs existed multiply in the dataset. In most of the cases, the user will observe a reduced number of CRs after importing the population or sample. The reason is that the CRs are distinctly counted after importing.

Note that the available memory on the computer decreases the more datasets have been imported without terminating CRExplorer in-between. Thus, memory is not freed when a new dataset is imported.


### Save

The program saves the dataset in the internal file format “*.cre”.


### Save as

The program saves the dataset in the internal file format “*.cre” and asks for a file name.


### Export

**Web of Science**: The dataset is exported in the WoS format as specified in the import section (see section 4.1.2).

**Scopus**: The dataset is exported in the Scopus format as specified in the import section (see section 4.1.2).

Note that the export can only contain data from the import, when one transfers from Scopus to WoS. WoS files, for example, only consider the first authors while Scopus files include all authors in the CR field. If one transfers from WoS to Scopus, not all information can be provided, since this information is not available in downloads from WoS.

**CSV (Graph)**: The graph data are exported for further processing in programs such as Excel, R, Stata, or GnuPlot. The export does not only include the sum of CRs per RPY (as presented in CRExplorer), but also the average. We provide Stata (plotrpys) and R (BibPlots) commands which produce monochrome and colored graphs. Both can be found in SSC Archive (in the case of Stata) and CRAN (Comprehensive R Archive Network, in the case of R), respectively.

**CSV (Cited References)**: A table with CRs data is saved as a csv-file.

**CSV (Citing Publications)**: A table with data on the citing publications is saved as csv-file.

**CSV (Cited References + Citing Publications)**: A table with both CRs data and data on the citing publications is saved as a csv-file.


### Settings

#### Table

In the sections **Cited References**, **Indicators**, **Clustering**, and **Searching** the columns can be selected which should be displayed in the table. It is also possible to select/deselect all columns. These functions enable to restrict the columns to those which are needed for a specific analysis. In the section **Value Settings**, the **Number of Digits** for all numerical columns can be modified.

Furthermore, the **N_PCT Range** can be adjusted. It might be a problem in computing **N_TOP50**, **N_TOP25**, and **N_TOP10** that the citation counts in a citing year are inflated by zeros (and/or similar values). Thus, we included the option in the program to extend the number of citing years which are considered in calculating **N_TOP50**, **N_TOP25**, and **N_TOP10**. If only the citing year itself should be considered in the analysis, the **NPCT Range** is set to 0. If it is set to 1, the thresholds for the top 50%, 25%, and 10% are computed based on citations from the preceding (*t*-1) and succeeding (*t*+1) citing years. This doubles the underlying dataset in the first and last citing year (since year *t*-1 or *t*+1, respectively, do not exist) and triples it in all other years.


#### Chart

**Chart Layout**: The user can select the lines which should be displayed: Number of Cited References, Deviation from the Median or both. The deviation of the number of CRs in each year Y from the median for the number of CRs in the previous, the current, and the following years can be set. The default is 2: *Y*–2 ; *Y*–1 ; *Y* ; *Y*+1 ; *Y*+2. Thus, the user can change the number to any other number and can thus work with medians calculated based on different time windows. Furthermore, Stroke Size and Shape size for the lines in the chart as well as Label Font Size and Tick Font Size for the axes can be set.

**Chart Engines**: Two different chart types (**JFreeChart** and **HighCharts**) can be selected. Both types have the same functionality: The user can zoom into the graph and click on a peak whereby the underlying CRs are sorted and marked correspondingly in the table. However, both types are different in look and feel. **JFreeChart** is a static visualization, similar to graph types in Excel or GnuPlot; **HighCharts** is a dynamic, web-based (“modern”) visualization. The underlying data of any graph can be downloaded with **File** – **Export** – **Graph CSV** and can be visualized in another software (e.g., Excel, R, or GnuPlot).

The **JFreeChart** can be saved in various formats by right-clicking on the graph. This is not possible with **HighCharts**. If the user wants to include the graph from **HighCharts** into another program (e.g., Microsoft Word), he or she should use programs such as the Snipping Tool (which is available in Microsoft Windows) or KSnapshot (which is available in KDE Linux) to cut the graph.
If there are any problems with changing of chart types, try **View** – **Reset Chart** (see section 4.3.9) or restart CRExplorer.


###	Exit

Leave CRExplorer.

## Edit

The user may restrict the CR analysis to a certain time period. Very early and most recent years are frequently not very helpful for the identification of the most frequently cited publications in the history. Although CRExplorer allows for the selection of periods in on-screen visualization, this selection does not lead to changes in the dataset. However, there are several ways of removing data from a dataset.

### Remove selected Cited References

Rows in the table can be marked and deleted using the menu item.

### Remove selected Cited References w/o Year

All CRs are removed without a year in the column RPY.

### Remove by Reference Publication Year.

The user can remove the data for specific RPYs.

### Remove by Number of Cited References

All CRs with a number of CR counts (column **N_CR**) within the specified range are deleted. This kind of restriction is helpful in identifying publications from early RPYs with a substantial impact (and to suppress the noise of less cited publications). Furthermore, in RPYs with many sparsely cited publications the publications with substantial impact can be easier identified then.

### Remove by Percent in Year

CRs can be removed by using thresholds for the column PERC_YR. Thus, it is possible to remove lowly CRs whereby “lowly” is defined in terms of the citation distribution in the RPYs.

### Retain Cited References by ID

One can select the citation environment of a specific CR (or of two or even more CRs) in the form of all co-cited CRs and analyze these CRs (e.g., for applying RPYS-CO, see [Marx et al., 2017](references.md#Marx2017)). The specific CRs can be prominent and seminal works which are used as a kind of marker or tracer references for a specific topic in a field. We assume that papers which cite the selected CRs are potential candidates for citing also many other CRs relevant in a specific historical context. This method takes advantage of the fact that concurrently cited (co-cited) papers are more or less closely related to each other ([Small, 1977](references.md#Small1977)).

By selecting **Retain Cited References by ID**, the CRs are restricted based on the IDs specified by the user (e.g., 20, 25, 40). Thus, only CRs are retained which are co-cited with the IDs (CRs) specified.

### Retain Publications citing Selected Cited References

The CRs are restricted based on the CRs marked by the user in the table. Thus, only CRs are retained which are co-cited with the CRs specified.

### Retain Publications within Citing Publication Year

The CRs are restricted based on the publication years of the citing publications. The user can specify the range of (citing) publication years. Thus, only CRs are retained which are cited or co-cited with the CRs in citing publications from the specified years.

### Copy Selected Cited References

The CR (CRs) which is (are) selected in the table is (are) copied to the clipboard for use in other programs (e.g., spreadsheet or word processing programs).

## View

### Info

The message box provides some basic information on the dataset: **Number of Cited References** and **Number of Cited References (shown)**. The user has the option to select data temporarily. Thus, the complete number of CRs and the temporarily selected number are shown. Furthermore, the user can cluster and merge CR variants, and the number of calculated clusters is shown. The message box also contains the **Range of Cited References Years**, the **Number of different Cited References Years**, the **Number of Publications** (including publications without CRs), the **Number of Citing Publications** (excluding publications without CRs), the **Range of Citing Publication Years**, and the **Number of different Citing Publication Years**.

### Cited Reference (Details)

An info box including all information from the columns appears for a selected CR.

### Citing Publications

The publications are shown which cite the CR (CRs) selected by the user.

### Show Cited References w/o Years

Scopus or WoS data sets contain CRs without RPY. As a rule, these CRs are not considered in the analyses of CRExplorer, but can be shown optionally in the table.

### Filter by Reference Publication Year

The graph is temporarily restricted to the minimum and maximum years included.

### Show Cited References of selected cluster(s) only

Restrict the CRs to only those which are in the same cluster(s) (used for disambiguation) as the selected CR(s).

### Search Cited References

CRs including the search string are sorted to the first positions in the tabular list of CRs. The column **Search_Score** in the table contains the value 1 for CRs including the search string and the value 0 otherwise. The column **Search_Score** itself can be used for sorting the data.

### Show all Cited References (currently X of Y)

All filters set by the user for filtering CRs are deactivated. If X in the menu item is smaller than Y, a filter is active.

### Reset Chart

The **JFreeChart** or **HighCharts** axes are reset to their maximum range.

###	Disambiguation

The user has the possibility to detect variants of the same CR, cluster them, and merge their occurrences (number of CRs). The clustering uses the table with the list of CRs as input file. Three algorithms are available for clustering CRs: Levenshtein distance, cosine similarity, and Jaccard similarity. Since the clustering (and merging) is a complex process which needs a lot of computer resources, the user is advised to cluster only those data which is of interest. 


The clustering and merging of the data is especially important for the Scopus data, since the CR data in Scopus is more heterogeneous than in WoS. Scopus data contains more information than WoS data (all authors and the titles of the referenced publication) which increases the probability of variants of the same CR. Furthermore, Scopus data may contain fragmented CR data which cannot be completely parsed into the bibliographic categories of CRExplorer (e.g., authors, titles, or volume numbers). The heterogeneous data of Scopus can be inspected best by sorting the list of CRs under the column **Authors**. A possible way of dealing with the heterogeneous CRs is to try their clustering and merging with CRs including complete information (if fragmented CRs are variants of these CRs).

### Cluster equivalent Cited References Levenshtein

In the first step of eliminating variants of CRs, variants of the same cited publication are identified. Two attributes are used for a first similarity computation: **Last name** of the first author and **Source title**. Based on this data, CRExplorer determines the pair-wise similarity of variants of CRs. The program computes the Levenshtein similarity (as provided by the [SimMetrics library](https://github.com/Simmetrics/simmetrics)) of both attributes (see also [Wasi & Flaaen, 2015](references.md#Wasi2015)). The Levenshtein similarity of two strings *s*1 and *s*2 is defined as sim(*s*1, *s*2) = 1 - LD(*s*1, *s*2)/max(|*s*1|, |*s*2|). Here |*s*| denotes the length of a string s and LD (*s*1, *s*2) is the Levenshtein distance which is defined as follows: The Levenshtein distance between two strings *s*1 and *s*2 is the minimal number of single-character edit operations (i.e., insertion, deletion, or substitution) required to transform string *s*1 into *s*2. The Levenshtein distance is 0 for equal strings (no edit operations necessary) and equals max(|*s*1|, |*s*2|) for totally different strings (substitute the first min(|*s*1|, |*s*2|) characters and insert / delete the remaining characters). Therefore, for any two strings the Levenshtein similarity is between 0 to 1 where 0 corresponds to “totally different” and 1 to “identical”.

For two CRs *o*1 and *o*2, CRExplorer computes the Levenshtein similarity of the first authors’ last names as well as the similarity of the source titles. The two CRs *o*1 and *o*2 are considered as “matching” if the weighted average (ratio 2:1) of the two similarity values is equal to or greater than the threshold of 0.75 (this can be changed by the user, see below). The combination of multiple similarity values that are based on different attributes typically achieves a better match quality compared to a single similarity of the entire CR strings. First, it restricts the similarity computation to relevant (and available) attribute values. Second, the combination allows for an appropriate weighting of attributes independent of their actual string length. 

CRExplorer performs a clustering based on the matching results, i.e., the list of the matching CR pairs. Two CRs o1 and o2 are assigned the same cluster, if the pair (o1, o2) appears in the matching result or if there is a list of other CRs t1, ..., tn so that (o1,t1), (t1,t2), (t2,t3), ..., (tn-1,tn), and (tn, o2) are all among the matching pairs. Each cluster is uniquely identified by its ClusterID, i.e., all CRs of a cluster are marked with a corresponding ClusterID. The results of the similarity computation can be inspected using the column **ClusterID** in the table. The number of CRs in each cluster is provided by the column **ClusterSize**.


### <a name="cluster-equivalent-cited-references"/>Cluster equivalent Cited References Jaccard 

The Jaccard similarity is a set-based measure for comparing two strings. It has been often used in document similarity, clustering, and duplicate detection. For two sets A and B, the Jaccard similarity is defined as the ratio between the size of their intersection and the size of their union:

$$ 
J(A,B) = \frac{|A \cap B|}{|A \cup B|} 
$$ 


For string comparison, each string is decomposed into so-called *shingles*, i.e. overlapping groups of characters or words of length *k*. These shingles form the sets *A* and *B*. The parameter *k* controls the sensitivity of the comparison: smaller values are more tolerant towards small variations, whereas larger values are more restrictive. Character-based shingles are especially useful for short strings such as author names, while word-based shingles are better suited for longer text fields such as journal titles or reference titles. The Jaccard similarity returns values between 0 and 1, where 0 means that the two sets have no common elements and 1 means that they are identical. Jaccard similarity compares cited references by measuring how strongly their shingles overlap. It is simple and effective for strings of different lengths, but it does not consider how often an element occurs and it is sensitive to changes in word or character order.


Enter a number for the parameter *n* and the algorithm mode **(Clustering Parameter): Number for the Parameter n** determines how many letters or words are grouped when using the algorithm. **Mode for the Algorithm** determines if the algorithm analyzes *n* letters or *n* words. For example, **Char** and "2" split input strings into string fragments with 2 characters such as "test" into "te", "es" , and "st".


### Cluster equivalent Cited References Cosine

The cosine similarity is a vector-based measure that determines the similarity of two strings by measuring the angle between their vector representations in a feature space. In text comparison, strings are transformed into term-frequency vectors based on characters, character n-grams, words, or word *n*-grams. For two vectors $\vec{A}$ and $\vec{B}$, the cosine similarity is defined as:

$$ 
\cos(\theta) = \frac{\vec{A} \cdot \vec{B}}{\|\vec{A}\| \cdot \|\vec{B}\|} 
$$
 
The cosine similarity returns values between 0 and 1, where 1 indicates maximum similarity and 0 indicates no similarity. In contrast to set-based measures, cosine similarity takes the frequency of features into account. This makes it more robust when the same terms or character sequences occur multiple times. Cosine similarity is particularly robust to differences in string length and performs well when cited references share many common elements but differ in abbreviation. The similarity comparison can be performed in a character-based or word-based mode. Character-based *n*-grams are well suited for short fields such as author names or abbreviated journal titles, whereas word-based *n*-grams are more appropriate for longer texts.


Enter a number for the parameter *n* and the algorithm mode **(Clustering Parameter): Number for the Parameter n** determines how many letters or words are grouped when using the algorithm. **Mode for the Algorithm** determines if the algorithm analyzes *n* letters or *n* words. For example, **Char** and "2" split input strings into string fragments with 2 characters such as "test" into "te", "es" , and "st".

### Include RPY in Blocking Process

When the user selects this option, the automatic clustering of variants is restricted to variants within the same RPY, but not across RPYs. Thus, a reference to a first edition of a book and a reference to a later edition are not clustered by this routine. If the user wishes to combine editions of the same cited publication from different RPYs, they should disable **Include RPY in Blocking Process**.




### <a name="Merge clustered Cited References"/> Merge clustered Cited References

In the second step of eliminating variants of CRs (subsequent to the clustering of variants), the ClusterID is used for the aggregation of variants, i.e., the values of the corresponding lines in the table are summed up per cluster.

Although the clustering and merging procedure can be helpful in aggregating variants of the same CR, this procedure itself is prone to error. For example, if there are several CRs from the same authors and published in the same journal in one year, these CRs can be clustered, although they refer to different publications. This aggregation error affects mainly journal papers. Books may be also prone to error in the clustering and merging procedure. The source title of the same book may contain different page numbers (included by the citing authors), the same book may be available and cited in different languages, and the book titles may be differently abbreviated by the data providers.

For this reason, we strongly recommend that a user of the disambiguation procedure controls the results from the clustering and corrects wrong matches (especially for cited books). For the manual correction we have implemented some features in the program which support the user in post-processing the clustered results. Corresponding control buttons appear above the table, when the user starts the clustering process. The features can be used separately or in combination to change the cluster results before the merging of the CR variants is started. The effects of the features can be inspected by the values in **ClusterID** which has two components: The first value in the column (before the slash) shows the cluster numbers which result from the initial clustering process which was done automatically. The second value (after the slash) marks sub-clusters which change after using the features. Thus, the user should inspect the second value of the **ClusterID** to assess the results of the chosen post-processing.

The features implemented in the program are the following:

* **Slide control (Levenshtein similarity)**: For matching similar CRs, the matching algorithm is initially used as similarity function with a threshold of 0.75. However, the user can change this afterwards by using the slide control which accepts values between 0.5 (shown as 50) and 1 (shown as 100). If the slider is moved in the direction of 50, less similar CRs are matched; by moving the slider in the direction of 100, the matching process becomes increasingly restricted.
* **Volume, Page, and DOI**: The user can select Volume, Page, and DOI in order to differentiate the clusters further. These selections affect the whole dataset and not only CRs which are marked by the user. Note that the Levenshtein approach is not applied to Volume, Page, and DOI; a precise match is required for these attributes.
* **Treat Missing Values as Equal**: If unchecked (false) the previous behavior is restored: CRs with missing values of metadata (Volume, Page, or DOI) that are checked are considered to be different. If checked (true), CRs with missing values for metadata (Volume, Page, or DOI) that are checked are considered to be the same. The use of this option should result in better clustering of books and conference papers. However, some CRs (e.g., arXiv papers or other preprints) might be clustered too generously if no title is present in the CR.
* **Manual generation of sub-clusters**: The tool offers three different ways for the manual changing of sub-clusters. They are named as Same, Different, Extract, and Undo. Most of the problems with the automated clustering procedure occur with false positives: The algorithm matches CRs, although they should be kept separate. For the manual separation of clusters, the user can apply Different or Extract. Different assigns different sub-cluster IDs to those CRs which are marked by the user manually (using the mouse click). Extract puts the marked CRs in a separated sub-cluster. Same gives marked CRs the same sub-cluster-ID. Manual changes based on Same, Different, and Extract can be rolled back using the Undo button.

##  Help

### Online Manual

The manual of CRExplorer is opened.

### Info

Some information is given by the program.
