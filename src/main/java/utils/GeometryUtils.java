package utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class GeometryUtils {
    /** Returns the centre of the rectangle that contains all the given points  */
    public static Point2D calculateBoundingBoxCentre(List<Point2D> points) {
        Rectangle2D bounds = calculateBoundingBox(points);
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }

    /** Returns the rectangle that contains all the given points */
    public static Rectangle2D calculateBoundingBox(List<Point2D> points) {
        if (points == null || points.isEmpty())
            return new Rectangle2D.Double(0, 0, 0, 0);

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point2D p : points) {
            double x = p.getX();
            double y = p.getY();

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**  Returns a scaled Rectangle to fit inside the container while preserving aspect ratio and centering it */
    public static Rectangle fitCentered(int containerW, int containerH, int contentW, int contentH) {
        double scale = Math.min(containerW / (double) contentW, containerH / (double) contentH);

        int w = (int) Math.round(contentW * scale);
        int h = (int) Math.round(contentH * scale);

        int x = (containerW - w) / 2;
        int y = (containerH - h) / 2;

        return new Rectangle(x, y, w, h);
    }

    /** Returns a scaled Rectangle */
    public static Rectangle2D scaleRectangle(Rectangle2D rect, double scale) {
        double newWidth  = rect.getWidth()  * scale;
        double newHeight = rect.getHeight() * scale;

        double centerX = rect.getCenterX();
        double centerY = rect.getCenterY();

        double newX = centerX - newWidth  / 2.0;
        double newY = centerY - newHeight / 2.0;

        return new Rectangle2D.Double(newX, newY, newWidth, newHeight);
    }

    /** Pads a rectangle so its aspect ratio matches either the landscape or portrait orientation of the target size while remaining centered. */
    public static Rectangle2D padToAspectRatio(Rectangle2D rect, double targetWidth, double targetHeight) {
        double cx = rect.getCenterX();
        double cy = rect.getCenterY();
        double rw = rect.getWidth();
        double rh = rect.getHeight();

        if (rw <= 0 || rh <= 0) {
            return rect;
        }

        double currentAR = rw / rh;

        double arLandscape = targetWidth / targetHeight;
        double arPortrait  = targetHeight / targetWidth;

        double landscapeDiff = Math.abs(currentAR - arLandscape);
        double portraitDiff  = Math.abs(currentAR - arPortrait);
        double desiredAR     = (landscapeDiff <= portraitDiff) ? arLandscape : arPortrait;

        double newW = rw;
        double newH = rh;

        if (currentAR < desiredAR)
            newW = rh * desiredAR;
        else if (currentAR > desiredAR)
            newH = rw / desiredAR;
         else
            return rect;

        double newX = cx - newW / 2.0;
        double newY = cy - newH / 2.0;

        return new Rectangle2D.Double(newX, newY, newW, newH);
    }
}
