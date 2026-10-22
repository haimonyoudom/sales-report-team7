package edu.itc.salesreport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Two branch files for 2026-09: three valid rows, one invalid row. */
public final class TestData {
    public static final String HEADER = "branch,date,receipt_no,sku,"
            + "product_name,category,quantity,unit_price,discount,"
            + "payment_method";

    private TestData() {}

    public static Path writeSample(Path dataDir) throws IOException {
        Path month = Files.createDirectories(dataDir.resolve("2026-09"));
        Files.writeString(month.resolve("PNH-2026-09-01.csv"), """
            %s
            PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR
            PNH,2026-09-01,PNH-000001,SKU-5002,Power Bank 10000mAh,Electronics,1,18.90,0.90,KHQR
            PNH,2026-09-01,PNH-000002,SKU-2001,Drinking Water 1.5L,Beverages,-3,0.60,0.00,CASH
            """.formatted(HEADER));
        Files.writeString(month.resolve("REP-2026-09-01.csv"), """
            %s
            REP,2026-09-01,REP-000001,SKU-4003,Sunscreen SPF50,Personal Care,3,7.50,1.50,CASH
            """.formatted(HEADER));
        return dataDir;
    }
}