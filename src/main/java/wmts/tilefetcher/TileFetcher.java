package wmts.tilefetcher;

import wmts.Layer;
import wmts.Tile;
import wmts.WMTSException;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface TileFetcher {
    BufferedImage fetch(Tile tile, Layer layer) throws WMTSException;
}