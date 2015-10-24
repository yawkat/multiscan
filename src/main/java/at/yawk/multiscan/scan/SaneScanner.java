package at.yawk.multiscan.scan;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author yawkat
 */
@Singleton
public class SaneScanner implements Scanner {
    private static final Pattern PROGRESS_PATTERN = Pattern.compile("Progress: (\\d+\\.\\d)%");

    @Inject Executor executor;

    @Override
    public BufferedImage scan(ScanProgressListener progressListener) throws Exception {
        progressListener.progress("Initializing", ScanProgressListener.PROGRESS_UNKNOWN);

        Process process = new ProcessBuilder()
                // todo: make these configurable
                .command("scanimage",
                         "-B",
                         "--scan-area", "A4",
                         "--x-resolution", "150",
                         "--y-resolution", "150",
                         "-p",
                         "-d", "epkowa:net:192.168.2.3",
                         "--format=png")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start();

        progressListener.progress("Scanning", ScanProgressListener.PROGRESS_UNKNOWN);

        CompletableFuture<BufferedImage> imageFuture = new CompletableFuture<>();
        Runnable task1 = () -> {
            try (InputStream in = process.getInputStream()) {
                imageFuture.complete(ImageIO.read(in));
            } catch (Throwable e) {
                imageFuture.completeExceptionally(e);
            }
        };
        executor.execute(task1);

        CompletableFuture<?> errorFuture = new CompletableFuture<>();
        Runnable task = () -> {
            try (InputStream in = process.getErrorStream()) {
                StringBuilder message = new StringBuilder();

                int i;
                while ((i = in.read()) != -1) {
                    switch (i) {
                    case '\r':
                        String messageString = message.toString();
                        Matcher matcher = PROGRESS_PATTERN.matcher(messageString);
                        if (matcher.matches()) {
                            progressListener.progress("Receiving Data", Double.parseDouble(matcher.group(1)) / 100.);
                        }
                        message.setLength(0);
                        break;
                    case '\n':
                        // todo
                        System.err.println(message);
                        message.setLength(0);
                        break;
                    default:
                        // assume ascii
                        message.append((char) i);
                        break;
                    }
                }

                errorFuture.complete(null);
            } catch (Throwable e) {
                errorFuture.completeExceptionally(e);
            }
        };
        executor.execute(task);

        int ret = process.waitFor();
        if (ret != 0) {
            throw new IOException("Return code " + ret);
        }

        errorFuture.get();
        return imageFuture.get();
    }
}
