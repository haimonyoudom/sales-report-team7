package edu.itc.salesreport.ui.mvp;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface ReportScreen {
    void showRows(List<RowItem> rows);
    void showCategoryRevenue(Map<String, BigDecimal> revenue);
    void showStatus(String text);
    void showError(String message);
}

