package at.yawk.multiscan;

import java.nio.file.Path;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * @author yawkat
 */
public class ScanPage {
    private final Path path;

    private final ScanStatus status = new ScanStatus();
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(null);

    public ScanPage(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public void setImage(Image image) {
        this.image.set(image);
    }
}
