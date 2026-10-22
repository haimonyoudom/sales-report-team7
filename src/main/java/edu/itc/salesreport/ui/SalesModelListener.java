package edu.itc.salesreport.ui;

@FunctionalInterface
public interface SalesModelListener {
    void modelChanged(SalesEvent event);
}