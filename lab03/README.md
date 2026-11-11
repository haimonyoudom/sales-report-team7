# Lab 03: Model-View-ViewModel (MVVM) and Background Jobs

## Overview
This lab refactors the Sales Report application presentation architecture from MVC/MVP to **Model-View-ViewModel (MVVM)** using JavaFX observable properties, data bindings, command abstractions, and asynchronous background worker jobs.

---

## File Catalog

| Path | Purpose | Author |
|---|---|---|
| `src/main/java/edu/itc/salesreport/ui/Command.java` | Action interface exposing an observable `executable` condition | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/DelegateCommand.java` | Command implementation delegating to a `Runnable` guarded by a condition | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/BackgroundJobs.java` | Port contract for running tasks off the UI thread and delivering callbacks on the UI thread | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/CallerThreadJobs.java` | Synchronous job runner implementation used to capture baseline UI thread freezes | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/FxBackgroundJobs.java` | Asynchronous job runner leveraging JavaFX `Task` and thread executor | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/FrameGapMonitor.java` | `AnimationTimer` measuring frame rendering intervals to detect UI stutters | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/SummarySource.java` | Port interface for loading branch summaries by month and branch | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/ReportViewModel.java` | Toolkit-free ViewModel managing UI state, reactive bindings, validation, and commands | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/ReportView.java` | JavaFX View declaring controls and establishing bidirectional bindings with the ViewModel | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/ReportViewerApp.java` | JavaFX composition root initializing asynchronous workers, ViewModel, and View | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/LoadBenchmarkApp.java` | Benchmark harness measuring frame gaps and UI responsiveness during heavy data loads | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/ManualJobs.java` | Deterministic test double holding jobs in a queue until triggered on the test thread | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/ReportViewModelTest.java` | Headless unit test suite (13 tests) for validation, commands, progress, errors, and filters | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/ViewModelImportsTest.java` | Architectural guard ensuring `ReportViewModel.java` uses only `javafx.base` | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/ReportViewModelValidationTest.java` | Headless tests verifying month/branch validation rules and error message retention | Team 7 |
| `lab03/viewmodel-design.md` | Architecture design document specifying properties, bindings, and command lifecycle | Team 7 |
| `lab03/measurements.md` | Experimental load benchmark results comparing `CallerThreadJobs` vs `FxBackgroundJobs` | Team 7 |
| `lab03/mvc-vs-mvvm.md` | Quantitative and architectural comparison between Lab-02 MVC and Lab-03 MVVM | Team 7 |
| `lab03/README.md` | Lab 03 summary, file catalog, reused inputs, and change log | Team 7 |

---

## Reused Inputs from Lab-02

* **Source Commit**: `eb2d974d4a79290411d0297439b25e8796a92f7e` (Tag / baseline: `lab-02-final`)
* **Reused Components**:
  * Domain Model: `edu.itc.salesreport.model` (`BranchSummary`, `ProductTotal`, `SaleTransaction`, `DailySummary`, `BranchCategoryKey`)
  * Data Ingestion: `edu.itc.salesreport.ingest` (`CsvMonthReader`, `CsvTransactionParser`)
  * Aggregation Engine: `edu.itc.salesreport.engine` (`SalesAggregator`, `DefaultSalesAggregator`)
  * Validation rules & helpers: `edu.itc.salesreport.ui.InputRules`, `Validation`
  * Test data fixtures: `TestData`, synthetic August 2026 transaction dataset

---

## Change Log

| Date | What Changed | Who | Model Package Changed? |
|---|---|---|---|
| 2026-11-05 | **Task 1**: Defined MVVM architecture and skeleton: `ReportViewModel`, `Command`, `DelegateCommand`, `BackgroundJobs`, `SummarySource`, and `viewmodel-design.md`. | haimonyoudom | **No** (untouched) |
| 2026-11-05 | **Task 2**: Implemented JavaFX `ReportView`, `CallerThreadJobs`, and application entry point `ReportViewerApp` with initial bindings. | haimonyoudom | **No** (untouched) |
| 2026-11-10 | **Task 3 & 4**: Completed input validation bindings, implemented `FxBackgroundJobs`, `FrameGapMonitor`, `LoadBenchmarkApp`, and recorded load benchmark results in `measurements.md`. | Song_Phengroth | **No** (untouched) |
| 2026-11-11 | **Task 5**: Added `ManualJobs` test double, 13 headless `ReportViewModelTest` tests, `ViewModelImportsTest` architecture guard, `mvc-vs-mvvm.md` comparison report, and `lab03/README.md`. | Team 7 | **No** (untouched) |

