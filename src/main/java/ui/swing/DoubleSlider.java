package ui.swing;

import javax.swing.*;

public class DoubleSlider extends JSlider{
    public DoubleSlider(double min, double max, double value) {
        setModel(new DoubleBoundedRangeModel(min, max, value));
    }

    public DoubleSlider(DoubleBoundedRangeModel model) {
        setModel(model);
    }

    public void setDoubleValue(double value) {
        getDoubleModel().setDoubleValue(value);
    }

    public double getDoubleValue() {
        return getDoubleModel().getDoubleValue();
    }

    public DoubleBoundedRangeModel getDoubleModel() {
        return (DoubleBoundedRangeModel) getModel();
    }
}
