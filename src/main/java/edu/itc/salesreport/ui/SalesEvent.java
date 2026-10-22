package edu.itc.salesreport.ui;

import java.time.YearMonth;

public sealed interface SalesEvent {
    record MonthLoaded(YearMonth month, int rows, int rejected) implements SalesEvent {}
    record BranchSelected(String branch) implements SalesEvent {}
}