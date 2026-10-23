package edu.itc.salesreport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class UiIndependenceTest {
    @Test
    void corePackagesDoNotImportDesktopToolkits() throws IOException {
        Path source = Path.of("src", "main", "java", "edu", "itc", "salesreport");
        try (Stream<Path> files = Stream.of(
                source.resolve("model"), source.resolve("ingest"),
                source.resolve("engine"), source.resolve("ui"))
                .flatMap(this::javaFiles)) {
            files.forEach(this::assertToolkitFree);
        }
    }

    private Stream<Path> javaFiles(Path root) {
        try {
            Stream<Path> files = Files.walk(root);
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.toString().contains("\\ui\\fx\\"))
                    .onClose(files::close);
        } catch (IOException e) {
            throw new IllegalStateException("Could not inspect " + root, e);
        }
    }

    private void assertToolkitFree(Path file) {
        try {
            boolean clean = Files.readAllLines(file).stream()
                    .noneMatch(line -> line.startsWith("import javafx.")
                            || line.startsWith("import java.awt.")
                            || line.startsWith("import javax.swing."));
            assertTrue(clean, "Toolkit import in " + file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
    }
}
