package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** One CSV line of a branch's daily export. */
public record SaleTransaction(String branch, LocalDate date, String receiptNo,
        String sku, String productName, String category, int quantity,
        BigDecimal unitPrice, BigDecimal discount, PaymentMethod paymentMethod) {

    /** quantity x unit price - discount, scale 2, HALF_UP. */
    public BigDecimal revenue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}