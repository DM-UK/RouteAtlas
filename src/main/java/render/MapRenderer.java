package render;

import org.locationtech.proj4j.ProjCoordinate;
import routeatlas.MapPage;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

/** Handles coordinate conversion and scale calculations for rendering map data onto a BufferedImage. */
public class MapRenderer {
    final MapPage map;
    final BufferedImage image;
    final Graphics2D g2d;
    private double pixelsPerGeographicMetre;
    private double paperMetresPerPixel;

    public MapRenderer(MapPage map, BufferedImage image){
        this.map = map;
        this.image = image;
        this.g2d = image.createGraphics();
        //high quality?
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        calculate();
    }

    //calculate map and paper scale
    private void calculate() {
        // screen space (pixels)
        int imageWidthPx  = image.getWidth();
        int imageHeightPx = image.getHeight();

        // world / geographic space (metres)
        double geoWidthMetres  = map.getBounds().getWidth();
        double geoHeightMetres = map.getBounds().getHeight();

        // paper / physical space (metres)
        double paperWidthMetres  = map.getScaledPaper().getPaperWidth();
        double paperHeightMetres = map.getScaledPaper().getPaperHeight();

        if (map.getOrientation() == PageFormat.LANDSCAPE) {
            double tmp = paperWidthMetres;
            paperWidthMetres = paperHeightMetres;
            paperHeightMetres = tmp;
        }

        // geographic scale (used for map content)
        double pixelsPerGeoMetreX = imageWidthPx  / geoWidthMetres;
        double pixelsPerGeoMetreY = imageHeightPx / geoHeightMetres;

        // paper scale (used for line widths, text etc...)
        double pixelsPerPaperMetreX = imageWidthPx  / paperWidthMetres;
        double pixelsPerPaperMetreY = imageHeightPx / paperHeightMetres;

        pixelsPerGeographicMetre =  Math.min(pixelsPerGeoMetreX, pixelsPerGeoMetreY);
        paperMetresPerPixel = Math.min(pixelsPerPaperMetreX, pixelsPerPaperMetreY);
    }

    /** Builds a shape from map coordinates converted to screen coordinates. */
    public Shape createShapeFromCoordinates(List<ProjCoordinate> coordinates, boolean close) {
        List<Point2D> points = new ArrayList<>(coordinates.size());

        for (ProjCoordinate pc : coordinates)
            points.add(new Point2D.Double(pc.x, pc.y));

        return createShape(points, close);
    }

    /** Builds a shape from map coordinates converted to screen coordinates. */
    public Shape createShape(List<Point2D> points, boolean close) {
        Path2D.Double path = new Path2D.Double();
        Point2D first = points.getFirst();
        Point firstScreenCoordinate = toPixel(first);
        path.moveTo(firstScreenCoordinate.x,firstScreenCoordinate.y);

        for (Point2D wp: points) {
            Point screenCoordinate = toPixel(wp);
            path.lineTo(screenCoordinate.x,screenCoordinate.y);
        }

        if (close)
            path.closePath();

        return path;
    }

    /** Converts a width in millimetres to the number of page pixels. */
    public float toPagePixels(double widthInMM) {
        double widthInMetres = widthInMM / 1000.0;
        return (float) (widthInMetres * paperMetresPerPixel);
    }

    /** Converts map coordinates to pixel coordinates relative to the map bounds. */
    public Point toPixel(Point2D point) {
        double dx = point.getX() - map.getBounds().getLowerCorner().x;
        double dy = map.getBounds().getUpperCorner().y - point.getY();

        int pixelX = (int) Math.round(dx * pixelsPerGeographicMetre);
        int pixelY = (int) Math.round(dy * pixelsPerGeographicMetre);

        return new Point(pixelX, pixelY);
    }
}
