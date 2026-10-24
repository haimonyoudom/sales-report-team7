package edu.itc.salesreport.web;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.itc.salesreport.engine.SalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ui.InputRules;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.Validation;
import edu.itc.salesreport.ui.Validation.Invalid;
import edu.itc.salesreport.ui.Validation.Valid;

@Controller
public class ReportWebController {
    private record Inputs(Validation<YearMonth> month, Validation<String> branch) {}

    private final CsvMonthReader reader;
    private final SalesAggregator aggregator;
    private final Clock clock;
    private final Path dataDir;

    public ReportWebController(CsvMonthReader reader, SalesAggregator aggregator,
            Clock clock, @Value("${salesreport.data-dir:data}") String dataDir) {
        this.reader = reader;
        this.aggregator = aggregator;
        this.clock = clock;
        // Keep this as a String in the constructor: a relative Path can be
        // resolved against the servlet context instead of the process directory.
        this.dataDir = Path.of(dataDir);
    }

    @GetMapping("/report")
    public String report(@RequestParam(defaultValue = "2026-09") String month,
            @RequestParam(defaultValue = "ALL") String branch, Model page) {
        page.addAttribute("month", month);
        page.addAttribute("branch", branch);
        page.addAttribute("branches", List.of("ALL", "PNH", "REP", "BTB"));

        var inputs = new Inputs(InputRules.month(month, clock), InputRules.branch(branch));
        if (inputs.month() instanceof Valid<?> validMonth
                && inputs.branch() instanceof Valid<?> validBranch) {
            show((YearMonth) validMonth.value(), (String) validBranch.value(), page);
        } else if (inputs.month() instanceof Invalid<?> invalidMonth) {
            page.addAttribute("error", invalidMonth.message());
        } else if (inputs.branch() instanceof Invalid<?> invalidBranch) {
            page.addAttribute("error", invalidBranch.message());
        }
        return "report";
    }

    private void show(YearMonth month, String branch, Model page) {
        // A new model per request prevents selected-branch state leaking between users.
        var reportModel = new SalesModel(reader, aggregator);
        try {
            reportModel.load(dataDir, month);
            reportModel.selectBranch(branch);
            page.addAttribute("summary", reportModel.summary());
            page.addAttribute("rejected", reportModel.rejectedRows());
            page.addAttribute("month", month.toString());
            page.addAttribute("branch", branch);
        } catch (IOException e) {
            page.addAttribute("error", "Could not read report data for " + month + ": " + e.getMessage());
        }
    }
}
