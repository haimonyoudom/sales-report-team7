package edu.itc.salesreport.ui;

import javafx.animation.AnimationTimer;

/**
 * Measures UI responsiveness: the longest gap between two pulses of the
 * FX Application Thread (about 16.7 ms at 60 Hz when the UI is idle).
 */
public final class FrameGapMonitor extends AnimationTimer {

    private long last;
    private long maxGapNanos;
    private long frames;

    @Override
    public void handle(long now) {
        if (last != 0) {
            maxGapNanos = Math.max(maxGapNanos, now - last);
        }
        last = now;
        frames++;
    }

    public void reset() {
        last = 0;
        maxGapNanos = 0;
        frames = 0;
    }

    public double maxGapMillis() {
        return maxGapNanos / 1_000_000.0;
    }

    public long frames() {
        return frames;
    }
}

