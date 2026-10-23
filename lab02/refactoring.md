# Fat controller refactoring

The legacy `FatReportController` mixed several responsibilities in one JavaFX
event handler:

- It performed filesystem I/O, so the handler could not be tested without a
  display and had no clear resource ownership for `Files.list`.
- It parsed CSV fields with positional string operations, duplicating the
  canonical parser and accepting malformed rows inconsistently.
- It calculated revenue directly, duplicating model/aggregator business rules.
- It filtered by the combo-box value, coupling domain behavior to JavaFX
  controls and making branch behavior difficult to reuse.
- It caught every `Exception`, hiding validation, I/O, and programming errors
  behind the same `"Error"` status.
- It updated a label directly instead of reporting structured errors through
  `ErrorDisplay`, so users received no useful failure reason.
- It accepted any month directory name rather than validating a `YearMonth`
  and rejecting future months.

With the broken row present in the 2026-09 PNH file, the legacy handler
silently calculated `26715.40` for ALL: it subtracted the malformed row's
`-1 * 6.50` as `-6.50`. The refactored model rejects that row, retains the
correct revenue `26721.90`, and reports `1 rejected`.

The original handler listing was 48 lines. The refactored compatibility shell
is 19 lines, and its `onLoad()` contains one delegation statement:

```java
controller.monthEntered(monthField.getText());
```

The parser, validation rules, model, and controller can now be tested without
starting JavaFX.
