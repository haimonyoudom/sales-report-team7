package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;

class ReportControllerTest {
    static final class FakeErrors implements ErrorDisplay {
        final List<String> shown = new ArrayList<>();

        @Override public void showError(String message) { shown.add(message); }
        @Override public void clearError() { shown.add("<cleared>"); }
    }

    @TempDir Path dataDir;
    private final FakeErrors errors = new FakeErrors();
    private final List<SalesEvent> events = new ArrayList<>();
    private SalesModel model;
    private ReportController controller;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(dataDir);
        model = new SalesModel(new CsvMonthReader(new CsvTransactionParser()),
                new DefaultSalesAggregator());
        model.addListener(events::add);
        Clock clock = Clock.fixed(Instant.parse("2026-09-27T03:00:00Z"),
                ZoneId.of("Asia/Phnom_Penh"));
        controller = new ReportController(model, dataDir, clock, errors);
    }

    @Test
    void validMonthLoadsModelAndClearsError() {
        controller.monthEntered(" 2026-09 ");

        assertEquals(List.of(new SalesEvent.MonthLoaded(
                java.time.YearMonth.of(2026, 9), 3, 1)), events);
        assertEquals(List.of("<cleared>"), errors.shown);
    }

    @Test
    void invalidMonthNeverReachesModel() {
        controller.monthEntered("09/2026");

        assertTrue(events.isEmpty());
        assertEquals(List.of("'09/2026' is not a month in the form yyyy-MM"),
                errors.shown);
    }

    @Test
    void missingFolderBecomesMessage() {
        controller.monthEntered("2026-07");

        assertEquals(List.of("No sales data found in "
                + dataDir.resolve("2026-07")), errors.shown);
        assertTrue(events.isEmpty());
    }

    @Test
    void branchFilterNarrowsRows() {
        controller.monthEntered("2026-09");
        errors.shown.clear();

        controller.branchChosen("rep");

        assertEquals(1, model.visibleTransactions().size());
        assertEquals("REP", model.branch());
        assertEquals(List.of("<cleared>"), errors.shown);
    }
}
