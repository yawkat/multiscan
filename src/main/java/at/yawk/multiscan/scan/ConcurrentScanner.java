package at.yawk.multiscan.scan;

import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Singleton;

/**
 * @author yawkat
 */
@Singleton
public class ConcurrentScanner implements Scanner {
    private final Lock lock = new ReentrantLock();
    private final Scanner delegate;

    public ConcurrentScanner(Scanner delegate) {
        this.delegate = delegate;
    }

    @Override
    public BufferedImage scan(ScanProgressListener progressListener) throws Exception {
        if (!lock.tryLock()) {
            progressListener.progress("Waiting for other job to finish", ScanProgressListener.PROGRESS_UNKNOWN);
            //noinspection LockAcquiredButNotSafelyReleased
            lock.lock();
        }
        try {
            return delegate.scan(progressListener);
        } finally {
            lock.unlock();
        }
    }
}
