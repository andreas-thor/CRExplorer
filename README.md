[![Java CI with Gradle](https://github.com/andreas-thor/cre3/actions/workflows/gradle.yml/badge.svg)](https://github.com/andreas-thor/cre3/actions/workflows/gradle.yml)



## Log

### 2022-07-01

Sample App installed based on https://openjfx.io/openjfx-docs/#gradle

* JDK 17, JavaFX
* Cross-Plattform Jar via Gradle 
    * runtimeOnly "org.openjfx:javafx ... in [app/build.gradle](app/build.gradle)


### 2022-07-17

Make gradlew executable for GitHub Actions

``git update-index --chmod=+x gradlew``