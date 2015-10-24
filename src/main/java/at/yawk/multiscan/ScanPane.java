package at.yawk.multiscan;

import at.yawk.multiscan.scan.Scanner;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javax.imageio.ImageIO;
import javax.inject.Inject;

/**
 * @author yawkat
 */
public class ScanPane {
    private static final String SAVE_PATH_PATTERN = "savePathPattern";
    private static final String BEEP = "beep";
    private static final String DIALOG = "dialog";
    private static final ButtonType SCAN_NEXT_BUTTON_TYPE = new ButtonType("Scan Next");

    @Inject Scanner scanner;
    @Inject Preferences preferences;
    @Inject Executor executor;

    @FXML TextField path;
    @FXML Button rescan;
    @FXML ProgressBar scanningProgress;
    @FXML Text scanningProgressText;
    @FXML ImageView scanImage;
    @FXML ScrollPane scanImageContainer;
    @FXML ListView<ScanPage> pageList;
    @FXML TextField nextIndexField;
    @FXML CheckBox dialog;
    @FXML CheckBox beep;

    private final ObservableList<ScanPage> pages = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        String savePathPattern = preferences.get("savePathPattern", Paths.get("%02d.png").toAbsolutePath().toString());
        path.setText(savePathPattern);
        path.textProperty().addListener((observable, oldValue, newValue) -> {
            preferences.put(SAVE_PATH_PATTERN, newValue);
        });

        dialog.setSelected(preferences.getBoolean(DIALOG, true));
        dialog.selectedProperty().addListener((observable, oldValue, newValue) -> {
            preferences.putBoolean(DIALOG, newValue);
        });
        beep.setSelected(preferences.getBoolean(BEEP, true));
        beep.selectedProperty().addListener((observable, oldValue, newValue) -> {
            preferences.putBoolean(BEEP, newValue);
        });

        pageList.getSelectionModel().selectedItemProperty().addListener(observable -> {
            ScanPage page = pageList.getSelectionModel().getSelectedItem();
            if (page != null) {
                rescan.disableProperty().bind(page.getStatus().scanningProperty());
                scanningProgress.progressProperty().bind(
                        Bindings.when(page.getStatus().scanningProperty())
                                .then(page.getStatus().progressProperty())
                                .otherwise(0)
                );
                scanningProgress.disableProperty().bind(Bindings.not(page.getStatus().scanningProperty()));
                scanningProgressText.textProperty().bind(
                        Bindings.when(page.getStatus().scanningProperty())
                                .then(page.getStatus().statusProperty())
                                .otherwise("")
                );
                scanningProgressText.disableProperty().bind(Bindings.not(page.getStatus().scanningProperty()));
                scanImage.imageProperty().bind(page.imageProperty());
            } else {
                rescan.disableProperty().unbind();
                rescan.setDisable(true);
                scanningProgressText.textProperty().unbind();
                scanningProgressText.setText("");
                scanningProgressText.disableProperty().unbind();
                scanningProgressText.setDisable(true);
                scanningProgress.progressProperty().unbind();
                scanningProgress.setProgress(0);
                scanningProgress.disableProperty().unbind();
                scanningProgress.setDisable(true);
                scanImage.imageProperty().unbind();
                scanImage.setImage(null);
            }
        });

        pageList.setItems(pages);
        pageList.setCellFactory(param -> new ListCell<ScanPage>() {
            @Override
            protected void updateItem(ScanPage item, boolean empty) {
                super.updateItem(item, empty);
                PageThumbnail view;
                if (getGraphic() == null) {
                    setGraphic(view = new PageThumbnail());
                } else {
                    view = (PageThumbnail) getGraphic();
                }

                if (item != null) {
                    view.thumbnail.imageProperty().bind(item.imageProperty());
                    view.title.setText(item.getPath().getFileName().toString());
                } else {
                    view.thumbnail.imageProperty().unbind();
                    view.thumbnail.imageProperty().set(null);
                    view.title.setText(null);
                }
            }
        });

        new ZoomManager(scanImage).applyToHandle(scanImage);
    }

    void rescanPage(ScanPage page) {
        page.getStatus().setScanning(true);
        executor.execute(() -> {
            try {
                BufferedImage image = scanner.scan(page.getStatus());
                Path containingDir = page.getPath().getParent();
                if (!Files.exists(containingDir)) {
                    try {
                        Files.createDirectories(containingDir);
                    } catch (FileAlreadyExistsException ignored) {}
                }
                saveImage(page, image);

                WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                Platform.runLater(() -> {
                    page.setImage(fxImage);
                    if (preferences.getBoolean(BEEP, true)) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                    if (preferences.getBoolean(DIALOG, true)) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("Scan Complete");
                        alert.setContentText(page.getPath().getFileName().toString() + " was scanned successfully.");
                        alert.getButtonTypes().add(0, SCAN_NEXT_BUTTON_TYPE);
                        alert.showAndWait().ifPresent(pressed -> {
                            if (pressed.equals(SCAN_NEXT_BUTTON_TYPE)) {
                                addAndScanPage();
                            }
                        });
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
                // todo
            } finally {
                Platform.runLater(() -> page.getStatus().setScanning(false));
            }
        });
    }

    private void saveImage(ScanPage page, BufferedImage image) throws IOException {
        try (OutputStream os = Files.newOutputStream(page.getPath())) {
            ImageIO.write(image, "PNG", os);
        }
    }

    @FXML
    void addAndScanPage() {
        int index = Integer.parseInt(nextIndexField.getText());
        nextIndexField.setText(String.valueOf(index + 1));

        ScanPage newPage = new ScanPage(Paths.get(String.format(path.getText(), index)));
        pages.add(newPage);
        pageList.getSelectionModel().selectLast();
        rescanPage(newPage);
    }

    @FXML
    void rescanPage() {
        ScanPage page = pageList.getSelectionModel().getSelectedItem();
        if (page != null) {
            rescanPage(page);
        }
    }

    @FXML
    void rotateLeft() {
        rotateImage(-Math.PI / 2);
    }

    @FXML
    void rotateRight() {
        rotateImage(Math.PI / 2);
    }

    private void rotateImage(double theta) {
        executor.execute(() -> {
            ScanPage selectedPage = pageList.getSelectionModel().getSelectedItem();
            if (selectedPage == null) { return; }

            BufferedImage original = SwingFXUtils.fromFXImage(selectedPage.getImage(), null);
            BufferedImage rotated = new AffineTransformOp(
                    AffineTransform.getRotateInstance(
                            theta, (double) original.getWidth() / 2, (double) original.getHeight() / 2),
                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR
            ).filter(original, null);

            try {
                saveImage(selectedPage, rotated);
            } catch (IOException e) {
                e.printStackTrace();
                // todo
            }
            Platform.runLater(() -> selectedPage.setImage(SwingFXUtils.toFXImage(rotated, null)));
        });
    }

    private static class PageThumbnail extends BorderPane {
        private final ImageView thumbnail = new ImageView();
        private final Text title = new Text();

        public PageThumbnail() {
            thumbnail.setPreserveRatio(true);
            thumbnail.setFitHeight(150);
            setCenter(thumbnail);
            setBottom(title);
        }
    }
}
