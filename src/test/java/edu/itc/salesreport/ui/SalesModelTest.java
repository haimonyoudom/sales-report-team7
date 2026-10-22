package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesEvent.BranchSelected;
import edu.itc.salesreport.ui.SalesEvent.MonthLoaded;

class SalesModelTest {
    @TempDir
    Path tempDir;

    private SalesModel model;

    @BeforeEach
    void setUp() throws Exception {
        TestData.writeSample(tempDir);
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        model = new SalesModel(reader, aggregator);
    }

    @Test
    void loadFiresMonthLoadedAndChainRevenueIs52() throws Exception {
        List<SalesEvent> events = new ArrayList<>();
        model.addListener(events::add);

        model.load(tempDir, YearMonth.parse("2026-09"));

        assertEquals(List.of(new MonthLoaded(YearMonth.parse("2026-09"), 3, 1)), events);
        assertEquals(new BigDecimal("52.00"), model.summary().revenue());
    }

    @Test
    void selectingPnHTwiceFiresOnceAndGivesRevenueAndBasket31() throws Exception {
        model.load(tempDir, YearMonth.parse("2026-09"));

        List<SalesEvent> events = new ArrayList<>();
        model.addListener(events::add);

        model.selectBranch("PNH");
        model.selectBranch("PNH");

        assertEquals(List.of(new BranchSelected("PNH")), events);
        var summary = model.summary();
        assertEquals("PNH", summary.branch());
        assertEquals(new BigDecimal("31.00"), summary.revenue());
        assertEquals(new BigDecimal("31.00"), summary.averageBasket());
    }

    @Test
    void loadOf202607ThrowsNoSuchFileExceptionFiresNothingAndKeeps3Rows() throws Exception {
        model.load(tempDir, YearMonth.parse("2026-09"));
        assertEquals(3, model.visibleTransactions().size());

        List<SalesEvent> events = new ArrayList<>();
        model.addListener(events::add);

        assertThrows(NoSuchFileException.class, () -> model.load(tempDir, YearMonth.parse("2026-07")));

        assertTrue(events.isEmpty());
        assertEquals(3, model.visibleTransactions().size());
    }

    @Test
    void removedListenerIsNotCalled() throws Exception {
        List<SalesEvent> events = new ArrayList<>();
        SalesModelListener listener = events::add;
        model.addListener(listener);
        model.removeListener(listener);

        model.load(tempDir, YearMonth.parse("2026-09"));
        model.selectBranch("PNH");

        assertTrue(events.isEmpty());
    }
}
