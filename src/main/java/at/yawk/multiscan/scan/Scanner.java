package at.yawk.multiscan.scan;

import java.awt.image.BufferedImage;

/**
 * @author yawkat
 */
public interface Scanner {
    BufferedImage scan(ScanProgressListener progressListener) throws Exception;
}
