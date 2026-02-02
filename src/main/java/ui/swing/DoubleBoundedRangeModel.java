package ui.swing;

import javax.swing.*;

public class DoubleBoundedRangeModel extends DefaultBoundedRangeModel{
    private double min;
    private double max;

    public DoubleBoundedRangeModel(double min, double max, double value) {
        super(0, 0, 0, Integer.MAX_VALUE);
        this.min = min;
        this.max = max;
        setDoubleValue(value);
    }

    public void setDoubleExtent(double value) {
        int intRange = Integer.MAX_VALUE - scaleToInt(max-value, min, max);
        setExtent(intRange);
    }

    private static int scaleToInt(double value, double min, double max) {
        return (int) ((value - min) / (max - min) * Integer.MAX_VALUE);
    }

    public void setDoubleValue(double value) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("Value out of bounds");
        }
        int intValue = (int) ((value - min) / (max - min) * Integer.MAX_VALUE);
        setValue(intValue);
    }

    public double getDoubleValue() {
        int intValue = getValue();
        return min + ((double) intValue / Integer.MAX_VALUE) * (max - min);
    }

    public double getDoubleMinimum() {
        return min;
    }

    public double getDoubleMaximum() {
        return max;
    }
}
