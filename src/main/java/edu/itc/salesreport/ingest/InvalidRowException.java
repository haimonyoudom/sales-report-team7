package edu.itc.salesreport.ingest;

/** A CSV line that does not satisfy the canonical format. */
public class InvalidRowException extends Exception {
    private final String line;

    public InvalidRowException(String reason, String line) {
        super(reason);
        this.line = line;
    }

    public String line() { return line; }
}