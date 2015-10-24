package at.yawk.multiscan.scan;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * @author yawkat
 */
public class FileScanner implements Scanner {
    private final Path source;

    public FileScanner(Path source) {
        this.source = source;
    }

    @Override
    public BufferedImage scan(ScanProgressListener progressListener) throws Exception {
        progressListener.progress("Loading File", ScanProgressListener.PROGRESS_UNKNOWN);
        try (InputStream in = Files.newInputStream(source)) {
            return ImageIO.read(in);
        }
    }
}
