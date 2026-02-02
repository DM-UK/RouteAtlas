package route;

import org.locationtech.proj4j.CoordinateReferenceSystem;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Route
{
    private List<WayPoint> waypoints = new ArrayList<WayPoint>();
    private String routeid;
    private CoordinateReferenceSystem crs;

    public Route(String routeid, CoordinateReferenceSystem crs) {
        this.routeid = routeid;
        this.crs = crs;
    }

    public void addWayPoint(WayPoint waypoint) {
        waypoints.add(waypoint);
    }

    public void addWayPoint(double x, double y, double elevation, double totalDistance) {
        addWayPoint(new Point2D.Double(x, y), elevation,totalDistance);
    }

    public void addWayPoint(Point2D coordinate, double elevation, double totalDistance) {
        double totalAscent = 0;
        double totalDescent = 0;

        if (!getWaypoints().isEmpty()) {
            double elevationFromLast = elevation - waypoints.getLast().getElevation();
            totalAscent = getTotalAscent();
            totalDescent = getTotalDescent();

            if (elevationFromLast > 0)
                totalAscent = totalAscent + elevationFromLast;
            else
                totalDescent = totalDescent + Math.abs(elevationFromLast);
        }

        WayPoint wp = new WayPoint(coordinate, waypoints.size(), elevation, totalDistance, totalAscent, totalDescent);
        waypoints.add(wp);
    }

    public double getTotalDescent() {
        if (waypoints.isEmpty())
            return 0;

        return waypoints.getLast().getTotalDescent();
    }

    public double getTotalAscent() {
        if (waypoints.isEmpty())
            return 0;

        return waypoints.getLast().getTotalAscent();
    }

    public double getTotalDistance() {
        if (waypoints.isEmpty())
            return 0;

        return waypoints.getLast().getTotalDistance();
    }

    public List<WayPoint> getWaypoints()
    {
        return waypoints;
    }

    public String getRouteid() {
        return routeid;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }
}
