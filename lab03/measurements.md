# Lab 03, Task 4: Load measurements

## Environment

| Item | Value |
| ---- | ----- |
| CPU | AMD Ryzen 9 5900Hx |
| RAM | 16GB|
| OS | Windows 11 |
| JDK | Java(TM) SE Runtime Environment 25.0.1+8-LTS-27 |
| JavaFX | 25.0.4 |

## Method

- Data: `SalesDataGenerator 2026-08 5000 42 data` (93 files, 465,000 rows).
- Harness: `LoadBenchmarkApp --data=data --month=2026-08` (copied from the lab; the only change is that the source is `CsvSummarySource` instead of `SimpleReportEngine`, which this project does not have).
- Per mode (CallerThreadJobs, FxBackgroundJobs): 2 warm-up runs (discarded) and 5 measured runs.
- Load ms is the time from Generate until `loading` becomes false. Max frame gap is the longest gap between two `FrameGapMonitor` pulses, measured until 250 ms after the load.

## Results

| mode | run | load ms | max frame gap ms |
| ---- | --- | ------- | ---------------- |
| caller-thread | 1 | 574 | 665 |
| caller-thread | 2 | 554 | 635 |
| caller-thread | 3 | 531 | 590 |
| caller-thread | 4 | 508 | 596 |
| caller-thread | 5 | 509 | 576 |
| fx-task | 1 | 497 | 31 |
| fx-task | 2 | 515 | 38 |
| fx-task | 3 | 516 | 34 |
| fx-task | 4 | 501 | 42 |
| fx-task | 5 | 492 | 34 |

| mode | mean load ms | worst frame gap ms |
| ---- | ------------ | ------------------ |
| caller-thread | 535 | 665 |
| fx-task | 504 | 42 |

The load times are about the same (535 ms against 504 ms). The worst frame gap drops from 665 ms to 42 ms, which is below the 100 ms target.

## Manual observation (20,000 rows per day)

TODO: 3 to 5 sentences. During a load in each mode, say what happened when you typed in the product filter, moved the window and pressed Cancel.
