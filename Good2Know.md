

## PostgreSQL Datenbank bereitstellen



``docker run --name CREPostgres -p 5455:5432 -e POSTGRES_PASSWORD=cre -d postgres:15.2``

``docker rm CREPostgres``


## Eine neue Version deployen

- Tag-Name in [Workflow File](.github/workflows/gradle_build.yml) setzen
    - bei "Delete previous release"
    - bei "Create release"
 

- Build wird aktiviert, wenn push in Repo und commit message beinhalt "[build]"

- bei dev: vorher in GitHub tags/releases löschen


## Gradle commands

- Run: 
- Run specific tests: 
    - ``./gradlew test --tests cre.FileFormats``
    - ``./gradlew test --tests cre.StorageEngineShort``




## Log



### 2022-07-18

- Wegen [Bug in v17 JavaFX ](https://bugs.openjdk.org/browse/JDK-8276553?attachmentSortBy=dateTime) downgrade zu JavaFX v16

### 2022-07-17

Make gradlew executable for GitHub Actions

``git update-index --chmod=+x gradlew``

### 2022-07-01

Sample App installed based on https://openjfx.io/openjfx-docs/#gradle

* JDK 17, JavaFX
* Cross-Plattform Jar via Gradle 
    * runtimeOnly "org.openjfx:javafx ... in [app/build.gradle](app/build.gradle)



## Bugs / ToDO



### DONE

- dbstore: n_cr aktualisieren via SQL --> Performanz!
- pub auch eine id bei MM (um Äquivalenz bei Export der Stores sicherzustellen)
- matching mit großén Daten testen 
    - mit climate500t: MM in ~1min; DB > 10min!!!
    - --> besser (ca. 4:30min bei DB)

- sortieren bei db
    - db spaltenname ist nicht immer cr_[name] ... vielleicht in Column angeben?

- clustering db und dann vol anklicken --> führt zu Eception
    - klick in Diagramm .. getItems().stream() geht nicht ... abstrahieren :-)
