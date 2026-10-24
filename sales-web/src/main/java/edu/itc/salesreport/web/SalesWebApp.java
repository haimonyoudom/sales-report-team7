package edu.itc.salesreport.web;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.engine.SalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;

@SpringBootApplication
public class SalesWebApp {
    public static void main(String[] args) {
        SpringApplication.run(SalesWebApp.class, args);
    }

    @Bean
    CsvMonthReader csvMonthReader() {
        return new CsvMonthReader(new CsvTransactionParser());
    }

    @Bean
    SalesAggregator salesAggregator() {
        return new DefaultSalesAggregator();
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
