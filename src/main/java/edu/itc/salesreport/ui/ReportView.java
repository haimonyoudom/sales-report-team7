package edu.itc.salesreport.ui;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Task 2 skeleton: build controls and bind them. No logic here. */
public final class ReportView {

    private final BorderPane root = new BorderPane();

    public ReportView(ReportViewModel vm) {
        ComboBox<YearMonth> month = new ComboBox<>(vm.monthChoices());
        month.setEditable(true);
        month.setConverter(new YearMonthConverter());
        month.valueProperty().bindBidirectional(vm.monthProperty());

        HBox filters = new HBox(8, new Label("Month"), month);
        for (String b : ReportViewModel.BRANCHES) {
            CheckBox box = new CheckBox(b);
            // TODO two-way: box.selectedProperty() <-> branchSelected(b)
            box.selectedProperty().bindBidirectional(vm.branchSelectedProperty(b));
            filters.getChildren().add(box);
        }

        Button generate = new Button("Generate");
        generate.setOnAction(e -> vm.generateCommand().execute());
        generate.disableProperty().bind(
                Bindings.not(vm.generateCommand().executableProperty()));
        Button cancel = new Button("Cancel");
        // TODO same two lines for vm.cancelCommand()
        cancel.setOnAction(e -> vm.cancelCommand().execute());
        cancel.disableProperty().bind(Bindings.not(vm.cancelCommand().executableProperty()));

        filters.getChildren().addAll(generate, cancel);

        ProgressBar bar = new ProgressBar();
        Label validation = new Label();
        Label status = new Label();
        Label error = new Label();
        Label total = new Label();
        // TODO one-way: bar.progress, bar.visible, validation.text,
        // status.text, error.text, total.text
        bar.progressProperty().bind(vm.progressProperty());
        bar.visibleProperty().bind(vm.loadingProperty());
        validation.textProperty().bind(vm.validationMessageProperty());
        status.textProperty().bind(vm.statusProperty());
        error.textProperty().bind(vm.loadErrorProperty());
        total.textProperty().bind(vm.totalRevenueTextProperty());

        TableView<BranchSummary> table = new TableView<>(vm.summaries());
        table.getColumns().add(column("Branch", BranchSummary::branch));
        table.getColumns().add(column("Revenue",
                s -> s.revenue().toPlainString()));
        // TODO columns Discounts, Receipts, Avg basket

        table.getColumns().add(column("Discounts", s -> s.discounts().toPlainString()));
        table.getColumns().add(column("Receipts", s -> Long.toString(s.receipts())));
        table.getColumns().add(column("Avg basket", s -> s.averageBasket().toPlainString()));

        TextField search = new TextField();
        search.setPromptText("Filter products");
        // TODO two-way: search.text <-> vm.productFilter
        search.textProperty().bindBidirectional(vm.productFilterProperty());
        ListView<ProductTotal> productList = new ListView<>(vm.visibleProducts());

        VBox top = new VBox(6, filters, validation,
                new HBox(8, bar, status, error));
        top.setPadding(new Insets(10));
        root.setTop(top);
        root.setCenter(table);
        root.setRight(new VBox(6, new Label("Top products"), search,
                productList));
        root.setBottom(new HBox(8, new Label("Chain revenue:"), total));
    }

    private static TableColumn<BranchSummary, String> column(String title,
            Function<BranchSummary, String> value) {
        TableColumn<BranchSummary, String> c = new TableColumn<>(title);
        c.setCellValueFactory(
                cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        return c;
    }

    public Parent root() {
        return root;
    }

    /** Text "2026-09" to YearMonth and back; null for unparsable input. */
    static final class YearMonthConverter extends StringConverter<YearMonth> {
        @Override
        public String toString(YearMonth m) {
            return m == null ? "" : m.toString();
        }

        @Override
        public YearMonth fromString(String s) {
            try {
                return s == null || s.isBlank() ? null
                        : YearMonth.parse(s.strip());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}