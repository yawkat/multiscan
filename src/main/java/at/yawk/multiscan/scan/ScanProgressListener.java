package at.yawk.multiscan.scan;

/**
 * @author yawkat
 */
public interface ScanProgressListener {
    double PROGRESS_UNKNOWN = -1;

    /**
     * Notify this listener of progress.
     *
     * @param progress The progress of this stage as a float between {@code 0} and {@code 1} or {@link
     *                 #PROGRESS_UNKNOWN} if the progress is unknown.
     */
    void progress(String stageName, double progress);
}
