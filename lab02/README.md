# Lab 02: Model-View-Presenter (MVP) and Refactoring

## Overview
This lab refactors the Sales Report application to separate presentation logic using Model-View-Presenter (MVP) with a passive view and headless testing.

## Files Created & Modified

| Path | Task | Author |
|---|---|---|
| `src/main/java/edu/itc/salesreport/ui/mvp/RowItem.java` | Toolkit-free row data transfer record | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/mvp/ReportScreen.java` | Passive view interface (no model dependency) | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/mvp/ReportPresenter.java` | Presenter handling inputs and formatting display strings | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/fx/FxReportScreen.java` | JavaFX passive view implementation of `ReportScreen` | Team 7 |
| `src/main/java/edu/itc/salesreport/ui/fx/ReportMvpApp.java` | JavaFX MVP Application launcher | Team 7 |
| `src/test/java/edu/itc/salesreport/ui/mvp/ReportPresenterTest.java` | Headless unit tests for Presenter with `FakeScreen` | Team 7 |
| `src/test/java/edu/itc/salesreport/UiIndependenceTest.java` | Verified that `ui/mvp` is toolkit-free | Team 7 |
| `lab02/mvc-vs-mvp.md` | Detailed comparison between MVC and MVP | Team 7 |
| `lab02/adr-002-presentation-pattern.md` | Architecture Decision Record selecting MVP | Team 7 |
| `lab02/README.md` | Lab 02 documentation, change log, and file inventory | Team 7 |

## Reused Lab-01 Inputs
* **Commit hash**: `aa03fa0d0fd80edf68a290f99acf35b86ff17d86`
* Reused components:
  * `SalesModel`, `SalesEvent`, `SalesModelListener`
  * `InputRules`, `Validation`
  * `CsvMonthReader`, `DefaultSalesAggregator`, `CsvTransactionParser`
  * `TestData`

## Change Log
* Implemented passive view `ReportScreen` and data record `RowItem`.
* Created `ReportPresenter` with `money()` formatting and state management.
* Built `FxReportScreen` and `ReportMvpApp` for JavaFX execution.
* Wrote `ReportPresenterTest` verifying formatted strings without JavaFX runtime.
* Updated `UiIndependenceTest` to enforce toolkit-free constraints on `ui.mvp`.
* Recorded architectural trade-offs in `lab02/mvc-vs-mvp.md` and `lab02/adr-002-presentation-pattern.md`.

