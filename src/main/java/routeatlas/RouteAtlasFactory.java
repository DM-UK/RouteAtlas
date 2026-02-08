package routeatlas;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;
import pagefit.Cluster;
import pagefit.PageFitClusterer;
import utils.GeometryUtils;
import route.Route;
import route.RouteUtils;
import wmts.bounds.Bounds;
import wmts.bounds.GeographicBounds;
import wmts.bounds.ProjectionBounds;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

public class RouteAtlasFactory {
    private static final double OVERVIEW_PAGE_ZOOM_FACTOR = 1.08;

    /** Creates a RouteAtlas. Sections are determined using PageFit clustering to calculate page centres. */
    public static RouteAtlas create(Route route, ScaledPaper paper) {
        CoordinateReferenceSystem crs = new CRSFactory().createFromName("EPSG:32630");
        Route convertedRoute = RouteUtils.convertCRS(route, crs);

        List<MapPage> sectionPages = createSectionPages(convertedRoute, paper);
        MapPage overview = createOverviewPage(sectionPages, crs, paper);

        List<MapPage> allPages = new ArrayList<>();
        allPages.add(overview);
        allPages.addAll(sectionPages);

        return new RouteAtlas(convertedRoute, List.copyOf(allPages));
    }

    private static List<MapPage> createSectionPages(Route route, ScaledPaper paper) {
        CoordinateReferenceSystem crs = route.getCrs();
        List<MapPage> pages = new ArrayList<>();

        // clustering
        List<Point2D> points = new ArrayList<>(route.getWaypoints());
        PageFitClusterer clusterer = new PageFitClusterer(50, points, paper.getScaledWidth(), paper.getScaledHeight());
        List<Cluster> clusters = clusterer.fit();

        // loop through each cluster, using its origin and the papers scaled dimensions to create a MapPage
        for (Cluster cluster : clusters) {
            ProjCoordinate origin = new ProjCoordinate(cluster.getPageBounds().getMinX(), cluster.getPageBounds().getMinY());
            Bounds bounds;

            if (cluster.getOrientation() == PageFormat.LANDSCAPE)
                bounds = new GeographicBounds(route.getCrs(), origin, paper.getScaledHeight(), paper.getScaledWidth());
            else
                bounds = new GeographicBounds(crs, origin, paper.getScaledWidth(), paper.getScaledHeight());


            MapPage page = new MapPage(crs, bounds, paper, cluster.getOrientation());
            pages.add(page);
        }

        return List.copyOf(pages);
    }

    // creates a page which is the bounding box of all the sections bounds.
    private static MapPage createOverviewPage(List<MapPage> sectionPages, CoordinateReferenceSystem crs, ScaledPaper paper) {
        Bounds overviewBounds = calculateOverviewBounds(sectionPages, paper, crs);
        double scale = overviewBounds.getWidth() / paper.getPaperWidth();
        ScaledPaper overviewScaledPaper = new ScaledPaper(paper.getPaper(), scale);
        return new MapPage(crs, overviewBounds, overviewScaledPaper, overviewBounds.getOrientation());
    }

    private static Bounds calculateOverviewBounds(List<MapPage> maps, ScaledPaper paper, CoordinateReferenceSystem crs) {
        // every page corner of the sections
        List<Point2D> allPoints = new ArrayList<>();

        for (MapPage map : maps) {
            List<ProjCoordinate> corners = map.getBounds().getAllCorners();
            for (ProjCoordinate corner: corners)
                allPoints.add(new Point2D.Double(corner.x, corner.y));
        }

        Rectangle2D boundingBoxOfAllSections = GeometryUtils.calculateBoundingBox(allPoints);
        Rectangle2D exactBoundingBox = GeometryUtils.padToAspectRatio(boundingBoxOfAllSections, paper.getPaperWidth(),  paper.getPaperHeight());
        Rectangle2D scaledBoundingBox = GeometryUtils.scaleRectangle(exactBoundingBox, OVERVIEW_PAGE_ZOOM_FACTOR);

        ProjCoordinate lower = new ProjCoordinate(scaledBoundingBox.getMinX(), scaledBoundingBox.getMinY());
        ProjCoordinate upper = new ProjCoordinate(scaledBoundingBox.getMaxX(), scaledBoundingBox.getMaxY());
        return new ProjectionBounds(lower, upper, crs);
    }
}
