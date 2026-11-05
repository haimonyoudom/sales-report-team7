package edu.itc.salesreport.ui;

/**
 * Runs work off the UI thread. Every JobListener callback is delivered
 * on the UI thread, so the ViewModel may update its properties there.
 */
public interface BackgroundJobs {

    <T> JobHandle submit(Work<T> work, JobListener<T> listener);

    /** The work itself: runs on a worker thread, touches no property. */
    @FunctionalInterface
    interface Work<T> {
        T run(ProgressSink progress) throws Exception;
    }

    /** Given to the work so it can report progress and see cancellation. */
    interface ProgressSink {
        void update(long done, long total);
        boolean isCancelled();
    }

    /** Outcome callbacks, always called on the UI thread. */
    interface JobListener<T> {
        void progress(double fraction);
        void succeeded(T value);
        void failed(Throwable error);
        void cancelled();
    }

    @FunctionalInterface
    interface JobHandle {
        void cancel();
    }
}