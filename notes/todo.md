
# CRTable

Die Klasse ``CRTable`` ist die abstrakte Klasse, welche alle Daten (CRs = Cited References und Pubs = Publications) hält.

Es gibt zwei Implementationen von ``CRTable``:
* ``CRTable_MM``, welche alle Daten im Hauptspeicher hält
* ``CRTable_DB``, welche die Daten in eine Datenbank (SQLite, PostgreSQL) auslagert und nur bei Bedarf lädt

``CRTable`` stellt Methoden bzgl. unterschiedlicher Aspekte zur Verfügung.
Diese werden durch Interfaces definiert. In der Implementation werden sie größtenteils in Subkomponenten ausgelagert.

## Berechnung der Indikatoren

Nach Veränderung des Datenbestands durch 
* Importieren
* Laden
* Löschen von CRs
müssen die Indikatoren neu berechnet werden.

# Wer ruft CRTable.updateData auf und warum?

CRE und CSV .. nach dem Laden --> in AfterLoad verschoben
Importer nach dem Importieren --> in AfterImport verschoben

DSL und MainController nach Setzen von NPCT_RANGE --> in setNPCT-Range verschoben

CRTable_DB und CRTable_MM nach dem Merge
Remover_DB und CRTable_MM nach dem Löschen von CR's



## Berechnung des Clusterings





## Interface ``Importer`` 

Steuert das Importieren von Publikationslisten inkl. CRs aus verschiedenen Formaten.
Wird von ``ImportFormat`` verwendet, der folgende Methoden aufruft

* ``onBeforeImport ()`` beim Starten des Import-Vorgangs
* ``addPub (PubType_MM pub)`` für jede zu importierende Publikation (die dann die citing references enthält)		
* ``onAfterImport ()`` am Ende des Import-Vorgangs

## Interface ``Loader``

Steuert das (Wieder-)Laden von CRExplorer-generierten Dateien, d.h. CRE- oder CSV-Dateien.
Wird von ``format.importer.CRE`` und ``format.importer.CSV`` verwendet, die folgende Methoden aufrufen

* ``onBeforeLoad()``
* ``onNewCR(CRType_MM cr)``
* ``onNewPub(PubType_MM pub, List<Integer> crIds)``
* ``onNewMatchPair(int crId1, int crId2, double sim, boolean isManual)``
* ``onAfterLoad()``

## Interface ``Remover``

Steuert das Löschen von CRs und/oder Pubs




* Remover (todo)
* Clustering
* Statistics 






# CRType

ist doch eigentlich unveränderbar bis auf wenige Properties (N_CR, Cluster)
reduzieren auf einen großen Konstruktor
