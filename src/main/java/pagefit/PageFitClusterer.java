package pagefit;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Modified k-means clustering algorithm, using midpoint rather than the mean as centroid   */
public class PageFitClusterer {
    private int iterationsBeforeAddingNewCluster;
    private Dimension2D pageDimensions;
    private List<Point2D> points;
    private List<Cluster> clusters = new ArrayList<>();

    public PageFitClusterer(int iterationsBeforeAddingNewCluster, List<Point2D> points, double paperWidth, double paperHeight) {
        this(iterationsBeforeAddingNewCluster, points, new Dimension2DDouble(paperWidth, paperHeight));
    }

    public PageFitClusterer(int iterationsBeforeAddingNewCluster, List<Point2D> points, Dimension2D pageDimensions) {
        this.iterationsBeforeAddingNewCluster = iterationsBeforeAddingNewCluster;
        this.points = points;
        this.pageDimensions = pageDimensions;
    }

    public List<Cluster> fit() {
        // Continue clustering until all points are within cluster pages
        while (!getPointsNotInPage().isEmpty()) {
            addCluster();

            for (int i = 0; i < iterationsBeforeAddingNewCluster; i++)
            {
                assignPointsToClusters();

                for (Cluster cluster : clusters){
                    cluster.calculateCentrePoint();
                    cluster.calculateBestOrientation(points);
                }
            }
        }

        // Sort clusters based on the average index of their points
        // NOTE: Not efficient (Comparator calculation done many times)
        List<Cluster> sortedClusters = clusters.stream()
                .sorted(Comparator.comparingDouble(c -> c.getAverageIndexOfPoints(points)))
                .toList();

        return sortedClusters;
    }

    /** Assigns each point to the nearest cluster based on Euclidean distance. Clears previous cluster assignments before reassignment. */
    private void assignPointsToClusters() {
        // Clear previous points from all clusters
        for (Cluster cluster : clusters)
            cluster.getPoints().clear();

        // Assign each point to the nearest cluster
        for (Point2D point : points) {
            Cluster nearestCluster = null;
            double nearestDistance = Double.MAX_VALUE;

            for (Cluster cluster : clusters) {
                double distance = cluster.calculateDistance(point);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestCluster = cluster;
                }
            }

            if (nearestCluster != null)
                nearestCluster.getPoints().add(point);
        }
    }

    /** Retrieves a list of points that are not contained within any of the clusters page. */
    private List<Point2D> getPointsNotInPage() {
        List<Point2D> pointsNotInPage = new ArrayList<>(points);

        for (Cluster cluster : clusters) {
            Rectangle2D pageRect = cluster.getPageBounds();

            if (pageRect != null)
                pointsNotInPage.removeIf(point -> pageRect.contains(point.getX(), point.getY()));
        }

        return pointsNotInPage;
    }

    /** Adds a new cluster to the list of clusters using a randomly selected point that is not within any existing cluster's page. */
    private void addCluster() {
        List<Point2D> pointsNotInPage = getPointsNotInPage();

        if (!pointsNotInPage.isEmpty()) {
            Point2D randomPoint = pointsNotInPage.get((int) (Math.random() * pointsNotInPage.size()));
            Cluster newCluster = new Cluster(randomPoint, pageDimensions);
            clusters.add(newCluster);
        }
    }

    public static class Dimension2DDouble extends Dimension2D {
        public double width;
        public double height;

        public Dimension2DDouble(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public double getWidth() {
            return width;
        }

        @Override
        public double getHeight() {
            return height;
        }

        @Override
        public void setSize(double width, double height) {
            this.width = width;
            this.height = height;
        }
    }
}