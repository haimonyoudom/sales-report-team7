package edu.itc.salesreport.model;

import java.math.BigDecimal;

public record ProductTotal(String sku, String productName,
        BigDecimal revenue, long quantity) {}
