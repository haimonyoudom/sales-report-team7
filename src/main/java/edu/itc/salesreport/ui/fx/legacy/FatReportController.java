package edu.itc.salesreport.ui.fx.legacy;

import edu.itc.salesreport.ui.ReportController;
import javafx.scene.control.TextField;

/** Compatibility shell for the former handler; behavior lives in the MVC controller. */
public final class FatReportController {
    private final TextField monthField;
    private final ReportController controller;

    public FatReportController(TextField monthField, ReportController controller) {
        this.monthField = monthField;
        this.controller = controller;
    }

    public void onLoad() {
        controller.monthEntered(monthField.getText());
    }
}
