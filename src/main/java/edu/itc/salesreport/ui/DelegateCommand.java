package edu.itc.salesreport.ui;

import java.util.Objects;
import javafx.beans.value.ObservableBooleanValue;

/** Command that delegates to a Runnable and guards it with a condition. */
public final class DelegateCommand implements Command {

    private final Runnable action;
    private final ObservableBooleanValue executable;

    public DelegateCommand(Runnable action,
                           ObservableBooleanValue executable) {
        this.action = Objects.requireNonNull(action);
        this.executable = Objects.requireNonNull(executable);
    }

    @Override
    public ObservableBooleanValue executableProperty() {
        return executable;
    }

    @Override
    public void execute() {
        // ignore calls while disabled (key shortcuts, double clicks)
        if (executable.get()) {
            action.run();
        }
    }
}