package edu.itc.salesreport.ui.console;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.YearMonth;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesEvent.BranchSelected;
import edu.itc.salesreport.ui.SalesEvent.MonthLoaded;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;

public class ConsoleReportView implements SalesModelListener {
    private final SalesModel model;
    private final PrintStream out;

    public ConsoleReportView(SalesModel model, PrintStream out) {
        this.model = model;
        this.out = out;
        model.addListener(this);
    }

    @Override
    public void modelChanged(SalesEvent event) {
        out.println(switch (event) {
            case MonthLoaded(var month, var rows, var rejected) ->
                "Loaded %s: %d rows, %d rejected".formatted(month, rows, rejected);
            case BranchSelected(var branch) -> "Branch " + branch;
        });
        var s = model.summary();
        out.printf("  %-4s revenue %9s  receipts %4d  basket %5s%n",
                s.branch(), s.revenue(), s.receipts(), s.averageBasket());
        s.revenueByCategory().forEach(
                (cat, rev) -> out.printf("    %-14s %9s%n", cat, rev));
    }

    public static void main(String[] args) throws Exception {
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        var model = new SalesModel(reader, aggregator);
        new ConsoleReportView(model, System.out);
        model.load(Path.of("data"), YearMonth.parse("2026-09"));
        model.selectBranch("PNH");
    }
}