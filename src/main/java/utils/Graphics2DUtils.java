package utils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

public class Graphics2DUtils {
    public enum HAlign {LEFT, CENTRE, RIGHT}
    public enum VAlign {ABOVE, MIDDLE, BELOW}

    public static void drawAlignedString(Graphics2D g2d, String text, int x, int y, HAlign hAlign, VAlign vAlign) {
        if (text.isBlank())
            return;

        FontRenderContext frc = g2d.getFontMetrics().getFontRenderContext();
        TextLayout layout = new TextLayout(text, g2d.getFont(), frc);
        Rectangle2D bounds = layout.getBounds();

        double bx = bounds.getX();
        double bw = bounds.getWidth();
        double advance = layout.getAdvance();

        double drawX = switch (hAlign) {
            case LEFT   -> x;
            case CENTRE -> x - advance / 2.0;
            case RIGHT  -> x - advance;
        };

        int drawY = switch (vAlign) {
            case ABOVE  -> (int) Math.round(y - bounds.getMaxY());
            case MIDDLE -> (int) Math.round(y - bounds.getCenterY());
            case BELOW  -> (int) Math.round(y - bounds.getMinY());
        };

        layout.draw(g2d, (float) drawX, (float) drawY);
    }
}
