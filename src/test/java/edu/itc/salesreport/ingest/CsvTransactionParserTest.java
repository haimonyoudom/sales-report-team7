package edu.itc.salesreport.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.SaleTransaction;

class CsvTransactionParserTest {
    private final CsvTransactionParser parser = new CsvTransactionParser();

    @Test
    void canonicalRowHasExpectedRevenueAndPaymentMethod() throws Exception {
        SaleTransaction sale = parser.parse(
                "PNH,2026-09-15,PNH-000001,SKU-1001,Jasmine Rice 5kg,"
                        + "Grocery,2,6.50,0.00,KHQR");

        assertEquals(new BigDecimal("13.00"), sale.revenue());
        assertEquals(PaymentMethod.KHQR, sale.paymentMethod());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "XXX,2026-09-15,R,SKU,Product,Grocery,1,2.00,0.00,KHQR",
        "PNH,not-a-date,R,SKU,Product,Grocery,1,2.00,0.00,KHQR",
        "PNH,2026-09-15,R,SKU,Product,Grocery,0,2.00,0.00,KHQR",
        "PNH,2026-09-15,R,SKU,Product,Grocery,1,2.00,3.00,KHQR",
        "PNH,2026-09-15,R,SKU,Product,Unknown,1,2.00,0.00,KHQR",
        "PNH,2026-09-15,R,SKU,Product,Grocery,1,2.00,0.00,UNKNOWN",
        "PNH,2026-09-15,R,SKU,Product,Grocery,1,2.00,0.00"
    })
    void invalidRowsAreRejected(String row) {
        assertThrows(InvalidRowException.class, () -> parser.parse(row));
    }
}
