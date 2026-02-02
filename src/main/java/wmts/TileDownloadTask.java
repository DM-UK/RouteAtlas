package wmts;

import wmts.tilefetcher.TileFetcher;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TileDownloadTask {
    private final TileFetcher fetcher;
    private final ExecutorService executor;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public TileDownloadTask(TileFetcher fetcher, int maxThreads) {
        this.fetcher = fetcher;
        this.executor = Executors.newFixedThreadPool(maxThreads);
    }

    public  Map<Tile, BufferedImage> download(List<Tile> tiles, Layer layer, ProgressListener progress) throws WMTSException, CancellationException {
        CompletionService<Map.Entry<Tile, BufferedImage>> cs = createCompletionService(tiles, layer);
        Map<Tile, BufferedImage> result = new ConcurrentHashMap<>();

        progress.onStart(tiles.size());

        int completed = 0;

        try {
            while (completed < tiles.size()) {
                if (cancelled.get()) {
                    progress.onCancelled("TaskCancelled");
                    throw new CancellationException("Tile download was cancelled");
                }

                Future<Map.Entry<Tile, BufferedImage>> f = cs.take();
                Map.Entry<Tile, BufferedImage> entry = f.get();
                result.put(entry.getKey(), entry.getValue());
                completed++;
                progress.onProgress(completed, tiles.size());
            }

            return result;
        } catch (ExecutionException | InterruptedException e) {
            progress.onCancelled(e.getCause().getMessage());
            throw new WMTSException("Tile download failed: " + e.getCause().getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    private CompletionService<Map.Entry<Tile, BufferedImage>> createCompletionService(List<Tile> tiles, Layer layer) {
        CompletionService<Map.Entry<Tile, BufferedImage>> cs = new ExecutorCompletionService<>(executor);
        for (Tile tile : tiles) {
            cs.submit(() -> {
                BufferedImage img = fetcher.fetch(tile, layer);
                return Map.entry(tile, img);
            });
        }

        return cs;
    }

    public void cancel() {
        cancelled.set(true);
        executor.shutdownNow();

    }
}
