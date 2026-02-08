package render;

import utils.Graphics2DUtils;

import java.awt.*;
import java.util.List;

public class TimeDistanceGraph {
    public static final double X_AXIS_HEIGHT_RATIO = 1.25;
    public static final double Y_AXIS_WIDTH_RATIO = 3.5;

    final Dimension canvasDimensions;
    final AxisModel xAxisModel;
    final AxisModel yAxisModel;
    final List<TimeDistance> plot;
    AxisLayout xAxisLayout;
    AxisLayout yAxisLayout;
    private Font tickFont = new Font("Helvetica", Font.BOLD, 1);
    private int leftPadding;
    int rightPadding;
    private int topPadding;
    private int bottomPadding;
    BasicStroke plotStroke;
    BasicStroke outlineStroke;

    public TimeDistanceGraph(Dimension plotDimensions, AxisModel xAxisModel, AxisModel yAxisModel, List<TimeDistance> plot){
        this.canvasDimensions = plotDimensions;
        this.xAxisModel = xAxisModel;
        this.yAxisModel = yAxisModel;
        this.plot = plot;
        setPlotLineThickness(2);
        setTickFontSize(15);
    }

    public void setTickFontSize(float fontSize){
        tickFont = tickFont.deriveFont(fontSize);
        configureLayout();
    }

    void setPlotLineThickness(float thickness) {
        plotStroke = new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        outlineStroke = plotStroke;
    }

    void setOutLineThickness(float thickness) {
        outlineStroke = new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    protected void configureLayout() {
        int fontSize = tickFont.getSize();
        int xAxisHeight = (int) (fontSize * X_AXIS_HEIGHT_RATIO);
        int yAxisWidth = (int) (fontSize * Y_AXIS_WIDTH_RATIO);
        leftPadding = (int) (fontSize * 0);
        rightPadding = (int) (fontSize * 0.8);
        topPadding = (int) (fontSize * 0.8);
        bottomPadding = (int) (fontSize * 0);
        Point origin = new Point(yAxisWidth + leftPadding, canvasDimensions.height + topPadding);
        xAxisLayout = new AxisLayout(xAxisModel, origin, canvasDimensions.width, xAxisHeight);
        yAxisLayout = new AxisLayout(yAxisModel, origin, yAxisWidth, canvasDimensions.height);
    }

    public void render(Graphics2D g2d) {
        Dimension dimension = getDimensions();
        g2d.fillRect(0, 0, dimension.width, dimension.height);
        g2d.setColor(Color.black);
        g2d.setStroke(outlineStroke);
        g2d.drawRect(0, 0, dimension.width, dimension.height);
        g2d.setFont(tickFont);
        xAxisLayout.render(g2d);
        yAxisLayout.render(g2d);
        g2d.setStroke(plotStroke);
        drawPlot(g2d);
    }

    public void drawPlot(Graphics2D g2d) {
        Point prev = null;

        for (TimeDistance plottable : plot) {
            double distance = plottable.distance;
            double time = plottable.time;

            Point curr = getTimeDistancePoint(distance, time);

            if (curr.x >= xAxisLayout.origin.x && curr.x <= xAxisLayout.origin.x + xAxisLayout.axisDimension.width){
                if (prev != null)
                    g2d.drawLine(prev.x, prev.y, curr.x, curr.y);

                prev = curr;
            }
        }
    }

    Point getTimeDistancePoint(double distance, double time) {
        double normalX = xAxisModel.getNormalValue(time);
        double normalY = yAxisModel.getNormalValue(distance);
        int xp = (int) (xAxisLayout.origin.x + (normalX * xAxisLayout.axisDimension.width));
        int yp = (int) (yAxisLayout.origin.y -(normalY * yAxisLayout.axisDimension.height));
        return new Point(xp, yp);
    }

    public Dimension getDimensions() {
        int offset = (int) (outlineStroke.getLineWidth()) * 2 ;
        int width = xAxisLayout.axisDimension.width + yAxisLayout.axisDimension.width + leftPadding + rightPadding + offset;
        int height = xAxisLayout.axisDimension.height + yAxisLayout.axisDimension.height + topPadding + bottomPadding + offset;
        return new Dimension(width, height);
    }

    public static class AxisModel{
        public final double startValue;
        public final double finishValue;
        public final double tickInterval;
        public final String textFormatString;
        public final double range;

        public AxisModel(double startValue, double finishValue, double tickInterval, String textFormatString) {
            this.startValue = startValue;
            this.finishValue = finishValue;
            this.tickInterval = tickInterval;
            this.textFormatString = textFormatString;
            this.range = finishValue - startValue;
        }

        public double getTickValue(double i) {
            return startValue + i * tickInterval;
        }

        public String getLabel(double value) {
            return String.format(textFormatString, value);
        }

        public double getNormalValue(double value) {
            return (value - startValue) / range;
        }
    }

    protected class AxisLayout{
        private final AxisModel model;
        private Point origin;
        final Dimension axisDimension;

        public AxisLayout(AxisModel model, Point origin, int width, int height) {
            this.model = model;
            this.origin = origin;
            this.axisDimension = new Dimension(width, height);
        }

        public AxisLayout(AxisModel model, Point origin, Dimension axisDimension) {
            this.model = model;
            this.origin = origin;
            this.axisDimension = axisDimension;
        }

        public void render(Graphics2D g2d){
            double range = model.finishValue - model.startValue;
            int tickCount = (int) Math.floor(range / model.tickInterval) + 1;

            for (int i = 0; i < tickCount; i++) {
                boolean isLast = false;

                if (i == tickCount -1)
                    isLast = true;

                Point p = getTickPosition(i);
                drawTick(g2d, p, i, isLast);
            }
        }

        void drawTick(Graphics2D g2d, Point p, int tickNumber, boolean isLast) {
            //if (isLast && this == xAxisLayout)
               // return;

            double value = model.getTickValue(tickNumber);
            String label = model.getLabel(value);

            if (this != yAxisLayout){
                int centreOffset = (axisDimension.height / 2);
                Graphics2DUtils.drawAlignedString(g2d, label,  p.x, p.y + centreOffset, Graphics2DUtils.HAlign.CENTRE, Graphics2DUtils.VAlign.MIDDLE);
            }
            else
                Graphics2DUtils.drawAlignedString(g2d, label+"  ",  p.x, p.y, Graphics2DUtils.HAlign.RIGHT, Graphics2DUtils.VAlign.MIDDLE);
        }

        public Point getTickPosition(double tickNumber) {
            double value = model.getTickValue(tickNumber);
            double ratio = model.getNormalValue(value);

            int x, y;

            if (this != yAxisLayout) {
                x = origin.x + (int) (ratio * axisDimension.width);
                y = origin.y;
            }
            else{
                x = origin.x;
                y = origin.y - (int) (ratio * axisDimension.height);
            }

            return new Point(x, y);
        }

        public void setOrigin(Point newOrigin) {
            int dx = newOrigin.x - origin.x;
            int dy = newOrigin.y - origin.y;
            axisDimension.width  -= dx;
            axisDimension.height += dy;
            this.origin = new Point(newOrigin);
        }

        public Point getOrigin() {
            return origin;
        }

        public Dimension getDimensions() {
            return axisDimension;
        }
    }

    public record TimeDistance(double time, double distance){

    }
}
