package wmts;


import java.awt.geom.Point2D;

public record TilingScheme(Point2D origin, double initialResolution) {
    public static final int TILE_SIZE = 256;
}
