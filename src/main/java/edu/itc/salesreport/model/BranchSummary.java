package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record BranchSummary(String branch, BigDecimal revenue,
        BigDecimal discounts, long receipts,
        Map<String, BigDecimal> revenueByCategory,
        List<ProductTotal> topProducts,
        Map<PaymentMethod, BigDecimal> revenueByPayment) {

    public BranchSummary {
        revenueByCategory = Collections.unmodifiableMap(
                new TreeMap<>(revenueByCategory));
        topProducts = List.copyOf(topProducts);
        var byPayment = new EnumMap<PaymentMethod, BigDecimal>(PaymentMethod.class);
        byPayment.putAll(revenueByPayment);
        revenueByPayment = Collections.unmodifiableMap(byPayment);
    }

    /** Revenue per receipt, 0.00 when there are no receipts. */
    public BigDecimal averageBasket() {
        return receipts == 0
                ? BigDecimal.ZERO.setScale(2)
                : revenue.divide(BigDecimal.valueOf(receipts), 2,
                        RoundingMode.HALF_UP);
    }
}