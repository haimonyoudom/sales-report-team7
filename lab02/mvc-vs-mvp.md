# MVC vs MVP Comparison

| Criterion | MVC (ReportView + ReportController) | MVP (FxReportScreen + ReportPresenter) |
|---|---|---|
| Lines of code (view / controller-presenter) | 133 lines (`ReportView`) / 54 lines (`ReportController`) | 120 lines (`FxReportScreen`) / 81 lines (`ReportPresenter`) (+ 12 lines `ReportScreen`, 12 lines `RowItem`) |
| Tests that run without JavaFX | 4 tests (`ReportControllerTest`) | 6 tests (`ReportPresenterTest` [2 tests] + headless validation; formatting and all displayed text testable) |
| Where money and status text are formatted | In JavaFX View (`ReportView.modelChanged`, lines 111-120) | In presenter (`ReportPresenter.money`, `ReportPresenter.toRow`, `ReportPresenter.modelChanged`) |
| Files changed to add a "Discount" column | 1 file: `ReportView.java` (add column extractor from `SaleTransaction::discount`) | 3 files: `RowItem.java` (record field), `ReportPresenter.java` (format and mapping), `FxReportScreen.java` (table column) |
| Does the view hold a model reference? | Yes (`ReportView` holds `SalesModel`, queries `model.summary()` & `model.visibleTransactions()`) | No (Passive view; depends only on primitive/DTO `RowItem`, `String`, `Map`, `BigDecimal`) |
| Effort for a second screen (web, Swing) | High: Business display logic, formatting, and status strings must be rewritten in the new UI view | Low: Presenter and `RowItem` are 100% toolkit-free and reusable; only a thin implementation of `ReportScreen` is needed |

