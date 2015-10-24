package at.yawk.multiscan;

import at.yawk.multiscan.scan.ScanProgressListener;
import javafx.application.Platform;
import javafx.beans.property.*;

/**
 * @author yawkat
 */
public class ScanStatus implements ScanProgressListener {
    private final StringProperty status = new SimpleStringProperty("");
    private final DoubleProperty progress = new SimpleDoubleProperty(PROGRESS_UNKNOWN);
    private final BooleanProperty scanning = new SimpleBooleanProperty(false);

    @Override
    public void progress(String stageName, double progress) {
        Platform.runLater(() -> {
            setStatus(stageName);
            setProgress(progress);
        });
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public boolean getScanning() {
        return scanning.get();
    }

    public BooleanProperty scanningProperty() {
        return scanning;
    }

    public void setScanning(boolean scanning) {
        this.scanning.set(scanning);
    }
}
