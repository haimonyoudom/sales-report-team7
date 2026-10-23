package edu.itc.salesreport.ui;

public sealed interface Validation<T> {
    record Valid<T>(T value) implements Validation<T> {}
    record Invalid<T>(String message) implements Validation<T> {}
}