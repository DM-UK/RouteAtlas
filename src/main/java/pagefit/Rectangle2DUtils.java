package pagefit;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class Rectangle2DUtils {
    public static Point2D calculateBoundingBoxCentre(List<Point2D> points) {
        Rectangle2D bounds = calculateBoundingBox(points);
        return new Point2D.Double(
                bounds.getCenterX(),
                bounds.getCenterY()
        );
    }

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

    public static Rectangle fitCentered(int containerW, int containerH, int contentW, int contentH) {
        double scale = Math.min(containerW / (double) contentW, containerH / (double) contentH);

        int w = (int) Math.round(contentW * scale);
        int h = (int) Math.round(contentH * scale);

        int x = (containerW - w) / 2;
        int y = (containerH - h) / 2;

        return new Rectangle(x, y, w, h);
    }
}
