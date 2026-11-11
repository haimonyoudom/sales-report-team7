package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.itc.salesreport.model.BranchSummary;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportViewModelValidationTest {

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-09-27T10:00:00Z"), ZoneId.of("UTC"));
    private final AtomicBoolean repFails = new AtomicBoolean(false);
    private ReportViewModel vm;

    private static BranchSummary summary(String branch) {
        return new BranchSummary(branch, new BigDecimal("10.00"),
                BigDecimal.ZERO.setScale(2), 1, Map.of(), List.of(), Map.of());
    }

    @BeforeEach
    void setUp() {
        SummarySource source = (m, b) -> {
            if (repFails.get() && b.equals("REP"))
                throw new IOException("REP-2026-08-03.csv: unreadable");
            return summary(b);
        };
        vm = new ReportViewModel(source, new CallerThreadJobs(), clock);
    }

    private void ticks(boolean pnh, boolean rep, boolean btb) {
        vm.branchSelectedProperty("PNH").set(pnh);
        vm.branchSelectedProperty("REP").set(rep);
        vm.branchSelectedProperty("BTB").set(btb);
    }

    private void check(YearMonth month, String message, boolean enabled) {
        vm.monthProperty().set(month);
        assertEquals(message, vm.validationMessageProperty().get());
        assertEquals(enabled, vm.canGenerateProperty().get());
    }

    @Test
    void emptyMonthAllBranches() {
        ticks(true, true, true);
        check(null, "Choose a month.", false);
    }

    @Test
    void futureMonth() {
        ticks(true, true, true);
        check(YearMonth.of(2026, 10), "Month 2026-10 is in the future.", false);
    }

    @Test
    void noBranch() {
        ticks(false, false, false);
        check(YearMonth.of(2026, 8), "Select at least one branch.", false);
    }

    @Test
    void emptyMonthAndNoBranch() {
        ticks(false, false, false);
        check(null, "Choose a month. Select at least one branch.", false);
    }

    @Test
    void validInput() {
        ticks(true, false, false);
        check(YearMonth.of(2026, 9), "", true);
    }

    @Test
    void failedLoadKeepsPreviousDataAndShowsFileMessage() {
        vm.monthProperty().set(YearMonth.of(2026, 8));
        vm.generateCommand().execute();
        assertEquals(3, vm.summaries().size());
        assertEquals("", vm.loadErrorProperty().get());

        repFails.set(true);
        vm.generateCommand().execute();
        assertEquals("REP-2026-08-03.csv: unreadable",
                vm.loadErrorProperty().get());
        assertEquals("Load failed, previous data kept.",
                vm.statusProperty().get());
        assertEquals(3, vm.summaries().size());
        assertFalse(vm.loadingProperty().get());
        assertTrue(vm.canGenerateProperty().get());
    }

    @Test
    void twoViewsListenersBothReceiveTotalRevenueAfterOneLoad() {
        // Challenge requirement 4: two listeners on totalRevenueTextProperty() both receive "USD 600.00" after one load
        SummarySource fixedSource = (month, branch) -> new BranchSummary(
                branch,
                new BigDecimal("200.00"),
                BigDecimal.ZERO.setScale(2),
                1,
                Map.of(),
                List.of(),
                Map.of()
        );
        ReportViewModel sharedVm = new ReportViewModel(fixedSource, new CallerThreadJobs(), clock);
        sharedVm.monthProperty().set(YearMonth.of(2026, 8));

        java.util.List<String> view1Values = new java.util.ArrayList<>();
        java.util.List<String> view2Values = new java.util.ArrayList<>();

        sharedVm.totalRevenueTextProperty().addListener((obs, oldVal, newVal) -> view1Values.add(newVal));
        sharedVm.totalRevenueTextProperty().addListener((obs, oldVal, newVal) -> view2Values.add(newVal));

        sharedVm.generateCommand().execute();

        assertEquals("USD 600.00", sharedVm.totalRevenueTextProperty().get());
        assertTrue(view1Values.contains("USD 600.00"));
        assertTrue(view2Values.contains("USD 600.00"));
    }
}

