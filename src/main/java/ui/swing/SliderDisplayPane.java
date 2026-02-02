package ui.swing;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class SliderDisplayPane extends JPanel{
    private final JLabel valueLabel = new JLabel();
    private final FlowLayout DEFAULT_LAYOUT = new FlowLayout(FlowLayout.RIGHT);
    private final JSlider slider;

    public SliderDisplayPane() {
        this(new JSlider());
    }

    public SliderDisplayPane(int min, int max, int value) {
        this(new JSlider(min, max, value));
    }

    public SliderDisplayPane(double min, double max, double value) {
        this(new DoubleSlider(min, max, value));
    }

    SliderDisplayPane(JSlider slider) {
        this.slider = slider;
        create();
    }

    public SliderDisplayPane(BoundedRangeModel model) {
        this(new JSlider(model));
    }

    private void create() {
        valueLabel.setPreferredSize(
                calculateWidestStringDimensions());
        valueLabel.setMinimumSize(
                calculateWidestStringDimensions());
        updateValueLabel();
        add(slider);
        add(valueLabel);
        addListener();
        setLayout(DEFAULT_LAYOUT);

    }

    private Dimension calculateWidestStringDimensions() {
        int maxLength = 0;

        if (getDoubleSlider() != null)
            maxLength = String.valueOf(
                    getDoubleSlider().
                    getDoubleModel().
                    getDoubleMaximum()).length() + 1;
        else
            maxLength = String.valueOf(
                    getSlider().
                    getMaximum()).length();

        String widestString = "8".repeat(maxLength);

        return new JLabel(widestString).getPreferredSize();
    }

    private void addListener() {
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateValueLabel();
            }
        });
    }

    private void updateValueLabel() {
        String value = getLabelString();
        valueLabel.setText(value);
        valueLabel.repaint();
        valueLabel.revalidate();
    }

    public JSlider getSlider()
    {
        return slider;
    }

    public DoubleSlider getDoubleSlider()
    {
        if (slider instanceof DoubleSlider doubleSlider)
            return doubleSlider;
        else
            return null;
    }

    private String getLabelString() {
        if (getDoubleSlider() != null)
            return formatDoubleValue(getDoubleSlider().getDoubleValue());
        else
            return "" + slider.getValue();
    }

    private static String formatDoubleValue(double doubleValue) {
        return String.format("%.2f", doubleValue);
    }

    @Override
    public void setEnabled(boolean enabled) {
        slider.setEnabled(enabled);
        valueLabel.setEnabled(enabled);
    }
}
