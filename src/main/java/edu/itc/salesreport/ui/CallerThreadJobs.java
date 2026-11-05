package edu.itc.salesreport.ui;

/**
 * Runs the work immediately on the calling thread. Used as the
 * "frozen UI" baseline in Lab-03 Task 4 and in simple tests.
 */
public final class CallerThreadJobs implements BackgroundJobs {

    @Override
    public <T> JobHandle submit(Work<T> work, JobListener<T> listener) {
        ProgressSink sink = new ProgressSink() {
            @Override
            public void update(long done, long total) {
                listener.progress((double) done / total);
            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        };
        try {
            listener.succeeded(work.run(sink));
        } catch (Exception e) {
            listener.failed(e);
        }
        return () -> { };
    }
}