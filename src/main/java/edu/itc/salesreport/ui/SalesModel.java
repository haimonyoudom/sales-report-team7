package edu.itc.salesreport.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.itc.salesreport.engine.SalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.MonthData;
import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.SaleTransaction;

/** MVC model of the report viewer. Confined to one thread (the UI thread). */
public final class SalesModel {
    public static final String ALL = "ALL";

    private final CsvMonthReader reader;
    private final SalesAggregator aggregator;
    private final List<SalesModelListener> listeners = new CopyOnWriteArrayList<>();
    private MonthData data;            // null until the first successful load
    private String branch = ALL;

    public SalesModel(CsvMonthReader reader, SalesAggregator aggregator) {
        this.reader = reader;
        this.aggregator = aggregator;
    }

    public void addListener(SalesModelListener l) { listeners.add(l); }
    public void removeListener(SalesModelListener l) { listeners.remove(l); }

    /** Replaces the data; on failure the old state is kept. */
    public void load(Path dataDir, YearMonth month) throws IOException {
        MonthData loaded = reader.read(dataDir, month);
        this.data = loaded;
        fire(new SalesEvent.MonthLoaded(loaded.month(), loaded.rows().size(), loaded.rejected().size()));
    }

    public void selectBranch(String newBranch) {
        if (Objects.equals(this.branch, newBranch)) {
            return;
        }
        this.branch = newBranch;
        fire(new SalesEvent.BranchSelected(newBranch));
    }

    public String branch() { return branch; }

    public Optional<YearMonth> month() {
        return Optional.ofNullable(data).map(MonthData::month);
    }

    public List<SaleTransaction> visibleTransactions() {
        if (data == null) {
            return List.of();
        }
        if (ALL.equals(branch)) {
            return data.rows();
        }
        return data.rows().stream()
                .filter(t -> t.branch().equals(branch))
                .toList();
    }

    public BranchSummary summary() {
        return aggregator.summarize(branch, visibleTransactions());
    }

    public List<String> rejectedRows() {
        return data == null ? List.of() : data.rejected();
    }

    private void fire(SalesEvent event) {
        for (var l : listeners) l.modelChanged(event);
    }
}