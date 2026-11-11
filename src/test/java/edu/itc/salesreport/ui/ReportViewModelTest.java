package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Plain JUnit: no JavaFX toolkit, no window, no robot, no sleep. */
class ReportViewModelTest {

    static final Clock SEP_27 = Clock.fixed(
            Instant.parse("2026-09-27T03:00:00Z"),
            ZoneId.of("Asia/Phnom_Penh"));
    static final YearMonth AUG = YearMonth.of(2026, 8);

    ManualJobs jobs = new ManualJobs();
    List<String> loaded = new ArrayList<>();
    boolean failNext;
    ReportViewModel vm;

    @BeforeEach
    void setUp() {
        SummarySource source = (month, branch) -> {
            if (failNext) {
                throw new IOException(branch + "-2026-08-03.csv: unreadable");
            }
            loaded.add(branch);
            return summary(branch, "200.00");
        };
        vm = new ReportViewModel(source, jobs, SEP_27);
    }

    static BranchSummary summary(String branch, String revenue) {
        var rice = new ProductTotal("SKU-1001", "Jasmine Rice 5kg",
                new BigDecimal(revenue), 5);
        return new BranchSummary(branch, new BigDecimal(revenue),
                BigDecimal.ZERO, 10, Map.of(), List.of(rice), Map.of());
    }

    @Test
    void futureMonthIsRejected() {
        vm.monthProperty().set(YearMonth.of(2026, 10));
        assertEquals("Month 2026-10 is in the future.",
                vm.monthErrorProperty().get());
        assertEquals("Month 2026-10 is in the future.",
                vm.validationMessageProperty().get());
        assertFalse(vm.canGenerateProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void cannotGenerateBeforeAMonthIsChosen() {
        assertEquals("Choose a month.", vm.monthErrorProperty().get());
        assertEquals("Choose a month.", vm.validationMessageProperty().get());
        assertFalse(vm.canGenerateProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void emptyBranchSelectionIsRejected() {
        vm.monthProperty().set(AUG);
        for (String b : ReportViewModel.BRANCHES) {
            vm.branchSelectedProperty(b).set(false);
        }
        assertEquals("Select at least one branch.", vm.branchErrorProperty().get());
        assertEquals("Select at least one branch.", vm.validationMessageProperty().get());
        assertFalse(vm.canGenerateProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void generateStartsLoadingAndDisablesItself() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        assertTrue(vm.loadingProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
        assertTrue(vm.cancelCommand().isExecutable());
        assertEquals(1, jobs.pendingCount());
        assertEquals("Loading 2026-08...", vm.statusProperty().get());
    }

    @Test
    void generateIsIgnoredWhileInvalid() {
        assertFalse(vm.generateCommand().isExecutable());
        vm.generateCommand().execute();
        assertFalse(vm.loadingProperty().get());
        assertEquals(0, jobs.pendingCount());
    }

    @Test
    void successfulLoadFillsSummariesAndTotal() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        assertTrue(vm.loadingProperty().get());

        jobs.runNext();

        assertFalse(vm.loadingProperty().get());
        assertEquals(3, vm.summaries().size());
        assertEquals(new BigDecimal("600.00"), vm.totalRevenueProperty().get());
        assertEquals("USD 600.00", vm.totalRevenueTextProperty().get());
        assertEquals("Loaded 3 branch(es) for 2026-08.", vm.statusProperty().get());
        assertEquals("", vm.loadErrorProperty().get());
        assertEquals(1, vm.visibleProducts().size());
        assertTrue(vm.generateCommand().isExecutable());
        assertFalse(vm.cancelCommand().isExecutable());
    }

    @Test
    void progressFollowsTheBranches() {
        vm.monthProperty().set(AUG);
        List<Double> progressValues = new ArrayList<>();
        vm.progressProperty().addListener((obs, oldVal, newVal) ->
                progressValues.add(newVal.doubleValue()));

        vm.generateCommand().execute();
        jobs.runNext();

        assertEquals(List.of(1.0 / 3.0, 2.0 / 3.0, 1.0), progressValues);
    }

    @Test
    void failureKeepsPreviousDataAndShowsTheError() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();
        assertEquals(3, vm.summaries().size());
        assertEquals("USD 600.00", vm.totalRevenueTextProperty().get());

        failNext = true;
        vm.generateCommand().execute();
        assertTrue(vm.loadingProperty().get());

        jobs.runNext();

        assertFalse(vm.loadingProperty().get());
        assertEquals("PNH-2026-08-03.csv: unreadable", vm.loadErrorProperty().get());
        assertEquals("Load failed, previous data kept.", vm.statusProperty().get());
        assertEquals(3, vm.summaries().size());
        assertEquals("USD 600.00", vm.totalRevenueTextProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
    }

    @Test
    void cancelReenablesGenerateAndKeepsOldData() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();
        assertEquals(3, vm.summaries().size());

        vm.generateCommand().execute();
        assertTrue(vm.loadingProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
        assertTrue(vm.cancelCommand().isExecutable());

        vm.cancelCommand().execute();

        assertFalse(vm.loadingProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
        assertFalse(vm.cancelCommand().isExecutable());
        assertEquals("Cancelled, previous data kept.", vm.statusProperty().get());
        assertEquals(3, vm.summaries().size());
        assertEquals(0, jobs.pendingCount());
    }

    @Test
    void productFilterIsCaseInsensitive() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();
        assertEquals(1, vm.visibleProducts().size());

        vm.productFilterProperty().set("JASMINE");
        assertEquals(1, vm.visibleProducts().size());

        vm.productFilterProperty().set("sku-1001");
        assertEquals(1, vm.visibleProducts().size());

        vm.productFilterProperty().set("  rice  ");
        assertEquals(1, vm.visibleProducts().size());

        vm.productFilterProperty().set("Nonexistent");
        assertEquals(0, vm.visibleProducts().size());

        vm.productFilterProperty().set("");
        assertEquals(1, vm.visibleProducts().size());

        vm.productFilterProperty().set(null);
        assertEquals(1, vm.visibleProducts().size());
    }

    @Test
    void workerUsesASnapshotOfTheInputs() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();

        // Mutate inputs after generate was clicked
        vm.monthProperty().set(YearMonth.of(2026, 7));
        vm.branchSelectedProperty("PNH").set(false);

        jobs.runNext();

        // Worker used the snapshot (AUG and all 3 branches)
        assertEquals(List.of("PNH", "REP", "BTB"), loaded);
        assertEquals("Loaded 3 branch(es) for 2026-08.", vm.statusProperty().get());
    }

    @Test
    void monthChoicesContain12MonthsEndingAtCurrentMonth() {
        assertEquals(12, vm.monthChoices().size());
        assertEquals(YearMonth.of(2026, 9), vm.monthChoices().get(0));
        assertEquals(YearMonth.of(2025, 10), vm.monthChoices().get(11));
    }

    @Test
    void mergeProductsAggregatesAndSortsByRevenue() {
        SummarySource multiProductSource = (month, branch) -> {
            var p1 = new ProductTotal("SKU-1", "Product One", new BigDecimal("100.00"), 2);
            var p2 = new ProductTotal("SKU-2", "Product Two", new BigDecimal("50.00"), 1);
            return new BranchSummary(branch, new BigDecimal("150.00"),
                    BigDecimal.ZERO, 3, Map.of(), List.of(p1, p2), Map.of());
        };
        ReportViewModel multiVm = new ReportViewModel(multiProductSource, jobs, SEP_27);
        multiVm.monthProperty().set(AUG);
        multiVm.generateCommand().execute();
        jobs.runNext();

        assertEquals(2, multiVm.visibleProducts().size());
        ProductTotal top = multiVm.visibleProducts().get(0);
        assertEquals("SKU-1", top.sku());
        assertEquals(new BigDecimal("300.00"), top.revenue());
        assertEquals(6, top.quantity());

        ProductTotal second = multiVm.visibleProducts().get(1);
        assertEquals("SKU-2", second.sku());
        assertEquals(new BigDecimal("150.00"), second.revenue());
        assertEquals(3, second.quantity());
    }

    @Test
    void twoViewsListenersBothReceiveTotalRevenueAfterOneLoad() {
        // Challenge requirement 4: simulates two views listening to the same ViewModel
        SummarySource source = (month, branch) -> new BranchSummary(
                branch,
                new BigDecimal("200.00"),
                BigDecimal.ZERO.setScale(2),
                1,
                Map.of(),
                List.of(),
                Map.of()
        );
        ReportViewModel sharedVm = new ReportViewModel(source, new CallerThreadJobs(), SEP_27);
        sharedVm.monthProperty().set(AUG);

        List<String> view1Received = new ArrayList<>();
        List<String> view2Received = new ArrayList<>();

        sharedVm.totalRevenueTextProperty().addListener((obs, oldVal, newVal) -> view1Received.add(newVal));
        sharedVm.totalRevenueTextProperty().addListener((obs, oldVal, newVal) -> view2Received.add(newVal));

        sharedVm.generateCommand().execute();

        assertEquals("USD 600.00", sharedVm.totalRevenueTextProperty().get());
        assertTrue(view1Received.contains("USD 600.00"), "View 1 should receive USD 600.00");
        assertTrue(view2Received.contains("USD 600.00"), "View 2 should receive USD 600.00");
        assertEquals("USD 600.00", view1Received.get(view1Received.size() - 1));
        assertEquals("USD 600.00", view2Received.get(view2Received.size() - 1));
    }
}
