package route;

import java.awt.geom.Point2D;

public class WayPoint extends Point2D.Double
{
    private int index;
    private double elevation;
    private double totalDistance;
    private double totalAscent;
    private double totalDescent;

    public WayPoint(Point2D coordinate, int index, double elevation, double totalDistance, double totalAscent, double totalDescent)
    {
        super(coordinate.getX(), coordinate.getY());
        this.index = index;
        this.elevation = elevation;
        this.totalDistance = totalDistance;
        this.totalAscent = totalAscent;
        this.totalDescent = totalDescent;
    }

    public WayPoint(WayPoint wp) {
        super(wp.getX(), wp.getY());
        this.index = wp.index;
        this.elevation = wp.elevation;
        this.totalDistance = wp.totalDistance;
        this.totalAscent = wp.totalAscent;
        this.totalDescent = wp.totalDescent;
    }

    public double getTotalDistance()
    {
        return totalDistance;
    }

    public double getElevation()
    {
        return elevation;
    }

    public double getTotalDescent() 
    {
        return totalDescent;
    }

    public double getTotalAscent() 
    {
        return totalAscent;
    }

    public void setTotalDistance(double distance)
    {
        totalDistance = distance;
    }

    public int getIndex()
    {
        return index;
    }

    @Override
    public String toString() {
        return "WayPoint{" +
                "index=" + index +
                ", elevation=" + elevation +
                ", totalDistance=" + totalDistance +
                ", totalAscent=" + totalAscent +
                ", totalDescent=" + totalDescent +
                "} " + super.toString();
    }
}
