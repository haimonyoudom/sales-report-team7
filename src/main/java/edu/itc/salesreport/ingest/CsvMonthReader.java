package edu.itc.salesreport.ingest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import edu.itc.salesreport.model.SaleTransaction;

public class CsvMonthReader {
    private static final Pattern FILE_NAME = Pattern.compile("(PNH|REP|BTB)-\\d{4}-\\d{2}-\\d{2}\\.csv");

    private final TransactionParser parser;

    public CsvMonthReader(TransactionParser parser) {
        this.parser = parser;
    }

    public MonthData read(Path dataDir, YearMonth month) throws IOException {
        Path monthDir = dataDir.resolve(month.toString());
        if (!Files.isDirectory(monthDir)) {
            throw new NoSuchFileException(monthDir.toString());
        }

        List<Path> files;
        try (Stream<Path> stream = Files.list(monthDir)) {
            files = stream
                    .filter(p -> FILE_NAME.matcher(p.getFileName().toString()).matches())
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }

        List<SaleTransaction> rows = new ArrayList<>();
        List<String> rejected = new ArrayList<>();

        for (Path file : files) {
            List<String> lines = Files.readAllLines(file);
            String name = file.getFileName().toString();
            for (int i = 1; i < lines.size(); i++) { // index 0 is the header
                try {
                    rows.add(parser.parse(lines.get(i)));
                } catch (InvalidRowException e) {
                    rejected.add(name + ":" + (i + 1) + " " + e.getMessage());
                }
            }
        }
        return new MonthData(month, rows, rejected);
    }
}
