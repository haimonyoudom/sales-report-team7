package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.SalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.model.BranchSummary;
import java.nio.file.Path;
import java.time.YearMonth;

public final class CsvSummarySource implements SummarySource {
    private final CsvMonthReader reader;
    private final SalesAggregator aggregator;
    private final Path dataDir;

    public CsvSummarySource(CsvMonthReader reader, SalesAggregator aggregator, Path dataDir) {
        this.reader = reader; this.aggregator = aggregator; this.dataDir = dataDir;
    }

    @Override
    public BranchSummary load(YearMonth month, String branch) throws Exception {
        return aggregator.summarize(branch, reader.readBranch(dataDir, month, branch).rows());
    }
}