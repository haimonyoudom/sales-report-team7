package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Seeded generator of small sample files: same seed, same files. */
public final class SalesDataGenerator {
    private record Product(String sku, String name, String category,
            String price) {}

    private static final List<Product> CATALOG = List.of(
        new Product("SKU-1001", "Jasmine Rice 5kg", "Grocery", "6.50"),
        new Product("SKU-1002", "Fish Sauce 700ml", "Grocery", "1.80"),
        new Product("SKU-1003", "Palm Sugar 1kg", "Grocery", "2.40"),
        new Product("SKU-2001", "Drinking Water 1.5L", "Beverages", "0.60"),
        new Product("SKU-2002", "Iced Coffee 250ml", "Beverages", "0.90"),
        new Product("SKU-2003", "Sugarcane Juice 1L", "Beverages", "1.60"),
        new Product("SKU-3001", "Laundry Powder 2kg", "Household", "5.90"),
        new Product("SKU-3002", "Dish Soap 500ml", "Household", "1.30"),
        new Product("SKU-3003", "Mosquito Coil 10pc", "Household", "1.10"),
        new Product("SKU-4001", "Toothpaste 150g", "Personal Care", "1.70"),
        new Product("SKU-4002", "Shampoo 400ml", "Personal Care", "3.80"),
        new Product("SKU-4003", "Sunscreen SPF50", "Personal Care", "7.50"),
        new Product("SKU-5001", "USB-C Cable 1m", "Electronics", "3.50"),
        new Product("SKU-5002", "Power Bank 10000mAh", "Electronics",
                "18.90"),
        new Product("SKU-5003", "LED Bulb 9W", "Electronics", "2.20"),
        new Product("SKU-6001", "Notebook A5", "Stationery", "0.80"),
        new Product("SKU-6002", "Ballpoint Pen 10pc", "Stationery", "1.50"),
        new Product("SKU-6003", "Sticky Notes", "Stationery", "1.20"));
    private static final List<String> BRANCHES = List.of("PNH", "REP", "BTB");

    /** Writes one file per branch and day; returns the number of rows. */
    public int generate(YearMonth month, int rowsPerDay, long seed,
            Path dataDir) throws IOException {
        var random = new Random(seed);
        Path dir = Files.createDirectories(dataDir.resolve(month.toString()));
        int total = 0;
        for (String branch : BRANCHES) {
            int receipt = 0;
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                LocalDate date = month.atDay(day);
                var lines = new ArrayList<String>();
                lines.add(CsvTransactionParser.HEADER);
                while (lines.size() <= rowsPerDay) {
                    receipt++;
                    int items = 1 + random.nextInt(4);
                    var pay = PaymentMethod.values()[random.nextInt(3)];
                    for (int i = 0; i < items && lines.size() <= rowsPerDay;
                            i++) {
                        var p = CATALOG.get(random.nextInt(CATALOG.size()));
                        int qty = 1 + random.nextInt(3);
                        String discount =
                                random.nextInt(10) == 0 ? "0.50" : "0.00";
                        lines.add(String.join(",", branch, date.toString(),
                                "%s-%06d".formatted(branch, receipt),
                                p.sku(), p.name(), p.category(),
                                Integer.toString(qty), p.price(), discount,
                                pay.name()));
                    }
                }
                total += lines.size() - 1;
                Files.write(dir.resolve(branch + "-" + date + ".csv"),
                        lines, StandardCharsets.UTF_8);
            }
        }
        return total;
    }

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of(args.length > 0 ? args[0] : "data");
        var generator = new SalesDataGenerator();
        for (String m : List.of("2026-08", "2026-09")) {
            var month = YearMonth.parse(m);
            int rows = generator.generate(month, 40,
                    202600L + month.getMonthValue(), dataDir);
            System.out.printf("%s: %d files, %d rows%n", month,
                    3 * month.lengthOfMonth(), rows);
        }
    }
}
