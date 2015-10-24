package at.yawk.multiscan;

import at.yawk.multiscan.scan.ConcurrentScanner;
import at.yawk.multiscan.scan.SaneScanner;
import at.yawk.multiscan.scan.Scanner;
import com.google.inject.AbstractModule;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javafx.fxml.FXMLLoader;

/**
 * @author yawkat
 */
public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Executor.class).toInstance(Executors.newCachedThreadPool());
        bind(Preferences.class).toInstance(Preferences.userNodeForPackage(Main.class));

        SaneScanner saneScanner = new SaneScanner();
        requestInjection(saneScanner);
        bind(Scanner.class).toInstance(new ConcurrentScanner(saneScanner));
        bind(FXMLLoader.class).toInstance(new FXMLLoader());
    }
}
