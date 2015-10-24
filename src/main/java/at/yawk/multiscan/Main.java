package at.yawk.multiscan;

import at.yawk.multiscan.scan.ConcurrentScanner;
import at.yawk.multiscan.scan.SaneScanner;
import at.yawk.multiscan.scan.Scanner;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author yawkat
 */
public class Main extends Application  {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Injector injector = Guice.createInjector(new MainModule());

        FXMLLoader loader = injector.getInstance(FXMLLoader.class);
        loader.setControllerFactory(type -> {
            try {
                Object o = type.newInstance();
                injector.injectMembers(o);
                return o;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

        Parent root = loader.load(Main.class.getResourceAsStream("scan_pane.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("at/yawk/multiscan/application.css");

        primaryStage.setTitle("Scanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
