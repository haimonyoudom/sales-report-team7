package edu.itc.salesreport.ui.fx;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import javax.imageio.ImageIO;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.ReportController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

public class ReportApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        var model = new SalesModel(reader, aggregator);
        var view = new ReportView(model);
        var controller = new ReportController(
                model, Path.of("data"), Clock.systemDefaultZone(), view);
        view.bind(controller);

        var scene = new Scene(view.root(), 960, 600);
        stage.setTitle("Sales Report");
        stage.setScene(scene);
        stage.show();

        controller.monthEntered("2026-09");

        Platform.runLater(() -> {
            captureScreenshot(scene, Path.of("lab02", "screenshots", "mvc-viewer.png"));
            if (Boolean.getBoolean("autoClose")) {
                stage.close();
            }
        });
    }

    public static void captureScreenshot(Scene scene, Path outputPath) {
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            WritableImage image = scene.snapshot(null);
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            PixelReader reader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bImage.setRGB(x, y, reader.getArgb(x, y));
                }
            }
            ImageIO.write(bImage, "png", outputPath.toFile());
        } catch (IOException e) {
            System.err.println("Could not capture screenshot: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
