package render;

import java.awt.*;

public interface ElevationProfileSettings {
    Dimension minimumCanvasDimensions();
    double xCanvasOffset();
    double yCanvasOffset();
    int maximumYAxisTicks();
    double xAxisInterval();
    double segmentInterval();
    float fontSize();
    double lineThickness();
    float outlineThickness();
    boolean useMileUnits();

    boolean shouldDisplay();
}
