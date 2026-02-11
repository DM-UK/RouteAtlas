package ordnancesurvey;

import java.util.List;

/** The reverse engineered route lat/lon/elevation data JSON object. */
public class OrdnanceSurveyRoute
{
    private List<OrdnanceSurveyWayPoint> result;
    private double totalAscent;
    private double totalDescent;
    private double totalDistance;

    public List<OrdnanceSurveyWayPoint> getResult()
    {
        return result;
    }

    public double getTotalAscent()
    {
        return totalAscent;
    }

    public double getTotalDescent()
    {
        return totalDescent;
    }

    public double getTotalDistance()
    {
        return totalDistance;
    }

    public static class OrdnanceSurveyWayPoint
    {
        private double lat;
        private double lon;
        private double distance;
        private double height;
        private double ascentFromPrevious;
        private double descentFromPrevious;
        private double distanceFromPrevious;

        public double getLat()
        {
            return lat;
        }

        public double getLon()
        {
            return lon;
        }

        public double getDistance()
        {
            return distance;
        }

        public double getHeight()
        {
            return height;
        }

        public double getAscentFromPrevious() {
            return ascentFromPrevious;
        }

        public double getDescentFromPrevious() {
            return descentFromPrevious;
        }

        public double getDistanceFromPrevious() {
            return distanceFromPrevious;
        }
    }
}
