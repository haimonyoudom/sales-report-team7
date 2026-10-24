package edu.itc.salesreport.ui.mvp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesModel;

class ReportPresenterTest {
    @TempDir
    Path tempDir;

    private SalesModel model;
    private FakeScreen screen;
    private ReportPresenter presenter;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(tempDir);
        model = new SalesModel(new CsvMonthReader(new CsvTransactionParser()), new DefaultSalesAggregator());
        screen = new FakeScreen();
        Clock clock = Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZoneId.of("UTC"));
        presenter = new ReportPresenter(model, screen, tempDir, clock);
    }

    @Test
    void updatesDisplayOnMonthAndBranchInput() {
        presenter.monthEntered("2026-09");
        presenter.branchChosen("PNH");

        assertEquals("PNH · 1 receipts · revenue USD 31.00 · basket USD 31.00", screen.status);
        assertEquals(2, screen.rows.size());
        assertEquals(new RowItem("2026-09-01", "PNH-000001", "Power Bank 10000mAh", "Electronics", "1", "18.00"),
                screen.rows.get(1));
    }

    @Test
    void formatsMoneyCorrectly() {
        assertEquals("1,234.50", ReportPresenter.money(new BigDecimal("1234.5")));
    }

    static class FakeScreen implements ReportScreen {
        List<RowItem> rows = List.of();
        Map<String, BigDecimal> revenue = Map.of();
        String status = "";
        String error = "";

        @Override
        public void showRows(List<RowItem> rows) {
            this.rows = rows;
        }

        @Override
        public void showCategoryRevenue(Map<String, BigDecimal> revenue) {
            this.revenue = revenue;
        }

        @Override
        public void showStatus(String text) {
            this.status = text;
        }

        @Override
        public void showError(String message) {
            this.error = message;
        }
    }
}

