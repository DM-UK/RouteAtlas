package wmts;

import wmts.cache.FileSystemTileCache;
import wmts.cache.TileCache;
import wmts.tilefetcher.CachingTileFetcher;
import wmts.tilefetcher.*;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;

public final class DefaultWMTSClient implements WMTSClient{
    private final AtomicReference<TileDownloadTask> currentTask = new AtomicReference<>();
    private final Path tileCacheDir;

    public DefaultWMTSClient(Path tileCacheDir) {
        this.tileCacheDir = tileCacheDir;
    }

    @Override
    public BufferedImage requestMap(MapRequest request) throws WMTSException, CancellationException {
        if (isInProgress())
            cancelCurrentRequest();

        TileCache cache = new FileSystemTileCache(tileCacheDir, request.wmts().getName());
        TileGrid grid = new TileGrid(request.wmts().tilingScheme());
        List<Tile> tiles = grid.tilesInBounds(request.bounds(), request.zoom());
        TileFetcher httpFetcher = new HttpTileFetcher(request.wmts().tileSource());
        TileFetcher cachedFetcher = new CachingTileFetcher(httpFetcher, cache);
        TileDownloadTask task = new TileDownloadTask(cachedFetcher, request.wmts().maxConcurrentConnections());

        currentTask.set(task);

        try {
            Map<Tile, BufferedImage> downloadedTiles = task.download(tiles, request.layer(), request.progress());
            BufferedImage img = render(grid, downloadedTiles, request.bounds(), request.zoom());
            request.progress().onComplete(img);
            return img;
        } finally {
            task.cancel();
            currentTask.compareAndSet(task, null);
        }
    }

    @Override
    public void cancelCurrentRequest() {
        TileDownloadTask task = currentTask.getAndSet(null);

        if (task != null)
            task.cancel();
    }

    @Override
    public boolean isInProgress() {
        return currentTask.get() != null;
    }
}

