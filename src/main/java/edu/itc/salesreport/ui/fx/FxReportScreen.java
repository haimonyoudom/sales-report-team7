package edu.itc.salesreport.ui.fx;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import edu.itc.salesreport.ui.mvp.ReportPresenter;
import edu.itc.salesreport.ui.mvp.ReportScreen;
import edu.itc.salesreport.ui.mvp.RowItem;
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

/** MVP Passive View: only setters and event forwarding. */
public final class FxReportScreen implements ReportScreen {
    private final BorderPane root = new BorderPane();
    private final TextField monthField = new TextField("2026-09");
    private final ComboBox<String> branchBox = new ComboBox<>();
    private final TableView<RowItem> table = new TableView<>();
    private final BarChart<String, Number> chart =
            new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final Label status = new Label("No month loaded");
    private final Label error = new Label();

    public FxReportScreen() {
        branchBox.getItems().setAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue("ALL");

        var dateCol = new TableColumn<RowItem, String>("Date");
        dateCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().date()));
        dateCol.setPrefWidth(90);

        var receiptCol = new TableColumn<RowItem, String>("Receipt");
        receiptCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().receipt()));
        receiptCol.setPrefWidth(110);

        var productCol = new TableColumn<RowItem, String>("Product");
        productCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().product()));
        productCol.setPrefWidth(180);

        var categoryCol = new TableColumn<RowItem, String>("Category");
        categoryCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().category()));
        categoryCol.setPrefWidth(110);

        var qtyCol = new TableColumn<RowItem, String>("Qty");
        qtyCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().quantity()));
        qtyCol.setPrefWidth(60);

        var revenueCol = new TableColumn<RowItem, String>("Revenue");
        revenueCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().revenue()));
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
    }

    public Parent root() {
        return root;
    }

    public void attach(ReportPresenter presenter) {
        monthField.setOnAction(e -> presenter.monthEntered(monthField.getText()));
        branchBox.setOnAction(e -> presenter.branchChosen(branchBox.getValue()));
    }

    @Override
    public void showRows(List<RowItem> rows) {
        table.getItems().setAll(rows);
    }

    @Override
    public void showCategoryRevenue(Map<String, BigDecimal> revenue) {
        var series = new XYChart.Series<String, Number>();
        revenue.forEach((cat, rev) -> series.getData().add(new XYChart.Data<>(cat, rev)));
        chart.getData().setAll(series);
    }

    @Override
    public void showStatus(String text) {
        status.setText(text);
    }

    @Override
    public void showError(String message) {
        error.setText(message);
    }
}

