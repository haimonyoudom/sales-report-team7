# Platform Mapping: ReportViewModel Architecture

This document maps each member of the JavaFX `ReportViewModel` to idiomatic constructs in **Android Jetpack** and **WPF (.NET)**, evaluating how the MVVM design pattern transitions to other modern GUI platforms.

---

## 1. Member Mapping Table

| ReportViewModel Member | JavaFX Type & Role | Android Jetpack Equivalent | WPF (.NET) Equivalent |
| :--- | :--- | :--- | :--- |
| **month** | `ObjectProperty<YearMonth>` (Two-way bound input state) | `MutableStateFlow<YearMonth?>` exposed as `StateFlow`, updated via UI event callbacks (unidirectional data flow) | `YearMonth`/`DateTime?` property implementing `INotifyPropertyChanged`, bound with `{Binding Month, Mode=TwoWay}` |
| **branch flags** (`branchSelected`) | `Map<String, BooleanProperty>` (Two-way bound toggle inputs) | `MutableStateFlow<Map<String, Boolean>>` (or `snapshotStateMapOf<String, Boolean>()`), modified via event handler | Individual properties or `ObservableCollection<BranchItemViewModel>` with `INotifyPropertyChanged` and `{Binding IsSelected, Mode=TwoWay}` |
| **summaries** | `ObservableList<BranchSummary>` (One-way bound collection output) | `StateFlow<List<BranchSummary>>` (immutable list) observed by Compose `LazyColumn` or `RecyclerView` with `ListAdapter` | `ObservableCollection<BranchSummary>` bound directly to `DataGrid.ItemsSource` |
| **loading** | `ReadOnlyBooleanProperty` (One-way bound state flag) | `StateFlow<Boolean>` exposed as read-only from `_loading` backing flow to control `CircularProgressIndicator` visibility | `bool` property raising `PropertyChanged`, bound to `ProgressBar.Visibility` via a `BooleanToVisibilityConverter` |
| **progress** | `ReadOnlyDoubleProperty` (One-way numeric fraction) | `StateFlow<Float>` (0.0f to 1.0f) bound to `LinearProgressIndicator(progress = { progress })` | `double` property raising `PropertyChanged`, bound directly to `ProgressBar.Value` |
| **validation** (`validationMessage`, `valid`, `canGenerate`) | `StringBinding` / `BooleanBinding` (Computed/derived reactive state) | Derived reactive state created using Kotlin Coroutines `combine(monthFlow, branchFlagsFlow) { ... }` or Compose `derivedStateOf { ... }` | Computed getter property raising `PropertyChanged` when dependents change, or implementing `INotifyDataErrorInfo` |
| **generate** (`generateCommand`) | `Command` (`execute()`, `executableProperty()`) | Public ViewModel function `fun generate()` invoked from UI event handler (`Button(onClick = { vm.generate() }, enabled = canGenerate)`) | `ICommand` (such as `RelayCommand` or `AsyncRelayCommand`) with `CanExecute(parameter)` bound to `Button.Command` |
| **cancel** (`cancelCommand`) | `Command` (Triggers cancellation and tracks executable flag) | Public ViewModel function `fun cancel()` calling `job?.cancel()`, or `ICommand` cancellation token triggering | `ICommand` bound to Cancel button, invoking `CancellationTokenSource.Cancel()` |
| **BackgroundJobs** | Interface port for asynchronous execution & progress reporting | Kotlin Coroutines via `viewModelScope.launch(Dispatchers.IO) { ... }` with injected `CoroutineDispatcher` for test isolation | `Task.Run()` / `async`/`await` abstraction with injected `IBackgroundJobService` or `IProgress<double>` |

---

## 2. What Would Not Carry Over

1. Android Jetpack Compose embraces unidirectional data flow (UDF) rather than the JavaFX/WPF two-way property binding mechanism, requiring explicit state hoisting where the UI emits events (`onValueChange`) to update ViewModel StateFlows rather than directly mutating two-way bound properties.
2. While JavaFX `Stage` instances do not destroy and recreate their views when resized or moved, Android Activities undergo configuration changes (such as device rotation) that completely destroy and recreate the View hierarchy while the Jetpack `ViewModel` survives retained by `ViewModelStore`.
3. Because the Jetpack ViewModel outlives the recreated UI components across screen rotation, any direct reference to UI views, contexts, or listeners within the ViewModel would leak the destroyed Activity.
4. In contrast to JavaFX's desktop multi-window model where two independent `Stage` windows concurrently bind to one persistent in-memory `ReportViewModel` instance, mobile Android interfaces typically represent screens as Fragments or full-screen destinations sharing a ViewModel scoped to an Activity or Navigation Graph.
5. Concurrency primitives also differ structurally: desktop JavaFX relies on background worker threads dispatching observable property notifications back to the JavaFX Application Thread, whereas Jetpack leverages structured concurrency via `viewModelScope` that automatically cancels running coroutines when the ViewModel is cleared.
