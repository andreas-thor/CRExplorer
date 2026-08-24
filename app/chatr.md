# Lies zuerst die AGENTS.md im Projektstamm. Führe eine volls…

> Lies zuerst die AGENTS.md im Projektstamm.
>
> Führe eine vollständige Bug-Analyse dieses Java-Projekts durch.
>
> Vorgehen:
>
> 1. Prüfe `git status`, aber verändere keine Dateien.
> 2. Ermittle Projektstruktur, Java-Version, Frameworks und Build-System.
> 3. Lies die Build-Konfiguration und finde die korrekten Test- und Prüfkommandos.
> 4. Führe Build, Tests und vorhandene statische Prüfungen aus.
> 5. Analysiere den produktiven Java-Code systematisch auf echte Fehler.
> 6. Berücksichtige auch Konfiguration, Persistenz, Nebenläufigkeit, Ressourcenverwaltung, Security und Fehlerbehandlung.
> 7. Ordne jeden Fund nach Schweregrad und Fehlertyp gemäß AGENTS.md ein.
> 8. Gib für jeden Fund Datei, Zeilenbereich, Auslöseszenario und Begründung an.
> 9. Trenne bestätigte Bugs von möglichen Risiken und Wartbarkeitshinweisen.
> 10. Verändere keinen Code, erstelle keinen Commit und pushe nichts.
>
> Untersuche zunächst das gesamte Projekt auf hoher Ebene. Vertiefe danach die Bereiche mit dem höchsten Risiko.
>
> Erstelle am Ende ausschließlich den strukturierten Bug-Bericht aus AGENTS.md.

<details><summary>1 previous message</summary>

> Ich beginne wie vorgegeben ausschließlich mit der `AGENTS.md` im Projektstamm. Danach halte ich mich an deren Berichtsschema und führe die Analyse vollständig read-only durch.
</details>

Im angegebenen Projektstamm `C:\Dev\CRExplorer` existiert keine Datei mit dem exakten Namen `AGENTS.md`. Ich prüfe nun nur die Verzeichniseinträge im Stamm und suche anschließend nach gleichnamigen Dateien, ohne etwas zu verändern.

> Lies zuerst die AGENTS.md im verzeichnis app.
>
> Führe eine vollständige Bug-Analyse dieses Java-Projekts durch.
>
> Vorgehen:
>
> 1. Prüfe `git status`, aber verändere keine Dateien.
> 2. Ermittle Projektstruktur, Java-Version, Frameworks und Build-System.
> 3. Lies die Build-Konfiguration und finde die korrekten Test- und Prüfkommandos.
> 4. Führe Build, Tests und vorhandene statische Prüfungen aus.
> 5. Analysiere den produktiven Java-Code systematisch auf echte Fehler.
> 6. Berücksichtige auch Konfiguration, Persistenz, Nebenläufigkeit, Ressourcenverwaltung, Security und Fehlerbehandlung.
> 7. Ordne jeden Fund nach Schweregrad und Fehlertyp gemäß AGENTS.md ein.
> 8. Gib für jeden Fund Datei, Zeilenbereich, Auslöseszenario und Begründung an.
> 9. Trenne bestätigte Bugs von möglichen Risiken und Wartbarkeitshinweisen.
> 10. Verändere keinen Code, erstelle keinen Commit und pushe nichts.
>
> Untersuche zunächst das gesamte Projekt auf hoher Ebene. Vertiefe danach die Bereiche mit dem höchsten Risiko.
>
> Erstelle am Ende ausschließlich den strukturierten Bug-Bericht aus AGENTS.md.

<details><summary>28 previous messages</summary>

> Verstanden — ich verwende `app/AGENTS.md` als verbindliche Prüfanweisung. Die Untersuchung bleibt vollständig read-only; am Ende liefere ich ausschließlich das dort definierte Berichtsformat.
>
> <details><summary>Ran a command</summary>
>
> - Ran `Get-Content -Raw -LiteralPath app\AGENTS.md`
>
> </details>
>
> Die Vorgaben verlangen vor jedem Eingriff `git status`; da Builds generierte Dateien erzeugen können, prüfe ich zunächst Status, Struktur und Build-Dateien rein lesend. Erst anhand der Konfiguration entscheide ich, welche Prüfkommandos sicher und projektkonform sind.
>
> <details><summary>Ran 3 commands</summary>
>
> - Ran `rg --files app`
> - Ran `Get-ChildItem -Force | Select-Object Mode,Length,LastWriteTime,Name; Get-ChildItem -Force -LiteralPath app | Select-Object Mode,Length,LastWriteTime,Name`
> - Ran `git status --short --branch`
>
> </details>
>
> `git status` wurde durch Git wegen abweichender Verzeichnis-Eigentümerschaft blockiert. Ich umgehe das ohne Konfigurationsänderung nur für einzelne Git-Aufrufe mit `-c safe.directory=...`. Parallel lese ich Gradle-Konfiguration, README und CI-Workflows, um Java-Version, Abhängigkeiten und die vorgesehenen Prüfungen exakt zu bestimmen.
>
> <details><summary>Ran 4 commands</summary>
>
> - Ran `Get-Content -Raw -LiteralPath settings.gradle; Get-Content -Raw -LiteralPath gradle.properties; Get-Content -Raw -LiteralPath app\build.gradle`
> - Ran `git -c safe.directory=C:/Dev/CRExplorer status --short --branch`
> - Ran `Get-ChildItem -File -Recurse -LiteralPath gradle | Select-Object FullName,Length; Get-Content -Raw -LiteralPath gradle\wrapper\gradle-wrapper.properties`
> - Ran `Get-Content -Raw -LiteralPath README.md; Get-ChildItem -File -Recurse -LiteralPath .github | ForEach-Object { $_.FullName; Get-Content -Raw -LiteralPath $_.FullName }`
>
> </details>
>
> Der Arbeitsbaum enthält bereits zwei Nutzeränderungen (`version.txt` modifiziert, `app/AGENTS.md` unversioniert), die ich nicht anfasse. Außerdem schreibt `compileJava` bei jedem Build absichtlich einen neuen Zeitstempel in die getrackte `version.txt`; daher werde ich Build und Tests nicht im Arbeitsbaum ausführen, sondern – sofern möglich – in einer isolierten temporären Kopie. Zunächst kartiere ich Codeumfang und Risikostellen vollständig lesend.
>
> <details><summary>Ran 4 commands</summary>
>
> - Ran `Get-Content -Raw -LiteralPath app\src\main\resources\application.properties; Get-Content -Raw -LiteralPath app\src\main\resources\Main.fxml`
> - Ran `$files = rg --files app/src/main/java -g '*.java'; $files | ForEach-Object { $n=(Get-Content -LiteralPath $_).Count; [PSCustomObject]@{Lines=$n;File=$_} } | Sort-Object Lines -Descending | Format-Table -AutoSize; ($files | Measure-Object).Count`
> - Ran `rg -n --glob '*.java' "(DriverManager|getConnection|Statement|PreparedStatement|ResultSet|execute(Query|Update)?|commit\(|rollback\(|setAutoCommit|close\(|Files\.|File(Input|Output)Stream|Buffered(Reader|Writer)|new Thread|Executor|synchronized|volatile|Platform\.runLater|catch \(|throws )" app/src/main/java`
> - Ran `rg -n --glob '*.java' "(Runtime\.getRuntime|ProcessBuilder|ScriptEngine|GroovyShell|evaluate\(|Yaml|ObjectInputStream|readObject|password|token|secret|jdbc:|SELECT |INSERT |UPDATE |DELETE |CREATE TABLE|DROP TABLE)" app/src/main/java app/src/main/resources`
>
> </details>
>
> Die risikoreichsten Bereiche sind der globale In-Memory-Datenzustand, Import/Export, Hintergrund-Threads und die Skriptausführung. Ich lese diese Komponenten jetzt mit Zeilennummern sowie ihre Tests; danach gleiche ich konkrete Verdachtsstellen gegen Aufrufer und Datenmodelle ab.
>
> <details><summary>Ran 8 commands</summary>
>
> - Ran `$paths=@('app/src/main/java/cre/data/type/abs/CRType.java','app/src/main/java/cre/data/type/abs/PubType.java','app/src/main/java/cre/data/type/abs/CRTable.java'); foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `$p='app/src/main/java/cre/store/mm/CRTable_MM.java'; $i=0; Get-Content -LiteralPath $p | ForEach-Object { $i++; '{0,5}: {1}' -f $i,$_ }; $p='app/src/main/java/cre/store/mm/Clustering_MM.java'; $i=0; Get-Content -LiteralPath $p | ForEach-Object { $i++; '{0,5}: {1}' -f $i,$_ }`
> - Ran `$paths=@('app/src/main/java/cre/format/importer/ImportFormat.java','app/src/main/java/cre/format/importer/ImportReader.java','app/src/main/java/cre/format/importer/CRE.java','app/src/main/java/cre/format/exporter/ExportFormat.java','app/src/main/java/cre/format/exporter/CRE.java'); foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `$paths=rg --files app/src/test/java -g '*.java'; foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `rg -n "cre\.store\.db|TABLE_IMPL_TYPES|filterByCluster\(" app/src/main/java app/src/test/java; git -c safe.directory=C:/Dev/CRExplorer ls-files app/src/main/java | Measure-Object | Select-Object Count; git -c safe.directory=C:/Dev/CRExplorer ls-files app/src/main/java/cre/store/db`
> - Ran `$paths=@('app/src/main/java/cre/ui/MainController.java','app/src/main/java/cre/CitedReferencesExplorerFX.java','app/src/main/java/cre/ui/UISettings.java'); foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `$paths=@('app/src/main/java/cre/format/importer/CSV.java','app/src/main/java/cre/format/importer/CSV_Pub.java','app/src/main/java/cre/format/importer/Scopus.java','app/src/main/java/cre/format/importer/WoS_txt.java','app/src/main/java/cre/format/importer/Crossref.java'); foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `$paths=@('app/src/main/java/cre/data/type/abs/CRTable.java','app/src/main/java/cre/store/mm/PubType_MM.java','app/src/main/java/cre/store/mm/CRType_MM.java','app/src/main/java/cre/store/mm/Loader_MM.java','app/src/main/java/cre/store/mm/Filter_MM.java','app/src/main/java/cre/store/mm/Statistics_MM.java'); foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
>
> </details>
>
> Ein erster bestätigter Funktionsfehler ist bereits belegt: Die Cluster-Filtermethode ruft sich selbst rekursiv auf und endet beim UI-Menübefehl in `StackOverflowError`. Zusätzlich blendet `rg` den getrackten Datenbank-Code wegen Ignore-Regeln aus; ich nehme ihn ausdrücklich in die Vollanalyse auf, da Persistenz und Transaktionen dort besonders kritisch sind.
>
> <details><summary>Ran 3 commands</summary>
>
> - Ran `Get-Content -Raw -LiteralPath .gitignore; rg -n --no-ignore --glob '*.java' "(createStatement\(|prepareStatement\(|execute(Query|Update)?\(|setAutoCommit|commit\(|rollback\(|ResultSet|Connection|close\(|catch \()" app/src/main/java/cre/store/db`
> - Ran `$paths=git -c safe.directory=C:/Dev/CRExplorer ls-files 'app/src/main/java/cre/store/db/*.java'; foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `$paths=@('app/src/main/java/cre/format/exporter/CSV.java','app/src/main/java/cre/format/exporter/WoS.java','app/src/main/java/cre/format/exporter/Scopus.java','app/src/main/java/cre/ui/chart/CRChart_HighCharts.java','app/src/main/java/cre/scriptlang/ScriptExecutionEngine.java','app/src/main/java/cre/scriptlang/DSL.java');foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
>
> </details>
>
> Die Datenbank-Implementierung enthält dieselben rekursiven Delegationsfehler sogar in drei Methoden (`filterByCluster`, `setShowNull`, `showAll`). Für die dynamische Verifikation benötige ich nun eine temporäre Kopie außerhalb des Projekts, weil der Gradle-Build sonst die bereits lokal geänderte `version.txt` überschreiben würde. Im Projekt selbst bleibt alles unangetastet.
>
> <details><summary>Ran a command</summary>
>
> - Ran `$dest = Join-Path $env:TEMP ('CRExplorer-review-' + [guid]::NewGuid().ToString('N')); New-Item -ItemType Directory -Path $dest | Out-Null; Copy-Item -LiteralPath settings.gradle,gradle.properties,gradlew,gradlew.bat -Destination $dest; Copy-Item -Recurse -LiteralPath gradle -Destination $dest; New-Item -ItemType Directory -Path (Join-Path $dest 'app') | Out-Null; Copy-Item -Recurse -LiteralPath app\src -Destination (Join-Path $dest 'app'); Copy-Item -LiteralPath app\build.gradle -Destination (Join-Path $dest 'app'); Write-Output $dest`
>
> </details>
>
> Die temporäre Kopie läuft noch; der Arbeitsbaum bleibt dabei unverändert. Sobald sie abgeschlossen ist, führe ich dort zunächst `tasks`, danach `test`, die benannten Zusatztests, `check` und `build` aus und dokumentiere jede Abweichung getrennt.
>
> <details><summary>Ran a command</summary>
>
> - Ran `.\gradlew.bat --version; .\gradlew.bat tasks --all`
>
> </details>
>
> Die isolierte Toolchain ist Gradle 9.4.1 auf Java 21.0.10; es sind keine Checkstyle/SpotBugs/PMD-Aufgaben konfiguriert. Ich führe jetzt die Standardtests sowie beide projektspezifischen Regressionstest-Aufgaben einzeln aus, damit ein früher Fehler nicht die übrigen Ergebnisse verdeckt.
>
> <details><summary>Ran 2 commands</summary>
>
> - Ran `.\gradlew.bat test --stacktrace`
> - Ran `.\gradlew.bat testCRIndicators --stacktrace`
>
> </details>
>
> Der Standardlauf `test` ist erfolgreich, allerdings nur weil die Build-Konfiguration fast alle Testklassen explizit ausschließt. Die produktiven Quellen kompilieren mit Warnungen zu veralteten und ungeprüften APIs. Die beiden tatsächlich relevanten Zusatz-Testaufgaben laufen noch.
>
> <details><summary>Ran 3 commands</summary>
>
> - Ran `.\gradlew.bat testStorageEngines --stacktrace`
> - Ran `.\gradlew.bat check --stacktrace`
> - Ran `.\gradlew.bat build --stacktrace`
>
> </details>
>
> Der Storage-Engine-Test hat keinen Abschluss geliefert und offenbar einen JVM-Prozess mit geöffnetem SQL-Ressourcenstream hinterlassen; dadurch scheiterten nachfolgende `check`/`build` beim Ersetzen von `DB_Store.sql.yaml`. Das ist reproduzierbares Laufzeitverhalten, aber ich trenne noch Test-Harness-Effekt von Produktfehler. Parallel verifiziere ich nun die konkreten Datenkonsistenz-, Iterator- und Ressourcenbefunde an den vollständigen Methoden.
>
> <details><summary>Ran 8 commands</summary>
>
> - Ran `Get-Process java -ErrorAction SilentlyContinue | Select-Object Id,StartTime,CPU,Path; Get-ChildItem -File -Recurse -LiteralPath 'C:\Users\athor\AppData\Local\Temp\CRExplorer-review-08bd067706474c09a7f9d07614be82ab\app\build\test-results' -ErrorAction SilentlyContinue | Select-Object FullName,Length,LastWriteTime`
> - Ran `$paths=@('app/src/main/java/cre/data/type/abs/PubType.java','app/src/main/java/cre/data/type/abs/CRType.java','app/src/main/java/cre/store/mm/CRTable_MM.java');foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `$paths=@('app/src/main/java/cre/store/db/Importer_DB.java','app/src/main/java/cre/store/db/Loader_DB.java','app/src/main/java/cre/store/db/DB_Store.java','app/src/main/java/cre/store/db/Queries.java');foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `$paths=@('app/src/main/java/cre/format/importer/ImportReader.java','app/src/main/java/cre/format/importer/ImportFormat.java','app/src/main/java/cre/format/importer/CRE.java','app/src/main/java/cre/format/exporter/ExportFormat.java');foreach($p in $paths){"FILE $p";$i=0;Get-Content -LiteralPath $p|ForEach-Object{$i++;'{0,5}: {1}' -f $i,$_}}`
> - Ran `Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" | Where-Object { $_.CommandLine -like '*CRExplorer-review-08bd067706474c09a7f9d07614be82ab*' } | Select-Object ProcessId,CommandLine | Format-List; Get-ChildItem -File -Recurse -LiteralPath 'C:\Users\athor\AppData\Local\Temp\CRExplorer-review-08bd067706474c09a7f9d07614be82ab\app\build\test-results' -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName; Get-Content -Raw -LiteralPath $_.FullName }`
> - Ran `git -c safe.directory=C:/Dev/CRExplorer status --short --branch; git -c safe.directory=C:/Dev/CRExplorer diff -- app/src/main/resources/version.txt`
> - Ran `$paths=@('app/src/main/java/cre/store/db/CRTable_DB.java','app/src/main/java/cre/store/mm/CRTable_MM.java','app/src/main/java/cre/store/db/Filter_DB.java');foreach($p in $paths){"FILE $p";$lines=Get-Content -LiteralPath $p; $ranges=if($p -like '*CRTable_DB.java'){@(@(470,493))}elseif($p -like '*CRTable_MM.java'){@(@(485,505))}else{@(@(1,$lines.Count))};foreach($r in $ranges){for($i=$r[0];$i -le $r[1];$i++){'{0,5}: {1}' -f $i,$lines[$i-1]}}}`
> - Ran `rg -n "(equals\(|hashCode\(|compareTo\()" app/src/main/java/cre/data/type/abs/PubType.java app/src/main/java/cre/data/type/abs/CRType.java; $p='app/src/main/java/cre/data/type/abs/PubType.java'; $lines=Get-Content -LiteralPath $p; for($i=250;$i -le [Math]::Min(330,$lines.Count);$i++){'{0,5}: {1}' -f $i,$lines[$i-1]}`
>
> </details>
>
> Die Test-XML zeigt, dass der Storage-Test nicht wegen Produktlogik scheiterte: Mein Prüf-Orchestrator startete `check` bereits, während `testStorageEngines` noch lief; `processResources` ersetzte dabei gerade die vom Test gelesene YAML-Datei. Ich werte diesen Lauf deshalb nicht als Bug und wiederhole den Storage-Test jetzt sauber allein, bevor ich den Build erneut starte.
>
> <details><summary>Ran a command</summary>
>
> - Ran `.\gradlew.bat testStorageEngines --rerun-tasks --stacktrace`
>
> </details>
>
> Der isolierte Wiederholungslauf ist valide und schlägt reproduzierbar nach 1:47 min fehl: `StorageEngine.test_DB_vs_MM`, Vergleich von CRE-Exporten, Assertion in Zeile 173. Ich ermittle jetzt, welcher ZIP-Inhalt zwischen In-Memory- und DB-Backend abweicht und ob das fachliche Datenkorruption oder nur nichtdeterministische Reihenfolge ist.
>
> <details><summary>Ran 3 commands</summary>
>
> - Ran `Get-ChildItem -File -Recurse -LiteralPath 'C:\Users\athor\AppData\Local\Temp\CRExplorer-review-08bd067706474c09a7f9d07614be82ab' -Include 'out_*.cre' | Select-Object FullName,Length,LastWriteTime; $xml='C:\Users\athor\AppData\Local\Temp\CRExplorer-review-08bd067706474c09a7f9d07614be82ab\app\build\test-results\testStorageEngines\TEST-cre.StorageEngine.xml'; Select-String -LiteralPath $xml -Pattern 'Different|Assertion|CRE/|out_' -Context 2,2`
> - Ran `$xml='C:\Users\athor\AppData\Local\Temp\CRExplorer-review-08bd067706474c09a7f9d07614be82ab\app\build\test-results\testStorageEngines\TEST-cre.StorageEngine.xml'; Select-String -LiteralPath $xml -Pattern 'CRE/' | Select-Object -Last 10 | ForEach-Object {$_.Line}; $base='C:\Users\athor\AppData\Local\Temp\CRExplorer-review-08bd067706474c09a7f9d07614be82ab\app\build\resources\test\testdata\tmp'; Get-ChildItem -File -LiteralPath $base -ErrorAction SilentlyContinue | Where-Object {$_.Name -like 'out_*'} | Select-Object Name,Length,LastWriteTime`
> - Ran `Add-Type -AssemblyName System.IO.Compression.FileSystem; $base='C:\Users\athor\AppData\Local\Temp\CRExplorer-review-08bd067706474c09a7f9d07614be82ab\app\build\resources\test\testdata\tmp'; $texts=@{}; foreach($kind in 'MM','DB'){ $zip=[IO.Compression.ZipFile]::OpenRead((Join-Path $base "out_$kind.cre")); foreach($name in 'crdata.json','pubdata.json','crmatch.json'){ $e=$zip.GetEntry($name); $sr=[IO.StreamReader]::new($e.Open()); $texts["$kind/$name"]=$sr.ReadToEnd(); $sr.Dispose(); Write-Output "$kind/$name length=$($texts["$kind/$name"].Length)" }; $zip.Dispose() }; foreach($name in 'crdata.json','pubdata.json','crmatch.json'){ $a=$texts["MM/$name"];$b=$texts["DB/$name"];$min=[Math]::Min($a.Length,$b.Length);$idx=-1;for($i=0;$i -lt $min;$i++){if($a[$i]-ne$b[$i]){$idx=$i;break}};Write-Output "$name equal=$($a -ceq $b) firstDiff=$idx";if($idx-ge 0){$s=[Math]::Max(0,$idx-120);$n=[Math]::Min(300,$min-$s);Write-Output ('MM: '+$a.Substring($s,$n));Write-Output ('DB: '+$b.Substring($s,$n))}}`
>
> </details>
>
> Die nach dem Test vorhandenen MM/DB-Archive sind in allen drei JSON-Einträgen bytegleich; der Regressionstest-Fehler ist daher nicht als fachlicher Produktbug belegbar (wahrscheinlich Testzustand beziehungsweise ein früherer Iterationsfall, dessen Dateien später überschrieben wurden). Abschließend führe ich `check` und `build` nun ohne konkurrierenden Testprozess aus und verifiziere danach nochmals den Git-Status.
>
> <details><summary>Ran a command</summary>
>
> - Ran `.\gradlew.bat check build --rerun-tasks --stacktrace`
>
> </details>
</details>

| ID | Schwere | Typ | Datei | Kurzbeschreibung | Sicherheit |
| -- | ------- | --- | ----- | ---------------- | ---------- |
| BUG-001 | S1 | Logikfehler | `app/src/main/java/cre/store/mm/CRTable_MM.java` | Clusterfilter endet immer in einem `StackOverflowError` | bestätigt |
| BUG-002 | S1 | Logikfehler | `app/src/main/java/cre/store/db/CRTable_DB.java` | Drei Filteroperationen im DB-Modus enden in einem `StackOverflowError` | bestätigt |
| BUG-003 | S2 | Serialisierung oder Deserialisierung | `app/src/main/java/cre/format/importer/Crossref.java` | Ein unerwartetes Arrayelement bricht den gesamten restlichen Crossref-Import ab | bestätigt |
| BUG-004 | S1 | Persistenz oder Transaktion | `app/src/main/java/cre/store/db/Importer_DB.java` | SQL-Fehler werden verschluckt und teilweise Daten anschließend committed | hohe Wahrscheinlichkeit |

### BUG-001: Clusterfilter im In-Memory-Modus ruft sich selbst auf

* **Schweregrad:** S1
* **Typ:** Logikfehler
* **Sicherheit:** bestätigt
* **Datei:** `app/src/main/java/cre/store/mm/CRTable_MM.java`
* **Zeilen:** 493–495
* **Komponente:** `CRTable_MM.filterByCluster(List<Integer>)`
* **Beschreibung:** Die Methode delegiert nicht an das vorhandene `Filter_MM`-Objekt, sondern ruft mit unveränderten Argumenten erneut sich selbst auf.
* **Auslöseszenario:** Im In-Memory-Modus werden Cluster erzeugt und anschließend im Menü „Show Cited References of selected Cluster(s) only“ ausgewählt. `MainController` ruft dabei `crTable.filterByCluster(sel)` auf.
* **Erwartetes Verhalten:** `filter.filterByCluster(sel)` sollte die Sichtbarkeit der zitierten Referenzen entsprechend der ausgewählten Cluster aktualisieren.
* **Tatsächliches Verhalten:** Die Methode rekursiert ohne Abbruch, bis die JVM einen `StackOverflowError` auslöst. Die Filterfunktion ist nicht nutzbar; der UI-Thread kann abbrechen.
* **Auswirkung:** Eine reguläre UI-Funktion führt zuverlässig zu einem schweren Laufzeitfehler und kann die Anwendung in einen nicht mehr bedienbaren Zustand versetzen.
* **Begründung:** Der Methodenrumpf besteht aus `this.filterByCluster(sel)`. Der konkrete Aufruf erfolgt in `MainController.java`, Zeilen 752–758. Es gibt ein bereits initialisiertes `Filter_MM filter`, das hier nicht verwendet wird.
* **Empfohlene Behebung:** Den rekursiven Aufruf durch `this.filter.filterByCluster(sel)` ersetzen.
* **Empfohlener Test:** Eine kleine `CRTable_MM` mit mindestens zwei Clustern erzeugen, `filterByCluster(List.of(clusterId))` aufrufen und prüfen, dass die Methode normal zurückkehrt und nur Referenzen des gewählten Clusters sichtbar bleiben.

### BUG-002: Filteroperationen im DB-Modus rekursieren endlos

* **Schweregrad:** S1
* **Typ:** Logikfehler
* **Sicherheit:** bestätigt
* **Datei:** `app/src/main/java/cre/store/db/CRTable_DB.java`
* **Zeilen:** 479–492
* **Komponente:** `CRTable_DB.filterByCluster(List<Integer>)`, `setShowNull(boolean)`, `showAll()`
* **Beschreibung:** Alle drei Methoden rufen sich selbst statt des vorhandenen `Filter_DB`-Objekts auf.
* **Auslöseszenario:** Die Anwendung läuft mit `-db=...`. Der Benutzer filtert nach Clustern, blendet Referenzen ohne Jahr ein oder aus oder wählt „Show All Cited References“.
* **Erwartetes Verhalten:** Die Methoden sollten an `filter.filterByCluster(sel)`, `filter.setShowNull(showNull)` beziehungsweise `filter.showAll()` delegieren.
* **Tatsächliches Verhalten:** Jeder Aufruf erzeugt unbeschränkte Rekursion und endet in einem `StackOverflowError`.
* **Auswirkung:** Zentrale Anzeige- und Filterfunktionen sind im DB-Modus unbrauchbar. Da die Aufrufe typischerweise vom JavaFX-Thread kommen, kann die gesamte Oberfläche ausfallen.
* **Begründung:** Die Implementierungen enthalten unmittelbar `this.filterByCluster(sel)`, `this.setShowNull(showNull)` und `this.showAll()`. `CRTable_DB` besitzt in Zeile 43 ein initialisiertes `Filter_DB filter`; dessen korrespondierende Methoden sind in `Filter_DB.java` vorhanden.
* **Empfohlene Behebung:** Alle drei Methoden explizit an `this.filter` delegieren. Zusätzlich einen Delegationstest für jede Methode ergänzen.
* **Empfohlener Test:** DB-Modus mit einer SQLite-In-Memory-Datenbank initialisieren und jede der drei Methoden aufrufen. Prüfen, dass kein `StackOverflowError` entsteht und die `CR_VI`-Werte erwartungsgemäß geändert werden.

### BUG-003: Unerwartetes Crossref-Element beendet den Import vorzeitig

* **Schweregrad:** S2
* **Typ:** Serialisierung oder Deserialisierung
* **Sicherheit:** bestätigt
* **Datei:** `app/src/main/java/cre/format/importer/Crossref.java`
* **Zeilen:** 86–99
* **Komponente:** `Crossref.computeNextEntry()`
* **Beschreibung:** Ist ein Element im Crossref-Array kein JSON-Objekt, kehrt die Methode zurück, ohne `currentItemIndex` zu erhöhen. Zugleich bleibt `entry` auf `null`. Dadurch meldet der gemeinsame `ImportReader.hasNext()` das Ende des Iterators, obwohl hinter dem ungültigen Element weitere gültige Publikationen liegen können.
* **Auslöseszenario:** Eine `.crossref`-Datei enthält in `message.items` nach mindestens einer gültigen Publikation beispielsweise `null`, einen String oder ein Array und danach weitere gültige Objekte.
* **Erwartetes Verhalten:** Das ungültige Element sollte entweder mit einer aussagekräftigen Formatexception abgelehnt oder übersprungen werden; nachfolgende gültige Publikationen sollten weiterhin verarbeitet werden.
* **Tatsächliches Verhalten:** Der Import endet still am ersten Nicht-Objekt. Alle nachfolgenden Publikationen fehlen im Ergebnis, ohne dass der Benutzer eine Fehlermeldung erhält.
* **Auswirkung:** Unvollständige Imports und damit falsche Statistiken, Exporte und Analysen.
* **Begründung:** Zeile 88 setzt `entry = null`. Die Zeilen 95–99 prüfen den Typ und kehren ohne Indexinkrement zurück. `ImportReader.hasNext()` liefert gemäß Zeilen 39–42 bei `entry == null` anschließend `false`.
* **Empfohlene Behebung:** Den Index vor der Typprüfung erhöhen oder ungültige Elemente in einer Schleife überspringen. Alternativ eine `UnsupportedFileFormatException` mit Elementindex auslösen.
* **Empfohlener Test:** `items` mit `[gültiges Objekt, null, gültiges Objekt]` importieren und prüfen, dass entweder beide gültigen Objekte verarbeitet werden oder eine dokumentierte Formatexception entsteht.

### BUG-004: Datenbankimport kann partielle Daten dauerhaft speichern

* **Schweregrad:** S1
* **Typ:** Persistenz oder Transaktion, Ausnahmebehandlung, Datenkonsistenz
* **Sicherheit:** hohe Wahrscheinlichkeit
* **Datei:** `app/src/main/java/cre/store/db/Importer_DB.java`
* **Zeilen:** 43–53, 58–82, 91–117, 120–151
* **Komponente:** `Importer_DB.onBeforeImport()`, `addPub(PubType_MM)`, `onAfterImport()`
* **Beschreibung:** SQL-Ausnahmen während der Initialisierung und beim batchweisen Einfügen werden lediglich mit `printStackTrace()` protokolliert. Sie werden weder weitergereicht noch führt der Code ein `rollback()` aus. Spätere Schritte und Commits werden deshalb trotz fehlgeschlagener Teiloperationen fortgesetzt.
* **Auslöseszenario:** Während eines größeren PostgreSQL- oder SQLite-Imports scheitert ein Batch, etwa durch eine Constraint-Verletzung, eine volle Datenbank, einen Verbindungsfehler oder ungültige Daten. Frühere Batches wurden bereits ausgeführt oder ein späterer Abschluss-Commit ist noch möglich.
* **Erwartetes Verhalten:** Der gesamte Import sollte atomar fehlschlagen, zurückgerollt werden und dem Aufrufer eine Exception liefern.
* **Tatsächliches Verhalten:** Die Exception wird verschluckt. Der Import läuft weiter beziehungsweise erscheint dem Aufrufer erfolgreich; andere Batches und `onAfterImport()` können den teilweise geschriebenen Zustand committen.
* **Auswirkung:** Fehlende Publikationen, Referenzen oder Zuordnungssätze; inkonsistente Statistiken und schwer erkennbare dauerhafte Datenfehler.
* **Begründung:** Die Verbindung arbeitet seit `DB_Store.java`, Zeile 39, mit `autoCommit=false`. `Importer_DB` fängt `SQLException` an mehreren Schreibgrenzen ab, ruft aber nirgends `rollback()` auf und propagiert den Fehler nicht. Gleichzeitig finden Commits unter anderem in `onBeforeImport()` und `onAfterImport()` statt.
* **Empfohlene Behebung:** Den gesamten Import in einer klaren Transaktionsgrenze ausführen. Bei jeder `SQLException` sofort `rollback()` aufrufen und eine domänenspezifische Exception bis zum UI beziehungsweise Skriptaufrufer propagieren. Erst nach allen erfolgreichen Batches einmal committen.
* **Empfohlener Test:** Beim zweiten Batch gezielt eine Constraint-Verletzung injizieren. Danach prüfen, dass eine Exception an den Aufrufer gelangt und sämtliche Importtabellen exakt den Zustand vor dem Import besitzen.

## 1. Nicht bestätigte Risiken

* **S2 – Ressourcenleck:** `ExportFormat.save(...)`, Zeilen 48–57, erzeugt einen `FileOutputStream` ohne `try-with-resources`. Die konkreten Exporter schließen ihn nur auf dem normalen Erfolgspfad. Bei einer Exception während Serialisierung oder Ausgabe kann der Dateihandle offen und eine Teildatei zurückbleiben.
* **S2 – Ressourcenleck:** `cre.format.importer.CRE.load(...)`, Zeilen 63–100, verwaltet `ZipFile` und Entry-Streams manuell. Bei Parserfehlern vor Zeile 95 wird das Archiv nicht zuverlässig geschlossen.
* **S2 – Fehlerzustand/Nebenläufigkeit:** `CRTable_MM.updateData()`, Zeilen 191–238, setzt `duringUpdate=true`, aber nicht in einem `finally`-Block zurück. Eine Runtimeexception oder ein `OutOfMemoryError` kann den Zustand dauerhaft auf `true` belassen.
* **S2 – Nebenläufigkeit:** Die Singletons und globalen Datenstrukturen in `CRTable`, `CRTable_MM`, `CRTable_DB`, `StatusBar` und `UISettings` sind nicht threadsicher, während `MainController` mehrere rohe Hintergrundthreads startet. Ein reproduzierbarer Race-Condition-Datenfehler wurde in den ausgeführten Tests nicht belegt.
* **S2 – Fehlerbehandlung:** Der Konstruktor von `CRTable_DB`, Zeilen 104–135, verschluckt Verbindungs- und Initialisierungsfehler und veröffentlicht anschließend möglicherweise ein nur teilweise initialisiertes Singleton. Nachfolgende Methoden können dadurch mit wenig aussagekräftigen `NullPointerException` abbrechen.
* **S3 – Ressourcenverbrauch:** Datenbankabfragen in `DB_Store.selectCR`, `selectCRBlock` und `selectPub`, Zeilen 133–167, geben Streams über ResultSets zurück. Ein vorzeitig beendeter Stream schließt ResultSet und PreparedStatement nicht zuverlässig.
* **Security:** Die Groovy-Skriptausführung ist absichtlich nicht sandboxed. Skripte besitzen über `GroovyShell` vollständigen JVM- und Dateisystemzugriff. Das ist nur dann ein Sicherheitsproblem, wenn Skripte aus nicht vertrauenswürdigen Quellen ausgeführt werden.

## 2. Wartbarkeitshinweise

* `MainController.java` umfasst rund 1.100 Zeilen und vermischt UI-Ereignisse, Threadsteuerung, Dateioperationen und Geschäftslogik.
* Zahlreiche `catch`-Blöcke verwenden ausschließlich `printStackTrace()` oder ignorieren Exceptions vollständig. Dadurch sind Fehlerursachen schwer automatisiert auszuwerten.
* Der Compiler meldet veraltete APIs in `CRTableView.java` sowie ungeprüfte beziehungsweise unsichere generische Operationen.
* Die Build-Konfiguration enthält ältere Bibliotheken, unter anderem Commons IO 2.6, SnakeYAML 1.24, OpenCSV 3.9 und JFreeChart 1.0.19. Eine Abhängigkeits- oder CVE-Prüfung ist nicht konfiguriert.
* `compileJava` verändert bei jedem Build die getrackte Ressource `app/src/main/resources/version.txt`. Das erschwert reproduzierbare Builds und erzeugt lokale Änderungen selbst bei reinen Prüfkommandos.
* Die Datenbankquellen liegen unter einem durch `.gitignore` ausgeblendeten Pfadmuster. Allgemeine Quellcode-Suchwerkzeuge können diesen produktiven Bereich dadurch versehentlich überspringen.

## 3. Fehlgeschlagene Builds oder Tests

* Toolchain: Gradle Wrapper 9.4.1, Java 21.0.10; Ziel- und Quellversion Java 21. Frameworks: JavaFX 21, Groovy 3.0.25, JDBC mit SQLite/PostgreSQL, JSON-P, JFreeChart.
* `gradlew test`: erfolgreich, 1 Test ausgeführt. Die Klassen `FileFormats`, `IndicatorsExample`, `StorageEngine`, `StorageEngineShort` und `RunScript` werden vom Standard-Testtask ausdrücklich ausgeschlossen.
* `gradlew testCRIndicators`: erfolgreich, 2 Tests ausgeführt.
* `gradlew testStorageEngines --rerun-tasks`: fehlgeschlagen; 2 Tests, davon `StorageEngine.test_DB_vs_MM` fehlgeschlagen. Assertion in `StorageEngine.java:173` beim byteweisen Vergleich eines CRE-ZIP-Eintrags.
* Die nach dem Test erhaltenen `out_MM.cre`- und `out_DB.cre`-Dateien waren in `crdata.json`, `pubdata.json` und `crmatch.json` bytegleich. Da der Test mehrere Szenarien nacheinander ausführt und Dateien überschreibt, ließ sich der konkrete abweichende Zwischenstand nicht mehr feststellen. Der Testfehler wird daher nicht als bestätigter Produktbug eingestuft.
* `gradlew check build --rerun-tasks`: anschließend sequenziell erfolgreich; 10 Tasks ausgeführt.
* Es sind keine Checkstyle-, PMD-, SpotBugs- oder vergleichbaren statischen Prüfaufgaben konfiguriert.
* Alle Gradle-Aufgaben wurden in einer isolierten temporären Projektkopie ausgeführt, da `compileJava` andernfalls die bereits lokal modifizierte `version.txt` überschrieben hätte.
* Abschließender Arbeitsbaumstatus unverändert: `app/src/main/resources/version.txt` war bereits modifiziert; `app/AGENTS.md` war bereits unversioniert.

## 4. Nicht analysierte Bereiche

* Keine dynamischen JavaFX-End-to-End-Tests mit realer Benutzerinteraktion.
* Keine Live-Tests gegen eine externe PostgreSQL-Instanz.
* Keine echten Crossref-Netzwerkanfragen.
* Keine vollständige Sicherheitsbewertung der eingebetteten Drittanbieter-JavaScript-Dateien.
* Keine externe CVE-Datenbankabfrage.
* Die Website-, Dokumentations- und Docker-Infrastruktur wurde nur hinsichtlich ihrer Verbindung zum Java-Build betrachtet.

## 5. Empfohlene nächste Schritte

1. BUG-001 und BUG-002 zuerst beheben und direkte Regressionstests für sämtliche Filterdelegationen ergänzen.
2. BUG-004 durch eine atomare Importtransaktion mit Rollback und Fehlerpropagierung beheben.
3. BUG-003 mit einem Crossref-Test für gemischte beziehungsweise ungültige `items` absichern.
4. Den fehlgeschlagenen Storage-Engine-Test so ändern, dass jedes Szenario eigene Ausgabedateien behält und bei Abweichungen den betroffenen ZIP-Eintrag sowie den ersten Unterschied ausgibt.
5. Die zusätzlichen Regressionstests in `check` beziehungsweise CI aufnehmen; derzeit kann der Standardbuild trotz eines fehlgeschlagenen Storage-Engine-Tests erfolgreich sein.
6. Ressourcen mit `try-with-resources` absichern und Datenbankfehler nicht mehr verschlucken.
7. Mindestens eine statische Java-Prüfung sowie eine aktuelle Abhängigkeits- und Schwachstellenprüfung in Gradle integrieren.
