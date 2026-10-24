package edu.itc.salesreport.ui.fx;

import java.nio.file.Path;
import java.time.Clock;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.mvp.ReportPresenter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ReportMvpApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        var model = new SalesModel(reader, aggregator);
        var screen = new FxReportScreen();
        var presenter = new ReportPresenter(
                model, screen, Path.of("data"), Clock.systemDefaultZone());
        screen.attach(presenter);

        var scene = new Scene(screen.root(), 960, 600);
        stage.setTitle("Sales Report (MVP)");
        stage.setScene(scene);
        stage.show();

        presenter.monthEntered("2026-09");

        Platform.runLater(() -> {
            ReportApp.captureScreenshot(scene, Path.of("lab02", "screenshots", "mvp-viewer.png"));
            if (Boolean.getBoolean("autoClose")) {
                stage.close();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

