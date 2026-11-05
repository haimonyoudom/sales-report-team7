package edu.itc.salesreport.ui;

import edu.itc.salesreport.model.BranchSummary;
import java.time.YearMonth;

/** What the ViewModel needs from the model: one branch summary at a time. */
@FunctionalInterface
public interface SummarySource {
    BranchSummary load(YearMonth month, String branch) throws Exception;
}