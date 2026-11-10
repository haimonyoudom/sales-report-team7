package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Lab-03 Task 4: load time and UI responsiveness, caller thread
 * (frozen UI) versus FxBackgroundJobs (Task on a worker thread).
 * Arguments: --data=data --month=2026-08 --warmup=2 --runs=5
 */
public class LoadBenchmarkApp extends Application {

    private record Result(String mode, double loadMs, double maxGapMs) {
    }

    private final FrameGapMonitor monitor = new FrameGapMonitor();
    private final List<Result> results = new ArrayList<>();
    private final Deque<Runnable> plan = new ArrayDeque<>();
    private ExecutorService workers;
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Map<String, String> args = getParameters().getNamed();
        Path dataDir = Path.of(args.getOrDefault("data", "data"));
        YearMonth month = YearMonth.parse(args.getOrDefault("month", "2026-08"));
        int warmup = Integer.parseInt(args.getOrDefault("warmup", "2"));
        int runs = Integer.parseInt(args.getOrDefault("runs", "5"));

        // adapted: uses this project's CsvSummarySource instead of SimpleReportEngine
        SummarySource source = new CsvSummarySource(
                new CsvMonthReader(new CsvTransactionParser()),
                new DefaultSalesAggregator(), dataDir);
        workers = Executors.newVirtualThreadPerTaskExecutor();

        for (String mode : List.of("caller-thread", "fx-task")) {
            for (int i = 0; i < warmup + runs; i++) {
                boolean measured = i >= warmup;
                plan.add(() -> runOnce(mode, measured, source, month));
            }
        }
        plan.add(this::printAndExit);

        stage.setScene(new Scene(new StackPane(), 960, 600));
        stage.show();
        monitor.start();
        plan.poll().run();
    }

    private void runOnce(String mode, boolean measured, SummarySource src,
            YearMonth month) {
        BackgroundJobs jobs = mode.equals("caller-thread")
                ? new CallerThreadJobs() : new FxBackgroundJobs(workers);
        var vm = new ReportViewModel(src, jobs, Clock.systemDefaultZone());
        vm.monthProperty().set(month);
        stage.getScene().setRoot(new ReportView(vm).root());

        long t0 = System.nanoTime();
        monitor.reset();
        vm.loadingProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Boolean> obs,
                    Boolean was, Boolean now) {
                if (now) return;
                obs.removeListener(this);
                double loadMs = (System.nanoTime() - t0) / 1e6;
                // let a few pulses pass so a blocked frame is measured
                PauseTransition settle = new PauseTransition(
                        Duration.millis(250));
                settle.setOnFinished(e -> {
                    if (measured) {
                        results.add(new Result(mode, loadMs,
                                monitor.maxGapMillis()));
                    }
                    plan.poll().run();
                });
                settle.play();
            }
        });
        vm.generateCommand().execute();
    }

    private void printAndExit() {
        System.out.println("mode run load ms max frame gap ms");
        int[] n = new int[1];
        String prev = "";
        for (Result r : results) {
            n[0] = r.mode().equals(prev) ? n[0] + 1 : 1;
            prev = r.mode();
            System.out.println(String.format(Locale.ROOT,
                    "%-13s %4d %8.0f %17.0f",
                    r.mode(), n[0], r.loadMs(), r.maxGapMs()));
        }
        for (String mode : List.of("caller-thread", "fx-task")) {
            var rs = results.stream().filter(r -> r.mode().equals(mode))
                    .toList();
            System.out.println(String.format(Locale.ROOT,
                    "%-13s mean load %6.0f ms, worst frame gap %6.0f ms",
                    mode,
                    rs.stream().mapToDouble(Result::loadMs).average()
                            .orElse(0),
                    rs.stream().mapToDouble(Result::maxGapMs).max()
                            .orElse(0)));
        }
        monitor.stop();
        workers.shutdownNow();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

