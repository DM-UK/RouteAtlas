package route;

import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;
import wmts.bounds.Bounds;

import java.awt.geom.Point2D;
import java.util.List;

public class RouteUtils
{
    public static final double KM_TO_MILE = 1.609344;

    public static Route convertCRS(Route oldRoute, CoordinateReferenceSystem crs){
        CoordinateTransform transform = new BasicCoordinateTransform(oldRoute.getCrs(), crs);
        Route route = new Route(oldRoute.getRouteid(), crs);

        for (WayPoint waypoint: oldRoute.getWaypoints()) {
            ProjCoordinate projectedCoordinate = transform.transform(new ProjCoordinate(waypoint.x, waypoint.y), new ProjCoordinate());
            Point2D.Double p = new Point2D.Double(projectedCoordinate.x, projectedCoordinate.y);
            route.addWayPoint(p, waypoint.getElevation(), waypoint.getTotalDistance());
        }

        return route;
    }

    public static Route createSegmentedRoute(Route route, double startDistance, double interval) {
        List<WayPoint> waypoints = route.getWaypoints();
        Route newRoute = new Route(route.getRouteid(), route.getCrs());

        double nextInterval = startDistance;
        WayPoint previous;
        WayPoint current;

        for (int i = 1; i < waypoints.size(); i++)
        {
            previous = waypoints.get(i - 1);
            current = waypoints.get(i);

            while (previous.getTotalDistance() <= nextInterval && current.getTotalDistance() >= nextInterval)
            {
                double segmentDistance = current.getTotalDistance() - previous.getTotalDistance();
                double fraction = (nextInterval - previous.getTotalDistance()) / segmentDistance;
                double interpolatedX = previous.getX() + fraction * (current.getX() - previous.getX());
                double interpolatedY = previous.getY() + fraction * (current.getY() - previous.getY());
                double interpolatedElevation = previous.getElevation() + fraction * (current.getElevation() - previous.getElevation());
                double interpolatedTotalAscent = previous.getTotalAscent()+ fraction * (current.getTotalAscent() - previous.getTotalAscent());
                double interpolatedTotalDescent = previous.getTotalDescent() + fraction * (current.getTotalDescent() - previous.getTotalDescent());
                Point2D interpolatedCoordinate = new Point2D.Double(interpolatedX, interpolatedY);
                WayPoint wp = new WayPoint(interpolatedCoordinate, newRoute.getWaypoints().size(), interpolatedElevation, nextInterval, interpolatedTotalAscent, interpolatedTotalDescent);
                newRoute.addWayPoint(wp);
                nextInterval = nextInterval + interval;
            }
        }

        newRoute.addWayPoint(waypoints.getLast());

        return newRoute;
    }

    public static Route createSegmentedRoute(Route route, double interval) {
        return createSegmentedRoute(route, 0, interval);
    }

    public static WayPoint getFirstPointWithinBounds(Route route, Bounds bounds) {
        List<WayPoint> waypoints = route.getWaypoints();

        for (int i = 0; i < waypoints.size(); i++) {
            WayPoint wp = waypoints.get(i);
            if (bounds.contains(wp.x, wp.y)) {
                return wp;
            }
        }

        return null;
    }

    public static WayPoint getLastPointWithinBounds(Route route, Bounds bounds) {
        List<WayPoint> waypoints = route.getWaypoints();

        for (int i = waypoints.size() - 1; i >= 0; i--) {
            WayPoint wp = waypoints.get(i);
            if (bounds.contains(wp.x, wp.y)) {
                return wp;
            }
        }

        return null;
    }

    public static double getMinElevation(Route route, double startDistance, double finishDistance) {
        double minElevation = Double.POSITIVE_INFINITY;

        for (WayPoint wp: route.getWaypoints()){
            if (wp.getTotalDistance() >= startDistance && wp.getTotalDistance() <= finishDistance){
                double elevation = wp.getElevation();
                if (elevation < minElevation)
                    minElevation = elevation;
            }
        }

        return minElevation;
    }

    public static double getMaxElevation(Route route, double startDistance, double finishDistance) {
        double maxElevation = Double.NEGATIVE_INFINITY;

        for (WayPoint wp: route.getWaypoints()){
            if (wp.getTotalDistance() >= startDistance && wp.getTotalDistance() <= finishDistance){
                double elevation = wp.getElevation();
                if (elevation > maxElevation)
                    maxElevation = elevation;
            }
        }

        return maxElevation;
    }
}
