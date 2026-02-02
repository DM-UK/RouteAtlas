package wmts.cache;

import wmts.Layer;
import wmts.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileSystemTileCache implements TileCache {
    private final Path root;

    public FileSystemTileCache(Path tileCacheDir, String providerName) {
        this.root = tileCacheDir.resolve(providerName);
    }

    /** Attempts to load a given tile from disc. Returns the object or null if file does not exist (or there is an IO exception). */
    @Override
    public BufferedImage load(Layer layer, Tile tile) {
        Path path = tilePath(layer, tile);

        if (!Files.exists(path))
            return null;

        try {
            return ImageIO.read(path.toFile());
        } catch (IOException e) {
            System.err.println("Failed to read cached tile: " + path);
            return null;
        }
    }

    /** Writes a given tile to PNG file. */
    @Override
    public void save(Layer layer, Tile tile, BufferedImage image) {
        Path path = tilePath(layer, tile);

        try {
            Files.createDirectories(path.getParent());
            ImageIO.write(image, "png", path.toFile());
        } catch (IOException e) {
            System.err.println("Failed to write cached tile: " + path);
        }
    }

    //tile_cache\name\layer\z\x\y.png
    private Path tilePath(Layer layer, Tile tile) {
        return root.resolve(layer.name())
                   .resolve(Integer.toString(tile.z()))
                   .resolve(Integer.toString(tile.x()))
                   .resolve(tile.y() + ".png");
    }
}
