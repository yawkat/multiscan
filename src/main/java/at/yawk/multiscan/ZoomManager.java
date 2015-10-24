package at.yawk.multiscan;

import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;

/**
 * @author yawkat
 */
public class ZoomManager {
    private final Node node;

    public ZoomManager(Node node) {
        this.node = node;
    }

    public void applyToHandle(Node handle) {
        handle.addEventFilter(ScrollEvent.SCROLL, event -> {
            double zoomDelta = event.getDeltaX() / event.getMultiplierX() +
                               event.getDeltaY() / event.getMultiplierY();

            double zoomFactor = Math.pow(1 + 0.1 * Math.signum(zoomDelta), Math.abs(zoomDelta));
            node.setScaleX(node.getScaleX() * zoomFactor);
            node.setScaleY(node.getScaleY() * zoomFactor);

            event.consume();
        });
    }
}
