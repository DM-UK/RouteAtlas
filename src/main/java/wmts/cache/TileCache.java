package wmts.cache;

import wmts.Layer;
import wmts.Tile;

import java.awt.image.BufferedImage;

public interface TileCache {
    BufferedImage load(Layer layer, Tile tile);
    void save(Layer layer, Tile tile, BufferedImage image);
}