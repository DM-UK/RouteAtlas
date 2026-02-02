package wmts.bounds;

import org.locationtech.proj4j.*;
import org.locationtech.proj4j.proj.Projection;
import org.locationtech.proj4j.util.ProjectionMath;

public class GeographicBounds implements Bounds {
    private final ProjCoordinate lowerCoordinate;
    private final ProjCoordinate upperCoordinate;
    private final CoordinateReferenceSystem crs;
    private final double offsetXMetres;
    private final double offsetYMetres;

    public GeographicBounds(CoordinateReferenceSystem crs, ProjCoordinate lowerCoordinate, double offsetXMetres, double offsetYMetres){
        MetreOffsetTransform transform = new MetreOffsetTransform(crs, offsetXMetres, offsetYMetres);
        this.crs = crs;
        this.offsetXMetres = offsetXMetres;
        this.offsetYMetres = offsetYMetres;
        this.lowerCoordinate = lowerCoordinate;
        this.upperCoordinate = transform.transform(lowerCoordinate, new ProjCoordinate());
    }

    @Override
    public ProjCoordinate getLowerCorner() {
        return lowerCoordinate;
    }

    @Override
    public ProjCoordinate getUpperCorner() {
        return upperCoordinate;
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    @Override
    public Bounds transform(CoordinateReferenceSystem otherCRS) {
        CoordinateTransform transform = new BasicCoordinateTransform(crs, otherCRS);
        ProjCoordinate lowerCoordinateProjection = transform.transform(lowerCoordinate, new ProjCoordinate());
        return new GeographicBounds(otherCRS, lowerCoordinateProjection, offsetXMetres, offsetYMetres);
    }

    public static class MetreOffsetTransform implements CoordinateTransform {
        private final CoordinateReferenceSystem crs;
        private final double offsetXMetres;
        private final double offsetYMetres;

        public MetreOffsetTransform(CoordinateReferenceSystem crs, double offsetXMetres, double offsetYMetres){
            this.crs = crs;
            this.offsetXMetres = offsetXMetres;
            this.offsetYMetres = offsetYMetres;
        }

        @Override
        public CoordinateReferenceSystem getSourceCRS() {
            return crs;
        }

        @Override
        public CoordinateReferenceSystem getTargetCRS() {
            return crs;
        }

        @Override
        public ProjCoordinate transform(ProjCoordinate source, ProjCoordinate target) throws Proj4jException {
            double mppu = calculateMetresPerProjectionUnit(crs, source);
            double offsetXProjectionUnits = offsetXMetres * mppu;
            double offsetYProjectionUnits = offsetYMetres * mppu;
            target.setValue(source.x + offsetXProjectionUnits, source.y + offsetYProjectionUnits);
            return target;
        }

        private static double calculateMetresPerProjectionUnit(CoordinateReferenceSystem crs, ProjCoordinate projectedPoint) {
            // Step: move +1 unit in both X and Y directions in projected space
            ProjCoordinate projectedDiagonal = new ProjCoordinate(projectedPoint.x + 1, projectedPoint.y + 1);

            // Convert both points to geographic coordinates (lat/lon)
            Projection projection = crs.getProjection();
            ProjCoordinate geoPoint = new ProjCoordinate();
            ProjCoordinate geoDiagonal = new ProjCoordinate();
            projection.inverseProject(projectedPoint, geoPoint);
            projection.inverseProject(projectedDiagonal, geoDiagonal);

            // Compute geodesic distance along Earth's surface for the diagonal step
            double diagonalDistanceMetres = projection.getEquatorRadius() * ProjectionMath.greatCircleDistance(
                    Math.toRadians(geoPoint.x), Math.toRadians(geoPoint.y),
                    Math.toRadians(geoDiagonal.x), Math.toRadians(geoDiagonal.y)
            );

            // Convert diagonal distance to "per projection unit" scale (1 unit along X/Y ≈ diagonal / sqrt(2))
            return 1 / (diagonalDistanceMetres / Math.sqrt(2.0));
        }
    }
}
