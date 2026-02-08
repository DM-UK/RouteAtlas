package render;


import route.Route;
import route.RouteUtils;
import route.WayPoint;
import routeatlas.MapPage;
import routeatlas.RouteAtlas;
import utils.Graphics2DUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class AtlasMapRenderer extends MapRenderer {
    private static final float PAGE_FONT_SCALE = 0.75f; // font size in relation to a page bounds smallest dimension
    private static final float MARKER_FONT_SCALE = 0.70f; // font size in relation to a markers radius

    private final MapRenderSettings mapRenderSettings;
    private final RouteAtlas atlas;
    private final int index;

    private Font mapFont = new Font("Helvetica", Font.BOLD, 1);
    private double majorMileMarkerRadius;
    private Color routeColor;
    private BasicStroke routeStroke;
    private Font titleFont;
    private BasicStroke pageBoundsStroke;
    private Font attributionFont;

    public AtlasMapRenderer(MapPage map, BufferedImage image, RouteAtlas atlas, int index, MapRenderSettings mapRenderSettings){
        super(map, image);
        this.atlas = atlas;
        this.index = index;
        this.mapRenderSettings = mapRenderSettings;
        configure();
    }

    private void configure() {
        //convert to pixel
        float pageBoundsThickness = toPagePixels(mapRenderSettings.getPageBoundsThickness());
        float transparency = (float) mapRenderSettings.getRouteTransparency();
        float routeWidth = toPagePixels(mapRenderSettings.getRouteWidth());
        float titleFontSize = toPagePixels(mapRenderSettings.getPageTitleFontSize());
        float attributionFontSize = toPagePixels(mapRenderSettings.getAttributionFontSize());
        majorMileMarkerRadius = toPagePixels(mapRenderSettings.getMarkerSize());

        routeColor = new Color(1f, 0f, 0.5f, transparency);
        routeStroke = new BasicStroke(routeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        pageBoundsStroke = new BasicStroke(pageBoundsThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        titleFont = mapFont.deriveFont(titleFontSize);
        attributionFont = mapFont.deriveFont(attributionFontSize);
    }

    public void render() {
        if (map == atlas.getOverviewPage()){
            drawRoute();
            drawPageBounds();
            drawPageTitle();
            drawAttribution();
        }
        else {
            drawRoute();
            drawMileMarkers(mapRenderSettings.getMarkerIntervals(), mapRenderSettings.getMarkerSegments());
            drawPageTitle();
            drawAttribution();
        }
    }

    //draw aligned to above the bottom right corner of image
    private void drawAttribution() {
        g2d.setColor(Color.black);
        g2d.setFont(attributionFont);
        Graphics2DUtils.drawAlignedString(g2d, mapRenderSettings.getAttribution(), image.getWidth(), image.getHeight(), Graphics2DUtils.HAlign.RIGHT, Graphics2DUtils.VAlign.ABOVE);
    }

    //draw aligned to below the top left corner of image
    private void drawPageTitle() {
        g2d.setColor(Color.black);
        g2d.setFont(titleFont);
        Graphics2DUtils.drawAlignedString(g2d, getTitle(), 0, 0, Graphics2DUtils.HAlign.LEFT, Graphics2DUtils.VAlign.BELOW);
    }

    private void drawRoute() {
        //create shape from waypoints
        List<Point2D> routePoints = new ArrayList<>(atlas.getRoute().getWaypoints());
        Shape routeShape = createShape(routePoints, false);

        //render
        g2d.setStroke(routeStroke);
        g2d.setColor(routeColor);
        g2d.draw(routeShape);
    }

    private void drawPageBounds(){
        g2d.setColor(Color.black);
        g2d.setStroke(pageBoundsStroke);

        for (int i = 1; i < atlas.getAllPages().size(); i++){
            MapPage map = atlas.getAllPages().get(i);
            Shape shape = createShapeFromCoordinates(map.getBounds().getAllCorners(), true);
            Rectangle pageBounds = shape.getBounds();

            //scale font to that of page bounds smallest dimension
            int smallestDimension = Math.min(pageBounds.width, pageBounds.height);
            Font scaledPageFont = mapFont.deriveFont(smallestDimension * PAGE_FONT_SCALE);

            //render shape
            g2d.setFont(scaledPageFont);
            g2d.draw(shape);

            //render page number inside the middle of the rectangle
            String indexStr = ""+i;
            Graphics2DUtils.drawAlignedString(g2d, indexStr, (int) pageBounds.getCenterX(), (int) pageBounds.getCenterY(), Graphics2DUtils.HAlign.CENTRE, Graphics2DUtils.VAlign.MIDDLE);
        }
    }

    private void drawMileMarkers(double distanceBetweenMajorMarkers, int numberOfMinorMarkersInBetween) {
        float majorMarkerLineThickness = (float) (majorMileMarkerRadius / 15);
        float minorMileMarkerRadius = (float) (majorMileMarkerRadius / 2.5);
        float minorMarkerLineThickness = (minorMileMarkerRadius / 15);
        g2d.setFont(mapFont);

        // distance between each minor marker
        double minorSpacing = distanceBetweenMajorMarkers / (numberOfMinorMarkersInBetween + 1);

        // convert spacing to miles if needed
        if (mapRenderSettings.useMileUnits())
            minorSpacing = minorSpacing * RouteUtils.KM_TO_MILE;

        // split the route into evenly spaced waypoints
        Route segmentedRoute = RouteUtils.createSegmentedRoute(atlas.getRoute(), minorSpacing);
        List<WayPoint> markers = segmentedRoute.getWaypoints();

        int majorInterval = numberOfMinorMarkersInBetween + 1;

        for (int i = 0; i < markers.size(); i++) {
            WayPoint wp = markers.get(i);
            Marker marker;

            boolean isStart = (i == 0);
            boolean isFinish = (i == markers.size() - 1);
            boolean isMajor = isFinish || (i % majorInterval == 0);

            Point p = toPixel(wp);

            if (isMajor) {
                String text;

                if (isStart)
                    text = "S";
                else if (isFinish)
                    text = "F";
                else {
                    double distance = wp.getTotalDistance();

                    if (mapRenderSettings.useMileUnits())
                        distance = distance / RouteUtils.KM_TO_MILE;

                    // last two digits only
                    long wholeDistance = Math.round(distance);
                    text = String.valueOf(wholeDistance % 100);
                }

                marker = new Marker(text, Color.white, (int) majorMileMarkerRadius, majorMarkerLineThickness);
            } else
                marker = new Marker("", Color.yellow, (int) minorMileMarkerRadius, minorMarkerLineThickness);

            marker.draw(g2d, p);
        }
    }

    private String getTitle() {
        if (mapRenderSettings.getTitle().isEmpty() && map != atlas.getOverviewPage())
            return ""+index;
        else
            return mapRenderSettings.getTitle();
    }

    public record Marker(String text, Color colour, int radius, float lineThickness){
        public void draw(Graphics2D g2d, Point p){
            //fill
            g2d.setColor(colour);
            g2d.fillOval(p.x - radius / 2, p.y - radius / 2, radius, radius);
            //text
            Font circledFont = g2d.getFont().deriveFont(radius * MARKER_FONT_SCALE);
            g2d.setFont(circledFont);
            g2d.setColor(Color.black);
            Graphics2DUtils.drawAlignedString(g2d, text, p.x, p.y, Graphics2DUtils.HAlign.CENTRE, Graphics2DUtils.VAlign.MIDDLE);
            //outline
            g2d.setStroke(new BasicStroke(lineThickness));
            g2d.setColor(Color.black);
            g2d.drawOval(p.x - radius / 2, p.y - radius / 2, radius, radius);
        }
    }
}
