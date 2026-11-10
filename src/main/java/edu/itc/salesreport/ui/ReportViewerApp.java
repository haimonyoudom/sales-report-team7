package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader; 
import edu.itc.salesreport.ingest.CsvTransactionParser;
import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Composition root: the only class that knows every concrete type. */
public class ReportViewerApp extends Application {

    private ExecutorService executor;

    @Override
    public void start(Stage stage) {
        Path dataDir = Path.of(getParameters().getNamed()
                .getOrDefault("data", "data"));
        // adapt YOUR Lab-02 model here; the model code stays unchanged
        SummarySource source = new CsvSummarySource(
        new CsvMonthReader(new CsvTransactionParser()),
        new DefaultSalesAggregator(), dataDir);
        // Task 2: CallerThreadJobs (UI still freezes, like Lab-02)
        // Task 4: new FxBackgroundJobs(workers)
        executor = Executors.newVirtualThreadPerTaskExecutor();
        var vm = new ReportViewModel(source, new FxBackgroundJobs(executor),
                Clock.systemDefaultZone());
        stage.setScene(new Scene(new ReportView(vm).root(), 960, 600));
        stage.setTitle("Angkor Mart · Monthly report (MVVM)");
        stage.show();
    }

    @Override
    public void stop() {
        if (executor != null)
            executor.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}