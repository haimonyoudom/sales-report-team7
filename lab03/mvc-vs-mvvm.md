# MVC vs MVVM Comparison

## Quantitative Metrics

| Metric | MVC (Lab-02, ReportController) | MVVM (Lab-03) | How measured |
|---|---|---|---|
| Lines of presentation code | 54 (controller) in a 133-line view (`ReportView`) and 72-line app (`ReportApp`) | 299 `ReportViewModel` + 130 view (`ReportView`) + 168 ports, commands, and jobs (`Command` 15, `DelegateCommand` 30, `BackgroundJobs` 35, `CallerThreadJobs` 29, `FxBackgroundJobs` 49, `SummarySource` 10) | `wc -l` |
| Presentation-logic line coverage without toolkit | 0 % (view formatting & state require JavaFX toolkit; controller only covers raw dispatch) | 99.1 % (111 of 112 lines covered in `ReportViewModel`, 126 of 127 lines including listener) | JaCoCo 0.8.15 (`jacoco.csv`) |
| `javafx.scene` imports in the logic class | 0 in `ReportController` (1 in `FatReportController`, 15 in `ReportView`) | 0 in `ReportViewModel` | `grep -c "^import javafx.scene"` |
| Control types the logic class references | 0 in `ReportController` (1 in `FatReportController`: `TextField`; 4 in `ReportView`: `ComboBox`, `Label`, `TableView`, `BarChart`) | 0 in `ReportViewModel` | Code review / architectural test |
| Test run time of presentation logic | ~0.028 s for 4 tests (`ReportControllerTest`) | ~0.087 s for 13 tests (`ReportViewModelTest`) | Surefire report |
| UI frozen during a load | yes (mean load 535 ms, worst frame gap 665 ms) | no (mean load 504 ms, worst frame gap 42 ms) | Task 4 `LoadBenchmarkApp` (`measurements.md`) |

## Architectural Reflection

Adopting the Model-View-ViewModel (MVVM) pattern dramatically improved the testability and maintainability of our presentation logic by decoupling state and user commands from the desktop UI toolkit. Because `ReportViewModel` depends strictly on `javafx.base` (observable properties and data bindings) rather than scene-graph controls, we achieved 99.1% presentation line coverage in under 0.1 seconds without initializing the JavaFX runtime. Furthermore, separating execution via the `BackgroundJobs` abstraction offloaded heavy aggregation to worker threads, eliminating UI freezing and reducing the worst-case frame gap from 665 ms down to 42 ms. Conversely, architectural complexity increased noticeably: the overall line count and class footprint grew with the addition of commands, ports, background job abstractions, and bidirectional binding code. Debugging reactive property chains and listener cascades is also more challenging than stepping through straightforward imperative MVC method invocations. Nevertheless, while classic MVC remains a pragmatic choice for small, synchronous utilities or fast disposable prototypes with simple UI flows, MVVM provides far superior architectural guarantees, test fidelity, and UI responsiveness for enterprise desktop applications.

