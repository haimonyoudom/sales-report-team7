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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** MVC view: observes SalesModel, forwards gestures to the controller. */
public final class ReportView implements SalesModelListener, ErrorDisplay {
    private final SalesModel model;
    private final BorderPane root = new BorderPane();
    private final TextField monthField = new TextField("2026-09");
    private final ComboBox<String> branchBox = new ComboBox<>();
    private final TableView<SaleTransaction> table = new TableView<>();
    private final BarChart<String, Number> chart =
            new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final Label status = new Label("No month loaded");
    private final Label error = new Label();

    public ReportView(SalesModel model) {
        this.model = model;
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

        chart.setTitle("Revenue by category (USD)");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        error.setStyle("-fx-text-fill: firebrick;");

        HBox filters = new HBox(10, new Label("Month:"), monthField, new Label("Branch:"), branchBox);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(10));
        root.setTop(filters);

        SplitPane center = new SplitPane(table, chart);
        center.setDividerPositions(0.6);
        root.setCenter(center);

        VBox bottom = new VBox(4, status, error);
        bottom.setPadding(new Insets(6, 10, 6, 10));
        root.setBottom(bottom);

        model.addListener(this);
    }

    public Parent root() { return root; }

    public void bind(ReportController controller) {
        monthField.setOnAction(e -> controller.monthEntered(monthField.getText()));
        branchBox.setOnAction(e -> controller.branchChosen(branchBox.getValue()));
    }

    public TextField monthField() { return monthField; }
    public ComboBox<String> branchBox() { return branchBox; }
    public TableView<SaleTransaction> table() { return table; }
    public BarChart<String, Number> chart() { return chart; }
    public Label status() { return status; }
    public Label error() { return error; }

    @Override
    public void modelChanged(SalesEvent event) {
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

    // records have no JavaFX properties: adapt each column with a lambda
    private static TableColumn<SaleTransaction, String> column(String title,
            Function<SaleTransaction, String> value) {
        var col = new TableColumn<SaleTransaction, String>(title);
        col.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(value.apply(c.getValue())));
        return col;
    }
}