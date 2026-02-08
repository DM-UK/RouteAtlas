package wmts.bounds;

import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;

import java.util.List;

public interface Bounds {
    default int getOrientation() {
        ProjCoordinate lower = getLowerCorner();
        ProjCoordinate upper = getUpperCorner();

        double width  = upper.x - lower.x;
        double height = upper.y - lower.y;

        if (width >= height) {
            return 0; // landscape
        } else {
            return 1; // portrait
        }
    }

    ProjCoordinate getLowerCorner();
    ProjCoordinate getUpperCorner();
    CoordinateReferenceSystem getCRS();
    Bounds transform(CoordinateReferenceSystem otherCRS);

    /** Returns all 4 corners as independent coordinates. */
    default List<ProjCoordinate> getAllCorners() {
        ProjCoordinate lower = getLowerCorner();
        ProjCoordinate upper = getUpperCorner();
        return List.of(
                new ProjCoordinate(lower.x, lower.y), // LL
                new ProjCoordinate(lower.x, upper.y), // UL
                new ProjCoordinate(upper.x, upper.y), // UR
                new ProjCoordinate(upper.x, lower.y)  // LR
        );
    }

    /** Creates a Bounds from an arbitrary set of points. */
    static Bounds fromPoints(List<ProjCoordinate> pts, CoordinateReferenceSystem crs) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (ProjCoordinate p : pts) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }

        return new ProjectionBounds(
                new ProjCoordinate(minX, minY),
                new ProjCoordinate(maxX, maxY), crs);
    }

    default double getWidth() {
        return getUpperCorner().x - getLowerCorner().x;
    }

    default double getHeight() {
        return getUpperCorner().y - getLowerCorner().y;
    }

    default boolean contains(double x, double y) {
        ProjCoordinate lower = getLowerCorner();
        ProjCoordinate upper = getUpperCorner();

        double minX = Math.min(lower.x, upper.x);
        double maxX = Math.max(lower.x, upper.x);
        double minY = Math.min(lower.y, upper.y);
        double maxY = Math.max(lower.y, upper.y);

        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
