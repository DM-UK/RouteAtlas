package ui.panes;

import render.ElevationProfileSettings;
import ui.swing.BasicForm;
import ui.swing.DoubleSlider;

import javax.swing.*;
import java.awt.*;

public class ElevationSetupPane extends BasicForm implements ElevationProfileSettings {
    private final DefaultComboBoxModel<String> unitsComboBoxModel;
    private final JComboBox<String> display;
    private final JSlider segmentInterval;
    private final JSlider tickInterval;
    private final JSlider minimumWidth;
    private final JSlider minimumHeight;
    private final DoubleSlider fontSize;
    private final DoubleSlider xOffset;
    private final DoubleSlider yOffset;
    private final DoubleSlider plotThickness;
    private final DoubleSlider outlineThickness;

    public ElevationSetupPane(DefaultComboBoxModel<String> unitsComboBoxModel){
        this.unitsComboBoxModel = unitsComboBoxModel;
        this.display = addComboBoxField("Display", new String[]{"On", "Off"});
        this.tickInterval = addSliderField("Distance Interval", 1, 15, 1);
        this.segmentInterval = addSliderField("Segment Interval", 1, 30, 5);
        this.xOffset =  addSliderField("X position", 0.0, 1.0, 0.995);
        this.yOffset =  addSliderField("Y position", 0.0, 1.0, 0.005);
        this.minimumWidth =  addSliderField("Min Width", 1, 500, 70);
        this.minimumHeight =  addSliderField("Min Height", 1, 200, 15);
        this.fontSize =  addSliderField("Font size", 1.0, 5.0, 2.0);
        this.plotThickness =  addSliderField("Plot Thickness", 0.0, 3.0, 0.4);
        this.outlineThickness =  addSliderField("Outline Thickness", 0.0, 3.0, 0.5);
    }

    public void setTickSpacing(int tickInterval, int segmentInterval){
        this.tickInterval.setValue(tickInterval);
        this.segmentInterval.setValue(segmentInterval);
    }

    @Override
    public Dimension minimumCanvasDimensions() {
        return new Dimension(minimumWidth.getValue(), minimumHeight.getValue());
    }

    @Override
    public double xCanvasOffset() {
        return xOffset.getDoubleValue();
    }

    @Override
    public double yCanvasOffset() {
        return yOffset.getDoubleValue();
    }

    @Override
    public int maximumYAxisTicks() {
        return 3;
    }

    @Override
    public double xAxisInterval() {
        return tickInterval.getValue();
    }

    @Override
    public double segmentInterval() {
        return segmentInterval.getValue();
    }

    @Override
    public float fontSize() {
        return (float) fontSize.getDoubleValue();
    }

    @Override
    public double lineThickness() {
        return plotThickness.getDoubleValue();
    }

    @Override
    public float outlineThickness() {
        return (float) outlineThickness.getDoubleValue();
    }

    @Override
    public boolean useMileUnits() {
        return unitsComboBoxModel.equals("Miles");
    }

    @Override
    public boolean shouldDisplay() {
        return display.getSelectedItem().equals("On");
    }

}
