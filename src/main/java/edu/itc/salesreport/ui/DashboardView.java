package edu.itc.salesreport.ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

/** challenge skeleton: a compact dashboard bound to the SAME ViewModel. */
public final class DashboardView {

    private final VBox root = new VBox(8);

    public DashboardView(ReportViewModel vm) {
        Label month = new Label();
        month.textProperty().bind(Bindings.createStringBinding(
                () -> "Month: " + (vm.monthProperty().get() == null ? "" : vm.monthProperty().get()),
                vm.monthProperty()));
        Label total = new Label();
        total.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        total.textProperty().bind(vm.totalRevenueTextProperty());
        Label branches = new Label();
        // "3 branch(es) loaded": Bindings.size(vm.summaries())
        branches.textProperty().bind(Bindings.concat(
                Bindings.size(vm.summaries()), " branch(es) loaded"));
        ProgressIndicator spinner = new ProgressIndicator();
        // bind spinner progress and visibility to the ViewModel
        spinner.progressProperty().bind(vm.progressProperty());
        spinner.visibleProperty().bind(vm.loadingProperty());
        Button refresh = new Button("Refresh");
        // execute vm.generateCommand(), disable when not executable
        refresh.setOnAction(e -> vm.generateCommand().execute());
        refresh.disableProperty().bind(Bindings.not(vm.generateCommand().executableProperty()));
        root.getChildren().addAll(month, total, branches, spinner, refresh);
        root.setPadding(new Insets(12));
    }

    public Parent root() {
        return root;
    }
}
