package wmts;

import wmts.bounds.Bounds;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public final class TileGrid {
    private final TilingScheme scheme;

    public TileGrid(TilingScheme scheme) {
        this.scheme = scheme;
    }

    public List<Tile> tilesInBounds(Bounds bounds, int zoom) {
        Tile topLeft = toTile(bounds.getLowerCorner().x, bounds.getUpperCorner().y, zoom);
        Tile bottomRight = toTile(bounds.getUpperCorner().x, bounds.getLowerCorner().y, zoom);

        int minX = Math.min(topLeft.x(), bottomRight.x());
        int maxX = Math.max(topLeft.x(), bottomRight.x());
        int minY = Math.min(topLeft.y(), bottomRight.y());
        int maxY = Math.max(topLeft.y(), bottomRight.y());

        List<Tile> tiles = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                tiles.add(new Tile(zoom, x, y));
            }
        }

        return tiles;
    }

    public Point2D worldToPixel(double easting, double northing, int zoom) {
        double mpp = metresPerPixel(zoom);
        double x = (easting - scheme.origin().getX()) / mpp;
        double y = (scheme.origin().getY() - northing) / mpp;

        return new Point2D.Double(x, y);
    }

    public Point2D tileToWorld(Tile tile) {
        double tileSizeMeters = TilingScheme.TILE_SIZE * metresPerPixel(tile.z());

        double x = scheme.origin().getX() + tile.x() * tileSizeMeters;
        double y = scheme.origin().getY() - tile.y() * tileSizeMeters;

        return new Point2D.Double(x, y);
    }

    public Point tilePixelOffset(Tile tile, Point2D boundsTopLeftPx, int zoom) {
        Point2D world = tileToWorld(tile);
        Point2D tilePx = worldToPixel(world.getX(), world.getY(), zoom);

        int x = (int) Math.round(tilePx.getX() - boundsTopLeftPx.getX());
        int y = (int) Math.round(tilePx.getY() - boundsTopLeftPx.getY());

        return new Point(x, y);
    }

    public Dimension imageSize(Bounds bounds, int zoom) {
        Point2D topLeft = worldToPixel(bounds.getLowerCorner().x, bounds.getUpperCorner().y, zoom);
        Point2D bottomRight = worldToPixel(bounds.getUpperCorner().x, bounds.getLowerCorner().y, zoom);

        int width = (int) Math.ceil(bottomRight.getX() - topLeft.getX());
        int height = (int) Math.ceil(bottomRight.getY() - topLeft.getY());

        return new Dimension(width, height);
    }

    public Tile toTile(double easting, double northing, int zoom) {
        double metresPerTile = TilingScheme.TILE_SIZE * metresPerPixel(zoom);

        int x = (int) Math.floor((easting - scheme.origin().getX()) / metresPerTile);
        int y = (int) Math.floor((scheme.origin().getY() - northing) / metresPerTile);

        return new Tile(zoom, x, y);
    }

    public double metresPerPixel(int zoom) {
        return scheme.initialResolution() / Math.pow(2, zoom);
    }
}