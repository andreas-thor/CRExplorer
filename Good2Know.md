

## Eine neue Version deployen

- Tag-Name in [Workflow File](.github/workflows/gradle_build.yml) setzen
    - bei "Delete previous release"
    - bei "Create release"


## Gradle commands

- Run: 
- Run specific tests: 
    - ``./gradlew test --tests cre.FileFormats``
    - ``./gradlew test --tests cre.StorageEngine``




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


