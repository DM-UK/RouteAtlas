package routeatlas;


import java.awt.print.Paper;

public class ScaledPaper {
    public static final String[] PAPER_SIZE_STRINGS = new String[]{"A1", "A2", "A3", "A4", "A5", "A6", "Custom"};

    private final Paper paper;
    private final double paperWidth;
    private final double paperHeight;
    private final double scaledWidth;
    private final double scaledHeight;
    private final double scale;

    private static final double POINT_TO_METRE = 0.0254 / 72.0;

    public ScaledPaper(Paper paper, double scale){
        this.paper = paper;
        this.paperWidth = paper.getWidth() * POINT_TO_METRE;
        this.paperHeight = paper.getHeight() * POINT_TO_METRE;
        this.scaledWidth = paperWidth * scale;
        this.scaledHeight = paperHeight * scale;
        this.scale = scale;
    }

    @Override
    public String toString() {
        return String.format(
                "Page: %.3fm x %.3fm | Map: %.0fm x %.0fm | 1: %.0f",
                paperWidth, paperHeight,
                scaledWidth, scaledHeight,
                scale
        );
    }

    public static ScaledPaper fromPoints(int width, int height, double scale) {
        Paper paper = new Paper();
        paper.setSize(width, height);
        return new ScaledPaper(paper, scale);
    }

    public static ScaledPaper fromMetres(double width, double height, double scale) {
        Paper paper = new Paper();
        double widthPts = width / POINT_TO_METRE;
        double heightPts = height / POINT_TO_METRE;
        paper.setSize(widthPts, heightPts);
        return new ScaledPaper(paper, scale);
    }

    public static ScaledPaper fromString(String str, double pageScale) {
        return switch (str) {
            case "A1" -> fromPoints(1684, 2384, pageScale);
            case "A2" -> fromPoints(1191, 1684, pageScale);
            case "A3" -> fromPoints(842, 1191, pageScale);
            case "A4" -> fromPoints(595, 842, pageScale);
            case "A5" -> fromPoints(420, 595, pageScale);
            case "A6" -> fromPoints(298, 420, pageScale);
            default -> null;
        };
    }

    public double getPaperWidth() {
        return paperWidth;
    }

    public double getPaperHeight() {
        return paperHeight;
    }

    public double getScaledWidth() {
        return scaledWidth;
    }

    public double getScaledHeight() {
        return scaledHeight;
    }

    public double getScale() {
        return scale;
    }

    public Paper getPaper() {
        return paper;
    }

    public double getDPI(int pixelWidth, int pixelHeight) {
        double widthInches  = paper.getWidth() / 72.0;
        double heightInches = paper.getHeight() / 72.0;
        double dpiX = pixelWidth / widthInches;
        double dpiY = pixelHeight / heightInches;
        return (dpiX + dpiY) / 2.0;
    }
}
