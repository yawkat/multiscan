package at.yawk.multiscan;

import at.yawk.multiscan.scan.ConcurrentScanner;
import at.yawk.multiscan.scan.SaneScanner;
import at.yawk.multiscan.scan.Scanner;
import com.google.inject.AbstractModule;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import javafx.fxml.FXMLLoader;

/**
 * @author yawkat
 */
public class MainModule extends AbstractModule {
    private Scanner scanner = null;

    /**
     * Override the scanner for this module. The scanner must support concurrent scanning.
     */
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    protected void configure() {
        bind(Executor.class).toInstance(Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Pool thread #" + index.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        }));
        bind(Preferences.class).toInstance(Preferences.userNodeForPackage(Main.class));

        if (this.scanner == null) {
            SaneScanner saneScanner = new SaneScanner();
            requestInjection(saneScanner);
            bind(Scanner.class).toInstance(new ConcurrentScanner(saneScanner));
        } else {
            bind(Scanner.class).toInstance(this.scanner);
        }

        bind(FXMLLoader.class).toInstance(new FXMLLoader());
    }
}
