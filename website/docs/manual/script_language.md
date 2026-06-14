# Script Language

CRExplorer can be operated by using the menus (see section 3) or by using a tailored script language (see the explanations and examples in [Haunschild et al., 2020](https://doi.org/10.1177/0165551519837175)). If a user wants to run a user-defined script name “myscript.crs”, it is necessary that the program is downloaded and started from the command line via the following command (Windows users might replace ``java`` by ``javaw``):

```
java -cp crexplorer.jar cre.Script myscript.crs (or java -cp crexplorer.jar cre.Script –db… in the database mode)
```

The script language allows the application of important program functions, which are exemplified as follows:

## Opening CRE file 

See [Section Options-File-Open](options.md#open)

```
openFile (file: ”folder/name/filename.cre”)
```

## Import of WoS, Scopus or Crossref files


See [Section Options-File-Import](options.md#import)

```
importFile (
    dir: “folder/name” or
    file: “folder/name/filename” or
    files: [“folder/name/f1”, “folder/name/f2”, ...], 
    type: “WOS” or “Scopus” or “CrossRef” or “CSVPUB”,	
    RPY: [minimum year, maximum year, w/o RPY? true or false],
    PY: [minimum year, maximum year, w/o PY? true or false],
    Sampling: “Random” or “None” or “Systematic” or “Cluster”,
    offset: 3,
    MAXCR: 1000
)
```

The offset parameter is only relevant for systematic sampling. The parameter “moves” the selected CRs by a certain values. For example, if offset = 0, CR #1, #5, #9, #13, … are selected. With offset = 1, it is CR #2, #6, #10, #14, ….

## Search for Crossref data 

See [Section Options-File-Import](options.md#import)

```
importSearch (
    type: “CrossRef”,
    doi: “DOI1” or doi: [“DOI1”,“DOI2”,...] or issn: “ISSN1”
)
```

The parameters for filtering (RPY, PY) and sampling (Sampling, Offset, MaxCR) can also be used for ``importSearch``.

## Save a CRE file 

See [Section Options-File-Save](options.md#save)

```
saveFile (
    file: “folder/name/filename.cre”,
	RPY: [minimum year, maximum year]
)
```

## Export data in different File Formats 

See [Section Options-File-Export](options.md#export)

```
exportFile (
    file: “folder/name/filename”, 
    type: “WOS” or “SCOPUS” or “CSV_CR” or “CSV_PUB” or
        “CSV_CR_PUB” or “CSV_GRAPH”
)
```

## Remove CRs by RPY or by N_CR 

See Sections [Options-Edit-Remove selected Cited References w/o Year](options.md#remove-selected-cited-references-wo-year), 
 [Options-Edit-Remove by Reference Publication Year](options.md#remove-by-reference-publication-year), and [Options-Edit-Remove by Number of Cited References](options.md#remove-by-number-of-cited-references)

``` 
removeCR (
	N_CR: [minimum value, maximum value],
	RPY: [minimum value, maximum value]
)
```

## Retain Citing Publications within PY


See [Section Options-Edit-Retain Publications within Citing Publication Year](options.md#retain-publications-within-citing-publication-year)

```
retainPub (
	PY: [minimum value, maximum value]
)
```


## Cluster equivalent CRs 

See [Section Options-Disambiguation-Cluster equivalent Cited References](options.md#cluster-equivalent-cited-references)

```
cluster (
threshold: [value between 0.5 and 1],
volume: true or false,
page: true or false,
DOI: true or false,
MISSING_IS_EQUAL: true or false
)
```

## Merge clustered cited references 

See [Section Options-Disambiguation-Merge clustered Cited References](options.md#merge-clustered-cited-references)

``` 
merge ()
```


## Settings

```
set (
  n_pct_range: 1, 
  median_range: 2)
```

## Loops

Users can expand the capabilities of CRExplorer by writing extensions ([Haunschild et al., 2020](https://doi.org/10.1177/0165551519837175)). One CRExplorer extension is available at [https://github.com/andreas-thor/cre/blob/master/crs/packages/Loop.crs](). This extension simplifies loop programming in the CRExplorer script language by introducing the forEach und forEachUnion operators. The number of cycles is provided as the value of count or as range (from, to). The functions differ in their behavior after each loop cycle: forEach performs no further action whereas forEachUnion merges the CR data of each cycle to a final CR dataset.

```
forEach (
  count: 10 or 
  from: 1, to: 10) {

     <script>
}

forEachUnion (
  count: 10 or 
  from: 1, to: 10) {

     <script>
}
```

If the ``count`` parameter is provided, the script is executed ``count`` times. If the from and to parameters are provided, the script is executed (``to-from+1``) times and the script can use the internal variable index that contains the current loop number (``from, from+1, ..., to``).

The loop operators are especially useful for sampling the same publication set multiple times and merging the results. An application of the script language with the three different sampling techniques for the publication set of climate change research (more than 200,000 publications and approximately 11 million CRs) has been performed by ([https://doi.org/10.1177/0165551519837175](references.md#Haunschild2020)).
