package ui.panes;

import render.MapRenderSettings;
import ui.swing.BasicForm;
import ui.swing.DoubleSlider;

import javax.swing.*;

public class RenderSetupPane extends BasicForm implements MapRenderSettings {
    private final JSlider markerInterval;
    private final DoubleSlider markerSize;
    private final JSlider markerSegments;
    private final DoubleSlider routeWidth;
    private final DoubleSlider routeTransparency;
    private final JTextField pageTitle;
    private final DoubleSlider pageBoundsThickness;
    private final DoubleSlider titleFontSize;
    private final DoubleSlider attributionFontSize;
    private final JTextField attribution;
    private final JComboBox<String> units;

    public RenderSetupPane(DefaultComboBoxModel<String> unitsComboBoxModel){
        this.pageTitle = addTextField("Title", "", 18);
        this.attribution = addTextField("Attribution", "", 18);
        this.units = addComboBoxField("Units", unitsComboBoxModel);
        this.markerInterval = addSliderField("Marker Intervals", 1, 10, 1);
        this.markerSize = addSliderField("Marker Size", 0.0, 30.0, 3.5);
        this.markerSegments = addSliderField("Marker Segments", 0, 10, 3);
        this.routeWidth = addSliderField("Route Thickness", 0.0, 10.0, 1);
        this.routeTransparency = addSliderField("Route Opaque", 0.0, 1.0, 0.5);
        this.pageBoundsThickness = addSliderField("Bounds Thickness", 0.0, 10.0, 1.0);
        this.titleFontSize = addSliderField("Title Size", 0.0, 20.0, 10.0);
        this.attributionFontSize = addSliderField("Attribution Size", 0.0, 20.0, 5.0);
    }

    @Override
    public int getMarkerIntervals() {
        return markerInterval.getValue();
    }

    @Override
    public double getMarkerSize() {
        return markerSize.getDoubleValue();
    }

    @Override
    public int getMarkerSegments() {
        return markerSegments.getValue();
    }

    @Override
    public double getRouteWidth() {
        return routeWidth.getDoubleValue();
    }

    @Override
    public double getRouteTransparency() {
        return routeTransparency.getDoubleValue();
    }

    @Override
    public String getTitle() {
        return pageTitle.getText();
    }

    @Override
    public double getPageBoundsThickness() {
        return pageBoundsThickness.getDoubleValue();
    }

    @Override
    public double getPageTitleFontSize() {
        return titleFontSize.getDoubleValue();
    }

    @Override
    public String getAttribution() {
        return attribution.getText();
    }

    @Override
    public double getAttributionFontSize() {
        return attributionFontSize.getDoubleValue();
    }

    @Override
    public boolean useMileUnits() {
        return units.getSelectedIndex() == 0;
    }
}
