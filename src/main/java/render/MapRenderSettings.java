package render;

public interface MapRenderSettings {
    int getMarkerIntervals();
    double getMarkerSize();
    int getMarkerSegments();
    double getRouteWidth();
    double getRouteTransparency();
    String getTitle();
    double getPageBoundsThickness();
    double getPageTitleFontSize();
    String getAttribution();
    double getAttributionFontSize();
    boolean useMileUnits();
}
