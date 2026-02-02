package wmts.tilefetcher;

import wmts.Layer;
import wmts.Tile;
import wmts.WMTSException;
import wmts.cache.TileCache;

import java.awt.image.BufferedImage;

/** TileFetcher that attempts to load a given tile from cache before using the delegate TileFetcher. */
public final class CachingTileFetcher implements TileFetcher {
    private final TileFetcher delegate;
    private final TileCache cache;

    public CachingTileFetcher(TileFetcher delegate, TileCache cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public BufferedImage fetch(Tile tile, Layer layer) throws WMTSException {
        BufferedImage cached = cache.load(layer, tile);

        if (cached != null)
            return cached;

        BufferedImage image = delegate.fetch(tile, layer);
        cache.save(layer, tile, image);
        return image;
    }
}
