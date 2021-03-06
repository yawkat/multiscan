package at.yawk.multiscan;

import at.yawk.multiscan.scan.FileScanner;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.nio.file.Paths;
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
        MainModule mainModule = new MainModule();

        String fileParameter = getParameters().getNamed().get("file");
        if (fileParameter != null) {
            mainModule.setScanner(new FileScanner(Paths.get(fileParameter)));
        }

        Injector injector = Guice.createInjector(mainModule);

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

        primaryStage.setTitle("Scanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
