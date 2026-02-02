package render;


import route.Route;
import route.RouteUtils;
import route.WayPoint;
import routeatlas.MapPage;
import routeatlas.RouteAtlas;
import wmts.bounds.Bounds;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ElevationProfileRenderer {
    public static final int PLOT_ASCENT = 0;
    public static final int PLOT_DESCENT = 1;

    private final Route route;
    private List<WayPoint> segmentWaypoints;
    private final TimeDistanceGraph chart;
    private final RouteAtlas atlas;
    private final MapPage map;
    private final ElevationProfileSettings settings;
    private final int fontSize;
    private final float lineThickness;
    private final float outlineThickness;
    private final Dimension minimumCanvasDimensions;
    private final BufferedImage image;

    private Font segmentLabelFont ;

    public ElevationProfileRenderer(RouteAtlas atlas, BufferedImage image, MapPage map, ElevationProfileSettings settings) {
        this.map = map;
        this.atlas = atlas;
        this.image = image;
        this.settings = settings;
        this.route = atlas.getRoute();
        //use a MapRenderer context to calculate our page pixels (proof the logic probably should be refactored...)
        MapRenderer mapRenderer = new MapRenderer(map, image);
        this.fontSize = (int) mapRenderer.toPagePixels(settings.fontSize());
        this.lineThickness = mapRenderer.toPagePixels(settings.lineThickness());
        this.outlineThickness = mapRenderer.toPagePixels(settings.outlineThickness());
        Dimension dim = settings.minimumCanvasDimensions();
        this.minimumCanvasDimensions = new Dimension(
                (int) mapRenderer.toPagePixels(dim.width),
                (int) mapRenderer.toPagePixels(dim.height)
        );
        this.chart = createGraph();
    }

    public Dimension getDimensions(){
        return chart.getDimensions();
    }

    public void render() {
       if (!settings.shouldDisplay())
           return;

        Graphics2D g2d = image.createGraphics();
        double rendererWidth = getDimensions().width;
        double rendererHeight = getDimensions().height;
        double xOffset = (image.getWidth()  - rendererWidth)  * settings.xCanvasOffset();
        double yOffset = (image.getHeight() - rendererHeight) * settings.yCanvasOffset();
        g2d.translate(xOffset, yOffset);
        chart.render(g2d);
    }

    private TimeDistanceGraph createGraph() {
        //plot each route waypoints' distance/elevation to distance/time for the TimeDistanceGraph
        List<TimeDistanceGraph.TimeDistance> elevationPlotPoints = new ArrayList<>();
        for (WayPoint wp : route.getWaypoints()){
            var timeDistance = new TimeDistanceGraph.TimeDistance(wp.getTotalDistance(), wp.getElevation());
            elevationPlotPoints.add(timeDistance);
        }

        var xAxisModel = createXAxisModel(map.getBounds(), settings.xAxisInterval()); //for distance ticks
        var segmentModel = createXAxisModel(map.getBounds(), settings.segmentInterval()); //for segment lines/elevation change labels
        var yAxisModel = createYAxisModel(xAxisModel.startValue, xAxisModel.finishValue); //for height ticks

        //interpolate waypoints at every segment interval. starting at the first point within the map bounds.
        segmentWaypoints = RouteUtils.createSegmentedRoute(route, xAxisModel.startValue, settings.segmentInterval()).getWaypoints();
        var dimensions = calculateGraphDimensions(xAxisModel, yAxisModel);
        return new ElevationProfileGraph(dimensions, xAxisModel, yAxisModel, segmentModel, elevationPlotPoints);
    }

    private Dimension calculateGraphDimensions(TimeDistanceGraph.AxisModel xAxisModel, TimeDistanceGraph.AxisModel yAxisModel) {
        //loop through all map elevation graph models and find the largest distance/elevation change
        double maxDistance = 0;
        double minElevationChange = Double.MAX_VALUE;

        // Find distance max and elevation MIN
        if (map == atlas.getOverviewPage()) {
            maxDistance = xAxisModel.range;
            minElevationChange = yAxisModel.range;
        } else {
            for (MapPage map : atlas.getSectionPages()) {
                var xModel = createXAxisModel(map.getBounds(), settings.xAxisInterval());
                var yModel = createYAxisModel(xModel.startValue, xModel.finishValue);

                maxDistance = Math.max(maxDistance, xModel.range);
                minElevationChange = Math.min(minElevationChange, yModel.range);
            }
        }

        //apply the scale
        double xScale = minimumCanvasDimensions.width / maxDistance;
        double yScale = minimumCanvasDimensions.height / minElevationChange;
        return new Dimension((int) (xAxisModel.range * xScale), (int) (yAxisModel.range * yScale));
    }

    private TimeDistanceGraph.AxisModel createXAxisModel(Bounds bounds, double interval){
        double startDistance = RouteUtils.getFirstPointWithinBounds(route, bounds).getTotalDistance(); //first distance metres on route within map bounds
        double finishDistance = (int) RouteUtils.getLastPointWithinBounds(route, bounds).getTotalDistance(); //last distance metres on route within map bounds
        int segments = (int) Math.ceil((finishDistance - startDistance) / settings.segmentInterval()); //rounded up to nearest segement
        double startInterval = (int) Math.ceil(startDistance / settings.xAxisInterval()) * settings.xAxisInterval(); //rounded up to nearest int
        double finishInterval = startInterval + (segments * settings.segmentInterval()); //start + n segments
        return new TimeDistanceGraph.AxisModel(startInterval, finishInterval, interval, "%.0f");
    }

    private TimeDistanceGraph.AxisModel createYAxisModel(double startDistance, double finishDistance) {
        double minElevation = RouteUtils.getMinElevation(route, startDistance, finishDistance); //lowest elevation metres on route within map bounds
        double maxElevation = RouteUtils.getMaxElevation(route, startDistance, finishDistance ); //highest elevation metres on route within map bounds
        double range = maxElevation - minElevation;
        double interval = (range / (settings.maximumYAxisTicks() - 1)); //interval metres between y axis ticks
        return new TimeDistanceGraph.AxisModel(minElevation, maxElevation, interval, "%.0fM");
    }

    private class ElevationProfileGraph extends TimeDistanceGraph{
        private float[] dashPattern = {lineThickness * 2, lineThickness * 2};
        private Stroke segmentLine;

        private AxisModel segmentModel;
        private AxisLayout lineSegmentLayout;
        private AxisLayout descentSegmentLayout;
        private AxisLayout ascentSegmentLayout;

        public ElevationProfileGraph(Dimension canvasDimensions, AxisModel xAxisModel, AxisModel yAxisModel, AxisModel segmentModel, List<TimeDistance> elevationPlot) {
            super(canvasDimensions, xAxisModel, yAxisModel, elevationPlot);
            this.segmentModel = segmentModel;
            setTickFontSize(fontSize);
            setPlotLineThickness(lineThickness);
            setOutLineThickness(outlineThickness);
            segmentLine = new BasicStroke(lineThickness/2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f, dashPattern, 0f);
        }

        @Override
        public Dimension getDimensions() {
            Dimension dimension = super.getDimensions();
            int height = dimension.height + descentSegmentLayout.axisDimension.height + ascentSegmentLayout.getDimensions().height;
            return new Dimension(dimension.width, height);
        }

        @Override
        public void render(Graphics2D g2d) {
            super.render(g2d);
            lineSegmentLayout.render(g2d);
            descentSegmentLayout.render(g2d);
            ascentSegmentLayout.render(g2d);
            Point p = ascentSegmentLayout.getOrigin();
            g2d.setStroke(plotStroke);
            g2d.drawLine(p.x, p.y, p.x + xAxisLayout.axisDimension.width, p.y);
        }

        @Override
        protected void configureLayout() {
            super.configureLayout();
            segmentLabelFont = new Font("Helvetica", Font.BOLD, fontSize);
            int plotHeight = (int) (fontSize * X_AXIS_HEIGHT_RATIO);
            Point descentPlotOrigin = new Point(xAxisLayout.getOrigin().x, xAxisLayout.getOrigin().y - plotHeight);
            Point ascentPlotOrigin = new Point(descentPlotOrigin.x, descentPlotOrigin.y - plotHeight);
            Dimension plotDimension = new Dimension(xAxisLayout.getDimensions().width, plotHeight);
            yAxisLayout.setOrigin(ascentPlotOrigin);
            descentSegmentLayout = new SegmentElevationChangeLayout(segmentModel, descentPlotOrigin, plotDimension, PLOT_DESCENT);
            ascentSegmentLayout = new SegmentElevationChangeLayout(segmentModel, ascentPlotOrigin, plotDimension, PLOT_ASCENT);
            lineSegmentLayout = new SegmentLineLayout(segmentModel, xAxisLayout.getOrigin(), plotDimension);
        }

        private class SegmentElevationChangeLayout extends AxisLayout {
            private final int plotType;

            public SegmentElevationChangeLayout(AxisModel segmentModel, Point origin, Dimension plotDimension, int plotType) {
                super(segmentModel, origin, plotDimension);
                this.plotType = plotType;
            }

            @Override
            protected void drawTick(Graphics2D g2d, Point p, int tickNumber, boolean isLast) {
                //since we're drawing between the centre of the previous tick this will fall off screen
                if (tickNumber == 0)
                    return;

                g2d.setFont(segmentLabelFont);
                //move to the centre of this and the previous tick
                p = getTickPosition(tickNumber - 0.5);

                WayPoint prev = segmentWaypoints.get(tickNumber - 1);

                //if (tickNumber >= segmentWaypoints.size())
                    //return;

                WayPoint cur = segmentWaypoints.get(tickNumber);

                String label;
                if (plotType == PLOT_DESCENT) {
                    double descent = cur.getTotalDescent() - prev.getTotalDescent();
                    label = String.format("-%.0fM", descent);
                }
                 else{
                    double ascent = cur.getTotalAscent() - prev.getTotalAscent();
                    label = String.format("+%.0fM", ascent);
                }

                int centreOffset = (axisDimension.height / 2);
                GraphicsUtils.drawAlignedString(g2d, label, p.x, p.y + centreOffset, GraphicsUtils.ALIGN_CENTRE);
            }
        }

        private class SegmentLineLayout extends AxisLayout {
            public SegmentLineLayout(AxisModel segmentModel, Point origin, Dimension dimensions) {
                super(segmentModel, origin, dimensions);
            }

            @Override
            void drawTick(Graphics2D g2d, Point p, int tickNumber, boolean isLast) {
                g2d.setStroke(segmentLine);
                g2d.setColor(Color.black);
                //if (isLast)
                  //  return;

                WayPoint wp = segmentWaypoints.get(tickNumber);
                if (wp == segmentWaypoints.getLast())
                    return;

                Point end = getTimeDistancePoint(wp.getElevation(), wp.getTotalDistance());

                g2d.drawLine(end.x, p.y, end.x, end.y);
            }
        }

    }
}
