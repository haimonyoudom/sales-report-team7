package edu.itc.salesreport.ui;


import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;

import edu.itc.salesreport.ui.Validation.Invalid;
import edu.itc.salesreport.ui.Validation.Valid;

/** Pure input rules: no UI toolkit, no I/O, clock injected. */
public final class InputRules {
    private static final Set<String> BRANCHES =
            Set.of(SalesModel.ALL, "PNH", "REP", "BTB");

    private InputRules() {}

    public static Validation<YearMonth> month(String text, Clock clock) {
        if (text == null || text.isBlank()) {
            return new Invalid<>("Enter a month as yyyy-MM, e.g. 2026-09");
        }
        String value = text.strip();
        try {
            YearMonth month = YearMonth.parse(value);
            if (month.isAfter(YearMonth.now(clock))) {
                return new Invalid<>(month + " is in the future");
            }
            return new Valid<>(month);
        } catch (DateTimeParseException e) {
            return new Invalid<>("'" + value + "' is not a month in the form yyyy-MM");
        }
    }

    public static Validation<String> branch(String text) {
        if (text == null) return new Invalid<>("Unknown branch 'null': use ALL, PNH, REP or BTB");
        String code = text.strip().toUpperCase(Locale.ROOT);
        if (BRANCHES.contains(code)) {
            return new Valid<>(code);
        }
        return new Invalid<>("Unknown branch '" + code + "': use ALL, PNH, REP or BTB");
    }
}