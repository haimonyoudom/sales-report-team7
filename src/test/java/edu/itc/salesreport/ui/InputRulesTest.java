package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.itc.salesreport.ui.Validation.Invalid;
import edu.itc.salesreport.ui.Validation.Valid;

class InputRulesTest {
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-27T03:00:00Z"),
            ZoneId.of("Asia/Phnom_Penh"));

    @Test
    void trimmedMonthIsValid() {
        assertEquals(new Valid<>(YearMonth.of(2026, 9)),
                InputRules.month(" 2026-09 ", CLOCK));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "", " ", "2026/09", "2026-9", "Sept", "2026-10"
    })
    void invalidMonthsHaveExpectedMessages(String text) {
        String message = switch (text) {
            case "", " " -> "Enter a month as yyyy-MM, e.g. 2026-09";
            case "2026/09", "2026-9", "Sept" ->
                "'" + text + "' is not a month in the form yyyy-MM";
            case "2026-10" -> "2026-10 is in the future";
            default -> throw new AssertionError(text);
        };
        assertEquals(new Invalid<>(message), InputRules.month(text, CLOCK));
    }

    @Test
    void branchIsTrimmedAndNormalised() {
        assertEquals(new Valid<>("REP"), InputRules.branch(" rep "));
        assertEquals(new Valid<>("ALL"), InputRules.branch("ALL"));
    }

    @Test
    void unknownBranchIsRejected() {
        Invalid<String> invalid = assertInstanceOf(Invalid.class,
                InputRules.branch(" phnom "));
        assertEquals("Unknown branch 'PHNOM': use ALL, PNH, REP or BTB",
                invalid.message());
    }
}
