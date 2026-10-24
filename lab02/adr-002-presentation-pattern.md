# ADR-002: Presentation Pattern Selection (MVC vs. MVP)

## Status
Accepted

## Context
In Lab-01 and Lab-02, the team evaluated two architectural patterns for the sales report display:
1. **Model-View-Controller (MVC)**: `ReportView` directly listens to `SalesModel` updates, pulls transactions and summary data, and formats currency and status strings inside the view.
2. **Model-View-Presenter (MVP) with Passive View**: `FxReportScreen` implements a toolkit-free interface `ReportScreen` with zero domain awareness. The `ReportPresenter` handles user actions, listens to `SalesModel`, converts domain data into formatted display DTOs (`RowItem`), and pushes text directly to the screen.

Quality-attribute scenario evaluated:
* **Testability & Portability**: Ability to verify 100% of the UI formatting, error reporting, and status strings without spinning up a graphical UI toolkit or headless display server in CI/CD pipelines.

## Decision
The team keeps **Model-View-Presenter (MVP)** with a Passive View for Lab-03 and future iterations.

## Consequences
### Positive
* **100% Headless Testability**: Display formatting (such as `1,234.50`, receipt count, revenue text, average basket calculations) can be thoroughly tested via unit tests using `FakeScreen` without JavaFX or TestFX.
* **Separation of Concerns**: The view (`FxReportScreen`) has no model dependency, eliminating accidental business logic leaks into UI components.
* **Portability**: Implementing a CLI, Swing, Web, or Android UI only requires implementing the 4 methods of `ReportScreen`. The presenter and model remain completely unchanged.

### Negative / Trade-offs
* **Indirection & Boilerplate**: Adding a new column requires changing `RowItem`, `ReportPresenter`, and `FxReportScreen` (compared to just `ReportView` in MVC). However, the testability and architectural decoupling significantly outweigh this slight overhead.

