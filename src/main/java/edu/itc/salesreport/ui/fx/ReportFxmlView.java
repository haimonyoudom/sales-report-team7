package edu.itc.salesreport.ui.fx;

import java.util.List;
import java.util.function.Function;

import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesEvent.BranchSelected;
import edu.itc.salesreport.ui.SalesEvent.MonthLoaded;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public final class ReportFxmlView implements SalesModelListener, ErrorDisplay {
    @FXML private BorderPane root;
    @FXML private TextField monthField;
    @FXML private ComboBox<String> branchBox;
    @FXML private TableView<SaleTransaction> table;
    @FXML private BarChart<String, Number> chart;
    @FXML private Label status;
    @FXML private Label error;

    private SalesModel model;

    @FXML
    public void initialize() {
        branchBox.getItems().setAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue("ALL");

        var dateCol = column("Date", t -> t.date().toString());
        dateCol.setPrefWidth(90);
        var receiptCol = column("Receipt", SaleTransaction::receiptNo);
        receiptCol.setPrefWidth(110);
        var productCol = column("Product", SaleTransaction::productName);
        productCol.setPrefWidth(180);
        var categoryCol = column("Category", SaleTransaction::category);
        categoryCol.setPrefWidth(110);
        var qtyCol = column("Qty", t -> Integer.toString(t.quantity()));
        qtyCol.setPrefWidth(60);
        var revenueCol = column("Revenue", t -> t.revenue().toString());
        revenueCol.setPrefWidth(80);

        table.getColumns().setAll(List.of(
                dateCol, receiptCol, productCol, categoryCol, qtyCol, revenueCol
        ));
    }

    public void setModel(SalesModel model) {
        this.model = model;
        model.addListener(this);
    }

    public Parent root() { return root; }
    public TextField monthField() { return monthField; }
    public ComboBox<String> branchBox() { return branchBox; }
    public TableView<SaleTransaction> table() { return table; }
    public BarChart<String, Number> chart() { return chart; }
    public Label status() { return status; }
    public Label error() { return error; }

    public void bind(ReportController controller) {
        monthField.setOnAction(e -> controller.monthEntered(monthField.getText()));
        branchBox.setOnAction(e -> controller.branchChosen(branchBox.getValue()));
    }

    @Override
    public void modelChanged(SalesEvent event) {
        if (model == null) return;
        branchBox.setValue(model.branch());
        table.getItems().setAll(model.visibleTransactions());

        var series = new XYChart.Series<String, Number>();
        model.summary().revenueByCategory().forEach((cat, rev) ->
                series.getData().add(new XYChart.Data<>(cat, rev)));
        chart.getData().setAll(series);

        status.setText(switch (event) {
            case MonthLoaded(var month, var rows, var rejected) ->
                "%s: %d rows, %d rejected · revenue USD %s · receipts %d"
                        .formatted(month, rows, rejected,
                                model.summary().revenue(), model.summary().receipts());
            case BranchSelected(var branch) ->
                "BranchSelected: revenue USD %s, receipts %d"
                        .formatted(model.summary().revenue(), model.summary().receipts());
        });
    }

    @Override public void showError(String message) { error.setText(message); }
    @Override public void clearError() { error.setText(""); }

    private static TableColumn<SaleTransaction, String> column(String title,
            Function<SaleTransaction, String> value) {
        var col = new TableColumn<SaleTransaction, String>(title);
        col.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(value.apply(c.getValue())));
        return col;
    }
}
