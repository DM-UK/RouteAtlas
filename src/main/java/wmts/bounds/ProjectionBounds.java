package wmts.bounds;

import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

import java.util.ArrayList;
import java.util.List;

public class ProjectionBounds implements Bounds {
    private final ProjCoordinate lower;
    private final ProjCoordinate upper;
    private final CoordinateReferenceSystem crs;

    public ProjectionBounds(ProjCoordinate lower, ProjCoordinate upper, CoordinateReferenceSystem crs) {
        this.lower = lower;
        this.upper = upper;
        this.crs = crs;
    }

    @Override
    public ProjCoordinate getLowerCorner() {
        return lower;
    }

    @Override public ProjCoordinate getUpperCorner() {
        return upper;
    }

    @Override
    public Bounds transform(CoordinateReferenceSystem otherCRS) {
        CoordinateTransform transform = new BasicCoordinateTransform(crs, otherCRS);

        List<ProjCoordinate> corners = getAllCorners();
        List<ProjCoordinate> transformed = new ArrayList<>(4);

        for (ProjCoordinate c : corners)
            transformed.add(transform.transform(c, new ProjCoordinate()));

        return Bounds.fromPoints(transformed, otherCRS);
    }
}