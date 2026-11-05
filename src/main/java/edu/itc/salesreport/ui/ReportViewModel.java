package edu.itc.salesreport.ui;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;

/** Task 1 skeleton: state, commands and ports. javafx.base only. */
public final class ReportViewModel {

    public static final List<String> BRANCHES = List.of("PNH", "REP", "BTB");

    private final SummarySource source;
    private final BackgroundJobs jobs;
    private final Clock clock;

    // ---- input state: two-way bound by the view
    private final ObjectProperty<YearMonth> month = new SimpleObjectProperty<>(this, "month");
    private final Map<String, BooleanProperty> branchSelected = new LinkedHashMap<>();
    private final StringProperty productFilter = new SimpleStringProperty(this, "productFilter", "");

    // ---- output state: one-way bound by the view
    private final ObservableList<YearMonth> monthChoices = FXCollections.observableArrayList();
    private final ObservableList<BranchSummary> summaries = FXCollections.observableArrayList();
    private final ObservableList<ProductTotal> products = FXCollections.observableArrayList();
    private final FilteredList<ProductTotal> visibleProducts = new FilteredList<>(products);
    private final ReadOnlyBooleanWrapper loading = new ReadOnlyBooleanWrapper(this, "loading", false);
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", 0);
    private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper(this, "status", "Choose a month.");
    private final ReadOnlyStringWrapper loadError = new ReadOnlyStringWrapper(this, "loadError", "");

    // ---- computed state (Task 3 replaces the placeholders)
    private final StringBinding monthError;
    private final StringBinding branchError;
    private final StringBinding validationMessage;
    private final BooleanBinding canGenerate;
    private final ObjectBinding<BigDecimal> totalRevenue;
    private final StringBinding totalRevenueText;

    private final Command generateCommand;
    private final Command cancelCommand;
    private BackgroundJobs.JobHandle running;

    public ReportViewModel(SummarySource source, BackgroundJobs jobs,
            Clock clock) {
        this.source = Objects.requireNonNull(source);
        this.jobs = Objects.requireNonNull(jobs);
        this.clock = Objects.requireNonNull(clock);
        YearMonth now = YearMonth.now(clock);
        for (int i = 0; i < 12; i++) {
            monthChoices.add(now.minusMonths(i));
        }
        for (String b : BRANCHES) {
            branchSelected.put(b, new SimpleBooleanProperty(this, b, true));
        }
        // TODO Task 3: real rules (future month, empty branch selection)
        monthError = Bindings.createStringBinding(() -> "", month);
        branchError = Bindings.createStringBinding(() -> "");
        validationMessage = Bindings.createStringBinding(() -> "");
        canGenerate = month.isNotNull().and(loading.not());
        // TODO Task 2: sum of summaries' revenue, formatted "USD 1,234.50"
        // TODO Task 2: bind visibleProducts' predicate to productFilter
        totalRevenue = Bindings.createObjectBinding(
                () -> summaries.stream().map(BranchSummary::revenue)
                        .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add),
                summaries);
        totalRevenueText = Bindings.createStringBinding(
                () -> formatUsd(totalRevenue.get()), totalRevenue);
        visibleProducts.predicateProperty().bind(
                Bindings.createObjectBinding(() -> matches(productFilter.get()), productFilter));

        generateCommand = new DelegateCommand(this::generate, canGenerate);
        cancelCommand = new DelegateCommand(this::cancel,
                loading.getReadOnlyProperty());
    }

    public List<String> selectedBranches() {
        return branchSelected.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(Map.Entry::getKey)
                .toList();
    }

    private void generate() {
        YearMonth m = month.get();
        List<String> branches = selectedBranches();
        loading.set(true);
        progress.set(0);
        loadError.set("");
        status.set("Loading " + m + "...");
        BackgroundJobs.JobHandle handle = jobs.submit(
                sink -> loadAll(m, branches, sink),
                new BackgroundJobs.JobListener<List<BranchSummary>>() {
                    public void progress(double f) {
                        progress.set(f);
                    }

                    public void succeeded(List<BranchSummary> r) {
                        summaries.setAll(r);
                        products.setAll(mergeProducts(r));
                        status.set("Loaded " + r.size() + " branch(es) for " + m + ".");
                        loading.set(false);
                    }

                    public void failed(Throwable e) {
                        loadError.set("Could not load " + m + ": " + e.getMessage());
                        status.set("Load failed.");
                        loading.set(false);
                    }

                    public void cancelled() {
                        status.set("Cancelled.");
                        loading.set(false);
                    }
                });
        running = loading.get() ? handle : null;
        // TODO Task 4: progress, cancelled(), failed() -> loadError
    }

    private List<BranchSummary> loadAll(YearMonth m, List<String> branches,
            BackgroundJobs.ProgressSink sink) throws Exception {
        List<BranchSummary> result = new ArrayList<>();
        for (String b : branches) {
            result.add(source.load(m, b));
            sink.update(result.size(), branches.size());
        }
        return result;
    }

    private static List<ProductTotal> mergeProducts(List<BranchSummary> branches) {
        Map<String, ProductTotal> bySku = new LinkedHashMap<>();
        for (BranchSummary s : branches)
            for (ProductTotal p : s.topProducts())
                bySku.merge(p.sku(), p, (a, b) -> new ProductTotal(a.sku(), a.productName(),
                        a.revenue().add(b.revenue()), a.quantity() + b.quantity()));
        return bySku.values().stream()
                .sorted(Comparator.comparing(ProductTotal::revenue).reversed()
                        .thenComparing(ProductTotal::sku))
                .toList();
    }

    private static Predicate<ProductTotal> matches(String filter) {
        if (filter == null || filter.isBlank())
            return p -> true;
        String needle = filter.strip().toLowerCase(Locale.ROOT);
        return p -> p.productName().toLowerCase(Locale.ROOT).contains(needle)
                || p.sku().toLowerCase(Locale.ROOT).contains(needle);
    }

    private static String formatUsd(BigDecimal amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        return "USD " + nf.format(amount);
    }

    private void cancel() {
        if (running != null)
            running.cancel();
        // TODO Task 4: cancel the running job, if any
    }

    // ---- accessors: read-only types for everything the view must not set
    public ObjectProperty<YearMonth> monthProperty() {
        return month;
    }

    public BooleanProperty branchSelectedProperty(String branch) {
        return Objects.requireNonNull(branchSelected.get(branch), branch);
    }

    public StringProperty productFilterProperty() {
        return productFilter;
    }

    public ObservableList<YearMonth> monthChoices() {
        return FXCollections.unmodifiableObservableList(monthChoices);
    }

    public ObservableList<BranchSummary> summaries() {
        return FXCollections.unmodifiableObservableList(summaries);
    }

    public ObservableList<ProductTotal> visibleProducts() {
        return visibleProducts;
    }

    public ReadOnlyBooleanProperty loadingProperty() {
        return loading.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty statusProperty() {
        return status.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty loadErrorProperty() {
        return loadError.getReadOnlyProperty();
    }

    public StringExpression monthErrorProperty() {
        return monthError;
    }

    public StringExpression branchErrorProperty() {
        return branchError;
    }

    public StringExpression validationMessageProperty() {
        return validationMessage;
    }

    public ObservableBooleanValue canGenerateProperty() {
        return canGenerate;
    }

    public ObservableObjectValue<BigDecimal> totalRevenueProperty() {
        return totalRevenue;
    }

    public StringExpression totalRevenueTextProperty() {
        return totalRevenueText;
    }

    public Command generateCommand() {
        return generateCommand;
    }

    public Command cancelCommand() {
        return cancelCommand;
    }
}