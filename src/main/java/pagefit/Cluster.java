package pagefit;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

public class Cluster {
    private final Dimension2D pageDimensions;
    private List<Point2D> points = new ArrayList();
    private Rectangle2D pageBounds;
    private Point2D centre;
    private int orientation;

    public Cluster(Point2D centre, Dimension2D pageDimensions) {
        this.centre = centre;
        this.pageDimensions = pageDimensions;
        this.orientation = PageFormat.LANDSCAPE;
        points.add(centre);
        pageBounds = createBounds(orientation);
    }

    private Rectangle2D createBounds(int orientation) {
        double width = pageDimensions.getWidth();
        double height = pageDimensions.getHeight();

        if (orientation == PageFormat.LANDSCAPE) {
            double temp = width;
            width = height;
            height = temp;
        }

        return new Rectangle2D.Double(
                centre.getX() - width / 2,
                centre.getY() - height / 2,
                width,
                height
        );
    }

    public void calculateCentrePoint() {
        this.centre = Rectangle2DUtils.calculateBoundingBoxCentre(points);
        pageBounds = createBounds(orientation);
    }

    public double calculateDistance(Point2D point) {
        return point.distanceSq(centre);
    }

    public double getAverageIndexOfPoints(List<Point2D> allCoordinates) {
        double sum = 0;

        for (Point2D clusterCoordinate: points)
            sum = sum + allCoordinates.indexOf(clusterCoordinate);

        return sum / points.size();
    }

    public Point2D getCentre() {
        return centre;
    }

    public int getOrientation() {
        return orientation;
    }

    public List<Point2D> getPoints() {
        return points;
    }

    public Rectangle2D getPageBounds() {
        return pageBounds;
    }

    public void calculateBestOrientation(List<Point2D> points) {
        int newOrientation;

        if (orientation == PageFormat.LANDSCAPE)
            newOrientation = PageFormat.PORTRAIT;
        else
            newOrientation = PageFormat.LANDSCAPE;

        Rectangle2D rotation = createBounds(newOrientation);

        int pointsWithinOriginalOrientation = 0;
        int pointsWithinRotation = 0;

        for (Point2D p: points){
            if (pageBounds.contains(p))
                pointsWithinOriginalOrientation++;

            if (rotation.contains(p))
                pointsWithinRotation++;
        }

        if (pointsWithinRotation > pointsWithinOriginalOrientation) {
            pageBounds = rotation;
            orientation = newOrientation;
        }
    }
}
