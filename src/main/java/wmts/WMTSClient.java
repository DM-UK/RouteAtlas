package wmts;

import wmts.bounds.Bounds;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;

public interface WMTSClient {
    BufferedImage requestMap(MapRequest request) throws WMTSException, CancellationException;

    void cancelCurrentRequest();

    boolean isInProgress();

    default BufferedImage render(TileGrid grid, Map<Tile, BufferedImage> tiles, Bounds bounds, int zoom) {
        Dimension size = grid.imageSize(bounds, zoom);
        BufferedImage result = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        // Pixel coordinate of bounds top-left
        Point2D origin = grid.worldToPixel(bounds.getLowerCorner().x, bounds.getUpperCorner().y, zoom);

        // Draw each tile at the correct offset
        for (Map.Entry<Tile, BufferedImage> entry : tiles.entrySet()) {
            Tile tile = entry.getKey();
            BufferedImage tileImage = entry.getValue();

            Point offset = grid.tilePixelOffset(tile, origin, zoom);
            g.drawImage(tileImage, offset.x, offset.y, null);
        }

        g.dispose();
        return result;
    }
}