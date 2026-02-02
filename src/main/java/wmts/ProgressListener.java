package wmts;

import java.awt.image.BufferedImage;

public interface ProgressListener {
    void onStart(int totalTiles);
    void onProgress(int completed, int total);
    void onCancelled(String reason);
    void onComplete(BufferedImage img);
}
