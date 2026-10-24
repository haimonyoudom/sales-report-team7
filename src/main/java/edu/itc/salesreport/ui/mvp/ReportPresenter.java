package edu.itc.salesreport.ui.mvp;

import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.InputRules;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import edu.itc.salesreport.ui.Validation.Invalid;
import edu.itc.salesreport.ui.Validation.Valid;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.Locale;

/** MVP presenter: handles input and pushes formatted data. */
public final class ReportPresenter implements SalesModelListener {
    private final SalesModel model;
    private final ReportScreen screen;
    private final Path dataDir;
    private final Clock clock;

    public ReportPresenter(SalesModel model, ReportScreen screen,
            Path dataDir, Clock clock) {
        this.model = model;
        this.screen = screen;
        this.dataDir = dataDir;
        this.clock = clock;
        model.addListener(this);
    }

    public void monthEntered(String text) {
        switch (InputRules.month(text, clock)) {
            case Valid(YearMonth month) -> {
                try {
                    model.load(dataDir, month);
                } catch (IOException e) {
                    screen.showError("No sales data for " + month);
                }
            }
            case Invalid(String message) -> screen.showError(message);
        }
    }

    public void branchChosen(String text) {
        switch (InputRules.branch(text)) {
            case Valid(String branch) -> model.selectBranch(branch);
            case Invalid(String message) -> screen.showError(message);
        }
    }

    @Override
    public void modelChanged(SalesEvent event) {
        var s = model.summary();
        var rows = model.visibleTransactions().stream()
                .map(ReportPresenter::toRow)
                .toList();
        screen.showRows(rows);
        screen.showCategoryRevenue(s.revenueByCategory());
        screen.showStatus("%s · %d receipts · revenue USD %s · basket USD %s"
                .formatted(model.branch(), s.receipts(), money(s.revenue()), money(s.averageBasket())));
        screen.showError("");
    }

    private static RowItem toRow(SaleTransaction t) {
        return new RowItem(
                t.date().toString(),
                t.receiptNo(),
                t.productName(),
                t.category(),
                Integer.toString(t.quantity()),
                money(t.revenue())
        );
    }

    public static String money(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }
}
