# Lab 03: Model-View-ViewModel (MVVM) Design Pattern

## Team 7
* **Members**: Song Phengroth, Haimon Youdom, Suon Caro, Theng Vanheng
* **Repository**: `sales-report-team7`

---

## Overview
This lab refactors the Angkor Mart sales report application from MVC/MVP to the **Model-View-ViewModel (MVVM)** design pattern using JavaFX observable properties, data bindings, command abstractions, and asynchronous background worker jobs, achieving high testability, asynchronous background processing, and complete multi-view reuse without UI freezes.

---

## Challenge: A Second View on the Same ViewModel (Extra Credit)

### 1. Multi-View Architecture
* Created `DashboardView` and `DashboardWindow` to provide an always-on-top compact executive dashboard displaying the current month, chain revenue, count of loaded branches, loading spinner, and a Refresh button.
* Both `ReportView` (main viewer) and `DashboardView` (dashboard) bind directly to the **same** `ReportViewModel` instance opened by `DashboardWindow.open(vm)` in `ReportViewerApp`.
* State synchronization is entirely reactive:
  - Changing the month or filter in either view automatically updates both windows.
  - Clicking **Refresh** in the dashboard immediately reloads data and populates the table in the main viewer.
  - Buttons in both windows are disabled while a background load is in progress.

### 2. Proof of ViewModel Reuse
The ViewModel was reused **100% unchanged** for the challenge. As verified:
```bash
git diff lab-03-task5 -- src/main/java/edu/itc/salesreport/ui/ReportViewModel.java
```
Produces **no output**, proving that no modifications to the ViewModel were necessary to support a secondary view.

### 3. Automated Two-Listener Test
Added `twoViewsListenersBothReceiveTotalRevenueAfterOneLoad()` in `ReportViewModelTest` and `ReportViewModelValidationTest`, simulating two distinct view listeners attached to `totalRevenueTextProperty()` concurrently receiving `"USD 600.00"` after a single load.

### 4. Cross-Platform Mapping
Documented in `lab03/platform-mapping.md`:
* Comprehensive mapping of each `ReportViewModel` member to **Android Jetpack** (`ViewModel`, `StateFlow`, `viewModelScope`) and **WPF** (`INotifyPropertyChanged`, `ObservableCollection`, `ICommand`, `async/await`).
* Analysis of platform differences that do not carry over (unidirectional data flow vs. two-way binding, Activity recreation across rotation, desktop multi-window vs. mobile single-destination navigation, and structured coroutine scopes).

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
| `src/main/java/edu/itc/salesreport/ui/ReportViewerApp.java` | Composition root launching both viewer and executive dashboard | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/DashboardView.java` | Compact dashboard view bound to the shared ViewModel | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/DashboardWindow.java` | Stage opener wiring the second window to the ViewModel | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/LoadBenchmarkApp.java` | Benchmark harness measuring frame gaps and UI responsiveness during heavy data loads | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/ManualJobs.java` | Deterministic test double holding jobs in a queue until triggered on the test thread | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/ReportViewModelTest.java` | Headless unit test suite (14 tests) for validation, commands, progress, errors, filters, and two-view listener sync | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/ViewModelImportsTest.java` | Architectural guard ensuring `ReportViewModel.java` uses only `javafx.base` | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/ReportViewModelValidationTest.java` | Headless tests verifying month/branch validation rules, error message retention, and challenge two-listener sync | Team 7 |
| `lab03/viewmodel-design.md` | Architecture design document specifying properties, bindings, and command lifecycle | Team 7 |
| `lab03/measurements.md` | Experimental load benchmark results comparing `CallerThreadJobs` vs `FxBackgroundJobs` | Team 7 |
| `lab03/platform-mapping.md` | Android Jetpack and WPF cross-platform mapping table & analysis | Team 7 |
| `lab03/mvc-vs-mvvm.md` | Quantitative and architectural comparison between Lab-02 MVC and Lab-03 MVVM | Team 7 |
| `lab03/screenshots/viewer.png` | Main viewer window screenshot | Team 7 |
| `lab03/screenshots/dashboard.png` | Side-by-side screenshot showing both synchronized windows | Team 7 |
| `lab03/README.md` | Lab 03 summary, challenge report, file catalog, reused inputs, and change log | Team 7 |

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
| 2026-11-11 | **Task 5**: Added `ManualJobs` test double, headless `ReportViewModelTest` tests, `ViewModelImportsTest` architecture guard, `mvc-vs-mvvm.md` comparison report, and `lab03/README.md`. | Team 7 | **No** (untouched) |
| 2026-11-11 | **Challenge**: Created `DashboardView`, `DashboardWindow`, wired dual-window launch in `ReportViewerApp`, added two-listener test, wrote `platform-mapping.md`, and captured screenshots. | Maestroo, Song_Phengroth, haimonyoudom | **No** (untouched) |

---

## Verification & Execution

### Run Tests and Coverage:
```bash
mvn clean verify
```
All tests pass cleanly without requiring a display or GUI initialization.

### Launch Application:
```bash
mvn javafx:run
```
Opens both the main sales report viewer and the executive dashboard concurrently.
