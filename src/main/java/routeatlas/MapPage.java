package routeatlas;

import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;
import wmts.bounds.Bounds;

/** Represent a map page and its bounds in the specified CRS */
public class MapPage {
    private final CoordinateReferenceSystem crs;
    private final Bounds bounds;
    private final ScaledPaper paper;
    private final int orientation;

    public MapPage(CoordinateReferenceSystem crs, Bounds bounds, ScaledPaper paper, int orientation) {
        this.crs = crs;
        this.bounds = bounds;
        this.paper = paper;
        this.orientation = orientation;
    }

    public int getOrientation() {
        return orientation;
    }

    public ProjCoordinate getOrigin() {
        return bounds.getLowerCorner();
    }

    public ScaledPaper getScaledPaper() {
        return paper;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public MapPage transform(CoordinateReferenceSystem otherCRS) {
        Bounds transformedBounds = bounds.transform(otherCRS);
        return new MapPage(crs, transformedBounds, paper, orientation);
    }
}
