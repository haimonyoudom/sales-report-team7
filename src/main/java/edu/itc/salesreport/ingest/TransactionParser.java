package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

@FunctionalInterface
public interface TransactionParser {
    SaleTransaction parse(String line) throws InvalidRowException;
}