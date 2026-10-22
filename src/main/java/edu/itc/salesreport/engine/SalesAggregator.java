package edu.itc.salesreport.engine;

import java.util.List;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.SaleTransaction;

public interface SalesAggregator {
    BranchSummary summarize(String branch, List<SaleTransaction> rows);
}
