# A practically oriented short guide to use CRExplorer

The following hints and rules of thumb may be helpful for the use of CRExplorer.

## Establishing a publication set

The publication set to be analysed may comprise the publications of specific authors, journals, research fields or any other publication corpora of interest. Based on our experience hitherto, we recommend that the size of the relevant publication set should not be much less than 100 papers for a meaningful cited references (CRs) analysis. On the other hand, a typical research field normally comprises much larger publication sets. Here, the size of the publication set used for the CRs analysis is limited by more practical considerations; memory requirements increase with the number of CRs.

The sample under study does not need to comprise every relevant publication (e.g., related to a specific research field) and should not contain too many irrelevant papers at the same time. As a rule, some missing publications do not change the overall picture derived from CRExplorer analysis and the location of the peaks in the spectrogram (the graph produced by CRExplorer) will hardly be affected. On the other hand, the presence of too many irrelevant papers in the set increases the noise in the spectrogram and reduces the height and distinctness of the peaks.

After uploading the WoS records (or records from another source) into CRExplorer, one can analyse the complete range of reference publication years (RPYs), for example, in order to reconstruct the evolution of a research field as reflected by the references cited (by the members of the corresponding scientific community). Alternatively, one can focus on early references in order to investigate the origins and historical roots of a research field or one may wish to analyse recent RPYs (e.g., the last decade only) to reveal recently published highly-cited papers. The historical roots have been investigated in most of the studies published so far.

## Clustering of CRs

For a first overview, one is advised to sort the table of references by RPY in temporal order and concurrently by the CR counts. For example, this procedure helps to identify the earliest CRs and the number of references in more recent years. We suggest marking the following columns via the table settings: **Cited Reference, Reference Publication Year, Number of Cited References, Percent in Year, Percent over all Years, ClusterID**, and **ClusterSize**. The other columns can be ignored for the moment.

Note that the reference counts of all CRs within a specific RPY are mutually comparable since they can be considered as field-normalized given the common search string: all citing papers belong to the same set, i.e., papers from a single research field, topic or author. Thus, the CRs generally originate from the same citation culture.

In the next step, the equivalent references are clustered. This clean-up procedure (the so-called “disambiguation”) is needed because there are many incomplete and misspelled references among the CRs (in particular with regard to the source name, volume, and page numbers). The automatic clustering procedure of CRExplorer does not work absolutely correctly. For example, the program cannot differentiate between papers published by the same author in the same journal and year. In other words, different publications are identified by the program as variants of the same publication and are clustered. Using volume and page numbers for clustering reference variants routinely leads to satisfactory results in references sets mainly consisting of journal papers (see the options above the table of CRs in CRExplorer after activating the clustering function).



ID | Reference variant | N
--: | :-- | --:
12156 | Arrhenius S., 1896, PHILOS MAG 5, V41, P237 | 50
333 | Arrhenius S, 1896, PHILOS MAG 5, V5, P237 | 22
4998 | Arrhenius S., 1896, NORDISK TIDSKRIFT, V14, P121 | 4
5002 | ARRHENIUS S, 1896, BIHANG TILL KUNGL SV, V22, P1 | 3
2553 | ARRHENIUS S, 1896, PHILOS MAG, V41, P267 | 2
4999 | ARRHENIUS S, 1896, PHILOS MAG, V41, P274 | 2
5587 | Arrhenius S., 1896, LONDON EDINBURGH DUB, V41, P237 | 2
50801 | Arrhenius Svante, 1896, LONDON EDINBURGH DUB, V5, P237 | 2
61256 | ARRHENIUS S, 1896, ON INFLUENCE CARBONI | 1
70088 | ARRHENIUS S, 1996, PHIL MAG J SCI   APR, P237 | 1
80494 | ARRHENIUS S, 1896, INFLUENCE CARBONIC A | 1
/// caption 
Table 1. Reference variants of the Arrhenius (1896) paper with the number of occurrences.
///


Using volume and page numbers may be problematic for papers where volume numbers are missing or where page numbers within the range of pages are cited (rather than the starting pages). The use of the DOIs (in addition to volume and page numbers) to cluster reference variants usually results in detecting fewer variants and incomplete clustering, because DOIs are not available in many cases or are not properly assigned to CRs. Therefore, CRExplorer offers the possibility of cleaning-up the data manually. However, the manual cleaning-up of a dataset is only feasible in the case of relatively low numbers of CRs in the sample. This is usually the case for references which are published earlier than 1900 (and sometimes also for references published before 1950).
As an example for reference variants in the data, we show in Table 1 a list of CRs from our analysis of the discovery of the “greenhouse effect” presented in section 2. There are some reference variants of the [Arrhenius (1896)](https://doi.org/10.1080/14786449608620846) paper which can be clustered and merged. One reference variant (ID 333) cites the journal (*Philosophical Magazine*) with incorrect volume number, two others (ID 4998 and 5002) cite the corresponding Swedish papers, further two variants (ID 2553 and 4999) do not cite the starting page number, three variants (ID 5587, 50801 and 70088) cite journal title variants, and two CRs (ID 61256 and 80494) cite the title of the paper rather than the journal title.

## Manual Cleaning

If manual cleaning-up is applied, we suggest ordering the table items by the number of references per cluster (**ClusterSize**). In a first step, the items of larger clusters (which usually comprise the majority of CRs) should be checked and cleaned-up. If the dataset contains a manageable number of clusters and the user needs a (more or less) completely cleaned-up dataset, clusters with a smaller cluster size (or even with cluster size one) should also be investigated. The items with cluster size one can best be checked after ordering the references alphabetically (second ordering criterion in the program after cluster size). If the referenced authors in the dataset are cited more or less correctly, the variants of the CR to be checked appear one after another. In order to cope with a large number of CRs when using CRExplorer, a substantial cut may be necessary to master the flood of references extracted from the publication set. In the case of very large reference sets it is helpful to exclude all references with reference count one. These references are usually the majority of the CR items within a given publication set, but are only a small fraction of the total of CR counts. These references should be excluded before one checks for reference variants and inspects the spectrogram. However, the best strategy for manual cleaning depends on the publication set and the intended analysis.

## Inspection of the spectrogram

In many publication sets, the references to be analysed have been published over a long period with quite different publication and citation cultures: the average number of references per RPY increases substantially in the course of time. We may distinguish between the period of “little science” (prior to 1950) and the period of “big science” (since 1950) ([Marx, 2011](https://doi.org/10.1002/asi.21479 )). In particular, the reference counts before the RPY 1900 are comparatively low. Whereas the average (and maximum) reference count (**Number of Cited References**) increases with the passage of time, the share of reference counts accounting for a specific reference in a single year (**Percent in Year**) tends to decrease. This is the result of the continuously increasing number of papers and CRs, respectively, leading to increasingly less pronounced peaks in the spectrogram.

The spectrogram may not exhibit distinct peaks unless the range of RPYs is limited by excluding the more recent period (**Edit – Remove by Reference Publication Year**). If the analysis aims to detect influential early works, it is reasonable to remove all references with RPYs later than 1950. With regard to the inspection and interpretation of the spectrogram, it might also be helpful to select two (or more) consecutive RPY periods (e.g., 1800-1900 and 1901-1950) rather than one single period. Thus, one would analyse the references and reveal the reference peaks using two or more separate spectrograms. This simplifies the analysis and interpretation.

After the clustering process, both the spectrogram and the table with the CRs can be further adjusted and revised by selecting a minimum reference count (**Edit – Remove by Number of Cited References**). Removing the many references with reference count 1 (or in the case of large data sets: 2-3) makes the spectrogram more pronounced and the table of references better manageable. During the inspection of the spectrogram the question typically arises, which specific peaks should be considered as distinct reference peaks for further analysis and discussion. This decision is rather arbitrary and depends on the specific data set and the maximum number of top references to be discussed. A minimum reference count of 10, for example, has proved to be reasonable for investigating referenced papers published prior to 1900 (e.g., if the analysis aims to detect influential early works).

For the identification of the peaks and the corresponding top references, both the overall number of CRs (red curve in the spectrogram of the **JFreeChart**) and the (absolute) deviation from the median (blue curve of the **JFreeChart**) can be considered. Normally, both curves deliver the same amount of information and can be used alternatively or concurrently. There may be cases for which one or the other curve might be better suited.

If one would like to analyse the recent evolution of a research field and focus on the more recent decades of the RPY, the spectrogram is less informative. The peaks are less pronounced, because each reference, although highly cited, comprises an increasingly smaller share of the reference counts of a RPY. This kind of analysis can best be performed via the table of references ordered concurrently by the RPY (**Reference Publication Year**) and the reference counts (**Number of Cited References**) (with the most CRs at the top).

## The RPYS-CO approach

CRExplorer allows the restriction of the CRs to only those which are co-cited with at least one selected CR using the menu items **Edit – Retain Publications citing Selected Cited References**. This restriction takes advantage of the fact that concurrently cited (co-cited) papers are more or less closely related to each other. One can select the citation environment of a specific reference (or of two or even more references) in the form of all co-cited references and analyse these references applying RPYS-CO. The specific reference should be a prominent and seminal work which is used as a kind of marker or tracer reference for a specific topic in a field. We assume that papers which cite the selected reference(s) are potential candidates for citing also many other references relevant in a specific historical context.

For example, if we are interested to refine the analysis of the discovery of the “greenhouse effect” with regard to the earliest roots, we could use the seminal paper by [Arrhenius (1896)](https://doi.org/10.1080/14786449608620846), the most pronounced pre-1900 peak in Figure 1. Svante Arrhenius was the first scientist who calculated how changes in the levels of carbon dioxide in the atmosphere could alter the surface temperature through the greenhouse effect. He predicted that emissions of carbon dioxide from the burning of fossil fuels were large enough to cause global warming. His paper can be seen as a cornerstone in the evolution of climate change research.

By analysing the co-citations of [Arrhenius (1896)](https://doi.org/10.1080/14786449608620846) as a marker reference, we investigate the discovery of the greenhouse effect and the specific role of carbon dioxide. This research topic marks the historical roots and origins of the current climate change research. As a result of applying RPYS-CO to the history of climate change research in a recent study ([Marx et al., 2017](https://doi.org/10.1007/s11192-016-2177-x)) the decisive works of the French mathematician and physicist Joseph Fourier become more clearly visible. We get to know that his 1827 paper is a reproduction of his 1824 work, which is much more pronounced in the RPYS-CO spectrogram.

## Conclusions

In summary, the strategy for using CRExplorer strongly depends both on the size of the publication and reference set to be analysed and on the specific focus of the analysis (early or more recent works). The strategy has to be adapted to the specific goal of the analysis. CRExplorer can be applied for three main objectives: (1) the detection of the knowledge basis (i.e. the origins and historical roots) of research topics, (2) the investigation of influential works published more recently, and (3) the disambiguation of CRs data.
