package edu.itc.salesreport.ui;

import java.util.ArrayDeque;
import java.util.Deque;

/** Test double: jobs wait until the test runs them, on the test thread. */
public final class ManualJobs implements BackgroundJobs {

    private final Deque<Pending<?>> pending = new ArrayDeque<>();

    @Override
    public <T> JobHandle submit(Work<T> work, JobListener<T> listener) {
        Pending<T> job = new Pending<>(work, listener);
        pending.add(job);
        return () -> {
            if (pending.remove(job)) {
                job.cancelled = true;
                listener.cancelled();
            }
        };
    }

    public int pendingCount() {
        return pending.size();
    }

    public boolean hasPending() {
        return !pending.isEmpty();
    }

    /** Runs the oldest job to completion, as a worker thread would. */
    public void runNext() {
        if (!pending.isEmpty()) {
            pending.remove().run();
        }
    }

    public void runAll() {
        while (!pending.isEmpty()) {
            pending.remove().run();
        }
    }

    private static final class Pending<T> implements ProgressSink {
        private final Work<T> work;
        private final JobListener<T> listener;
        private boolean cancelled;

        Pending(Work<T> work, JobListener<T> listener) {
            this.work = work;
            this.listener = listener;
        }

        void run() {
            T value;
            try {
                value = work.run(this);
            } catch (Exception e) {
                listener.failed(e);
                return;
            }
            listener.succeeded(value);
        }

        @Override
        public void update(long done, long total) {
            listener.progress((double) done / total);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
