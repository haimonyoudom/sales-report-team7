# Sales Web

A separate Spring MVC + Thymeleaf presentation for the existing `sales-report` model. No files in the source project are changed by this web module.

## Build and run

From the repository root, install the existing model project first:

```bash
mvn install
cd sales-web
mvn verify
mvn dependency:tree -Dincludes=org.openjfx
java -jar target/sales-web-0.2.0-SNAPSHOT.jar --salesreport.data-dir=../data
```

Open `http://localhost:8080/report?month=2026-09&branch=PNH`. The data directory defaults to `data` relative to the process working directory; override it with `--salesreport.data-dir=...` when necessary.

The controller uses the shared `InputRules`, `CsvMonthReader`, `SalesAggregator`, and a fresh `SalesModel` per request. The only web-specific behavior is HTTP parameter handling and rendering the model data through Thymeleaf.
