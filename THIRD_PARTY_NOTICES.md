# Third-party notices

The Apache License 2.0 in this repository applies only to CRExplorer material
for which the CRExplorer copyright holders are entitled to grant that license.
It does not relicense third-party software, publications, data, images, logos,
trademarks, or other separately licensed material.

## Bundled browser libraries

| Component | Location | License |
| --- | --- | --- |
| Highcharts JS 5.0.6 and Exporting module | `app/src/main/resources/highcharts/` and embedded copies in `CRChart.html` | Highsoft license; proprietary/source-available, not Apache-2.0 |
| jQuery 1.8.2 | `app/src/main/resources/highcharts/` and an embedded copy in `CRChart.html` | MIT |

Highcharts may be used and redistributed only to the extent permitted by the
applicable license obtained from Highsoft AS. Possession of this repository or
the Apache-2.0 license for CRExplorer does not grant a Highcharts license. See
<https://www.highcharts.com/license>.

## Runtime dependencies

CRExplorer uses the following direct runtime dependencies. Each component
remains under its own license, including any required notices and license text.

| Component | Declared/resolved version | License |
| --- | --- | --- |
| Apache Groovy | 3.0.25 | Apache-2.0 |
| Simmetrics | 4.1.1 | Apache-2.0 |
| Jakarta/Java API for JSON Processing (`javax.json`) | 1.1 | CDDL-1.1 or GPL-2.0-with-classpath-exception |
| Eclipse GlassFish JSON Processing implementation | 1.1 | CDDL-1.1 or GPL-2.0-with-classpath-exception |
| OpenCSV | 3.9 | Apache-2.0 |
| JFreeChart | 1.5.0 (selected by Gradle) | LGPL-2.1-or-later |
| JFreeChart-FX | 1.0.1 | LGPL-2.1-or-later |
| FXGraphics2D | 1.6 | LGPL-2.1-or-later |
| Apache Commons IO | 2.6 | Apache-2.0 |
| H2 Database Engine | 2.1.214 | MPL-2.0 or EPL-1.0 |
| PostgreSQL JDBC Driver | 42.6.0 | BSD-2-Clause |
| SnakeYAML | 1.24 | Apache-2.0 |
| Xerial SQLite JDBC | 3.50.3.0 | Apache-2.0 |
| OpenJFX | 21 | GPL-2.0-with-classpath-exception |

These dependencies bring transitive dependencies. The authoritative dependency
set for a build is produced by:

```text
./gradlew :app:dependencies --configuration runtimeClasspath
```

Binary distributions must retain the license and notice files of all direct
and transitive dependencies. In particular, the Gradle `jar` task builds an
uber-JAR by unpacking the runtime classpath. Release maintainers must verify
that `META-INF/LICENSE*`, `META-INF/NOTICE*`, service descriptors, and other
required attribution files have not been lost or overwritten before publishing
that JAR.

## Other excluded material

Unless a file carries an explicit open license or its rights have otherwise
been verified, the following are not licensed under Apache-2.0:

- publications and publisher-formatted files in `documentation/papers/`;
- presentations and office documents in `documentation/`;
- imported or example bibliographic datasets, including Scopus and Web of
  Science data;
- third-party screenshots, icons, logos, trademarks, and branding;
- the Gradle wrapper and other third-party binary artifacts.

Their inclusion is not a representation that any use beyond the applicable
third-party terms or statutory exceptions is permitted.
