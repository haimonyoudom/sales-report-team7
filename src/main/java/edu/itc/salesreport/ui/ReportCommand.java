package edu.itc.salesreport.ui;

import java.time.YearMonth;

public sealed interface ReportCommand {
    record LoadMonth(YearMonth month) implements ReportCommand {}
    record SelectBranch(String branch) implements ReportCommand {}
}