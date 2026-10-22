package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.SaleTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

/** The canonical parser for one line of a branch CSV file. */
public final class CsvTransactionParser implements TransactionParser {
    public static final String HEADER = "branch,date,receipt_no,sku,"
            + "product_name,category,quantity,unit_price,discount,"
            + "payment_method";
    public static final Set<String> BRANCHES = Set.of("PNH", "REP", "BTB");
    public static final Set<String> CATEGORIES = Set.of("Grocery",
            "Beverages", "Household", "Personal Care", "Electronics",
            "Stationery");

    @Override
    public SaleTransaction parse(String line) throws InvalidRowException {
        String[] f = line.split(",", -1);
        if (f.length != 10) {
            throw new InvalidRowException(
                    "expected 10 fields, found " + f.length, line);
        }
        try {
            String branch = oneOf(f[0], BRANCHES, "branch", line);
            LocalDate date = LocalDate.parse(f[1]);
            String category = oneOf(f[5], CATEGORIES, "category", line);
            int quantity = Integer.parseInt(f[6]);
            var unitPrice = new BigDecimal(f[7]);
            var discount = new BigDecimal(f[8]);
            var payment = PaymentMethod.valueOf(f[9]);
            if (quantity <= 0) {
                throw new InvalidRowException(
                        "quantity must be positive: " + quantity, line);
            }
            if (unitPrice.signum() < 0 || discount.signum() < 0) {
                throw new InvalidRowException("negative amount", line);
            }
            var gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
            if (discount.compareTo(gross) > 0) {
                throw new InvalidRowException(
                        "discount exceeds line amount", line);
            }
            return new SaleTransaction(branch, date, f[2], f[3], f[4],
                    category, quantity, unitPrice, discount, payment);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            // NumberFormatException is an IllegalArgumentException
            throw new InvalidRowException("bad value: " + e.getMessage(), line);
        }
    }

    private static String oneOf(String value, Set<String> allowed,
            String field, String line) throws InvalidRowException {
        if (!allowed.contains(value)) {
            throw new InvalidRowException(
                    "unknown " + field + " '" + value + "'", line);
        }
        return value;
    }
}