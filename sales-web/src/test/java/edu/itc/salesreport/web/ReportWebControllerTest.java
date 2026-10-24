package edu.itc.salesreport.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(controllers = ReportWebController.class,
        properties = "salesreport.data-dir=target/test-data")
class ReportWebControllerTest {
    @Autowired
    MockMvcTester mvc;

    @BeforeAll
    static void writeTestData() throws IOException {
        Path month = Path.of("target/test-data/2026-09");
        Files.createDirectories(month);
        String header = "branch,date,receipt_no,sku,product_name,category,quantity,unit_price,discount,payment_method";
        Files.writeString(month.resolve("PNH-2026-09-01.csv"), header + "\n"
                + "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR\n"
                + "PNH,2026-09-01,PNH-000001,SKU-5002,Power Bank 10000mAh,Electronics,1,18.90,0.90,KHQR\n");
        Files.writeString(month.resolve("REP-2026-09-01.csv"), header + "\n"
                + "REP,2026-09-01,REP-000001,SKU-4003,Sunscreen SPF50,Personal Care,3,7.50,1.50,CASH\n");
    }

    @Test
    void rendersSummaryOfOneBranch() {
        assertThat(mvc.get().uri("/report?month=2026-09&branch=PNH"))
                .hasStatusOk()
                .hasViewName("report")
                .bodyText().contains("Revenue USD 31.00", "Jasmine Rice 5kg", "Power Bank 10000mAh");
    }

    @Test
    void rendersCombinedReportForAllBranches() {
        assertThat(mvc.get().uri("/report?month=2026-09&branch=ALL"))
                .hasStatusOk()
                .hasViewName("report")
                .bodyText().contains("Revenue USD 52.00", "Jasmine Rice 5kg", "Sunscreen SPF50");
    }

    @Test
    void invalidMonthShowsValidationMessageInsteadOfServerError() {
        assertThat(mvc.get().uri("/report?month=2026-13&branch=PNH"))
                .hasStatusOk()
                .hasViewName("report")
                .bodyText().contains("2026-13&#39; is not a month in the form yyyy-MM");
    }

    @Test
    void invalidBranchShowsValidationMessageInsteadOfServerError() {
        assertThat(mvc.get().uri("/report?month=2026-09&branch=XYZ"))
                .hasStatusOk()
                .hasViewName("report")
                .bodyText().contains("Unknown branch &#39;XYZ&#39;");
    }
}
