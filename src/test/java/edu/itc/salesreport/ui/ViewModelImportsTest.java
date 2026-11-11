package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Architecture guard: the ViewModel may use javafx.base only. */
class ViewModelImportsTest {

    private static final List<String> FORBIDDEN = List.of(
            "import javafx.scene", "import javafx.stage",
            "import javafx.application", "import javafx.concurrent",
            "import java.awt.", "import javax.swing.");

    @Test
    void viewModelUsesJavafxBaseOnly() throws IOException {
        Path src = Path.of("src/main/java/edu/itc/salesreport/ui",
                "ReportViewModel.java");
        List<String> bad = Files.readAllLines(src).stream()
                .filter(line -> FORBIDDEN.stream().anyMatch(line::startsWith))
                .toList();
        assertEquals(List.of(), bad);
    }
}
