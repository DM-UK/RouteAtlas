package render;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

public class GraphicsUtils {
    public static final int ALIGN_CENTRE = 0;
    public static final int ALIGN_LEFT = 1;
    public static final int ALIGN_RIGHT = 2;//normal
    public enum HAlign {LEFT, CENTRE, RIGHT}
    public enum VAlign {ABOVE, MIDDLE, BELOW}

    public static void drawAlignedString(Graphics g2d, String text, int x, int y, HAlign hAlign, VAlign vAlign) {
        if (text.isBlank())
            return;

        FontRenderContext frc = g2d.getFontMetrics().getFontRenderContext();
        TextLayout layout = new TextLayout(text, g2d.getFont(), frc);

        // Tight glyph bounds (layout-local, may be negative)
        Rectangle2D bounds = layout.getBounds();
        //Rectangle2D bounds = fm.getStringBounds(text, g);

        double bx = bounds.getX();
        double bw = bounds.getWidth();

        int drawX = switch (hAlign) {
            case LEFT   -> (int) Math.round(x - bx);
            case CENTRE -> (int) Math.round(x - bx - bw / 2);
            case RIGHT  -> (int) Math.round(x - bx - bw);
        };

        int drawY = switch (vAlign) {
            case ABOVE  -> (int) Math.round(y - bounds.getMaxY());
            case MIDDLE -> (int) Math.round(y - bounds.getCenterY());
            case BELOW  -> (int) Math.round(y - bounds.getMinY());
        };

        g2d.drawString(text, drawX, drawY);
    }


    public static void drawAlignedString(Graphics g, String label, int x, int y, int alignment) {
        FontMetrics fm = g.getFontMetrics();

        int textWidth = fm.stringWidth(label);

        // Horizontal alignment
        int drawX = x;
        switch (alignment) {
            case ALIGN_CENTRE:
                drawX = x - textWidth / 2;
                break;
            case ALIGN_RIGHT:
                drawX = x - textWidth;
                break;
            case ALIGN_LEFT:
            default:
                break;
        }

        // Vertical midpoint alignment
        int textHeight = fm.getAscent() + fm.getDescent();
        int drawY = y + fm.getAscent() - textHeight / 2;

        g.drawString(label, drawX, drawY);
    }
}
