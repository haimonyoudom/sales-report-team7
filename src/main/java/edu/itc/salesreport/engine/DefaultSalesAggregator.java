package edu.itc.salesreport.engine;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.ProductTotal;
import edu.itc.salesreport.model.SaleTransaction;

public class DefaultSalesAggregator implements SalesAggregator {

    private static final class ProductAccumulator {
        final String sku;
        final String productName;
        BigDecimal revenue = BigDecimal.ZERO.setScale(2);
        long quantity = 0;

        ProductAccumulator(String sku, String productName) {
            this.sku = sku;
            this.productName = productName;
        }

        void add(SaleTransaction t) {
            revenue = revenue.add(t.revenue());
            quantity += t.quantity();
        }

        ProductTotal toProductTotal() {
            return new ProductTotal(sku, productName, revenue, quantity);
        }
    }

    @Override
    public BranchSummary summarize(String branch, List<SaleTransaction> rows) {
        BigDecimal revenue = BigDecimal.ZERO.setScale(2);
        BigDecimal discounts = BigDecimal.ZERO.setScale(2);
        Set<String> receipts = new HashSet<>();
        Map<String, BigDecimal> byCategory = new TreeMap<>();
        Map<PaymentMethod, BigDecimal> byPayment = new EnumMap<>(PaymentMethod.class);
        Map<String, ProductAccumulator> bySku = new HashMap<>();

        for (SaleTransaction t : rows) {
            revenue = revenue.add(t.revenue());
            discounts = discounts.add(t.discount());
            receipts.add(t.receiptNo());
            byCategory.merge(t.category(), t.revenue(), BigDecimal::add);
            byPayment.merge(t.paymentMethod(), t.revenue(), BigDecimal::add);
            bySku.computeIfAbsent(t.sku(), k -> new ProductAccumulator(t.sku(), t.productName()))
                 .add(t);
        }

        List<ProductTotal> topProducts = bySku.values().stream()
                .map(ProductAccumulator::toProductTotal)
                .sorted(Comparator.comparing(ProductTotal::revenue, Comparator.reverseOrder())
                        .thenComparing(ProductTotal::sku))
                .limit(10)
                .toList();

        return new BranchSummary(branch, revenue, discounts, receipts.size(), byCategory, topProducts, byPayment);
    }
}