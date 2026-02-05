package ui.swing;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class BasicForm extends JPanel {
    private List<PropertyChangeListener> fieldChangeListeners = new ArrayList<>();

    private JPanel labelPanel = new JPanel();
    private JPanel componentPanel = new JPanel();
    private JPanel buttonPanel = new JPanel();
    private JButton button = new JButton();

    public BasicForm(){
        init();
    }

    private void init() {
        setLayout(new BorderLayout(0, 0));

        labelPanel.setLayout(
                new GridLayout(0, 1, 0, 0));

        componentPanel.setLayout(
                new GridLayout(0, 1, 0, 0));

        add(labelPanel, BorderLayout.WEST);
        add(componentPanel, BorderLayout.CENTER);

        button.setVisible(false);
        buttonPanel.add(button);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public JButton getButton() {
        return button;
    }

    public void addLabel(String labelText) {
        JLabel label = new JLabel(labelText);
        labelPanel.add(label);
    }

    public void addField(JComponent component) {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 1));
        container.add(component);
        componentPanel.add(container);
    }

    public JTextField addTextField(String labelText, String defaultValue, int length){
        JTextField textField = new JTextField(defaultValue, length);
        attachListenerToTextField(textField);
        addLabel(labelText);
        addField(textField);
        return textField;
    }

    public JSlider addSliderField(String labelText, BoundedRangeModel model) {
        SliderDisplayPane sliderDisplayPane = new SliderDisplayPane(model);
        JSlider slider = sliderDisplayPane.getSlider();
        attachListenerToSlider(slider);
        addLabel(labelText);
        addField(sliderDisplayPane);
        return slider;
    }

    public JSlider addSliderField(String labelText, int min, int max, int init){
        SliderDisplayPane sliderDisplayPane = new SliderDisplayPane(min, max, init);
        JSlider slider = sliderDisplayPane.getSlider();
        attachListenerToSlider(slider);
        addLabel(labelText);
        addField(sliderDisplayPane);
        return slider;
    }

    public DoubleSlider addSliderField(String labelText, double min, double max, double init){
        SliderDisplayPane sliderDisplayPane = new SliderDisplayPane(min, max, init);
        DoubleSlider slider = sliderDisplayPane.getDoubleSlider();
        attachListenerToSlider(slider);
        addLabel(labelText);
        addField(sliderDisplayPane);
        return slider;
    }

    public <T> JComboBox<T> addComboBoxField(String labelText, ComboBoxModel<T> model) {
        JComboBox<T> comboBox = new JComboBox<>(model);
        attachListenerToComboBox(comboBox);
        addLabel(labelText);
        addField(comboBox);
        return comboBox;
    }

    public <T> JComboBox<T> addComboBoxField(String labelText, T[] options) {
        JComboBox<T> comboBox = new JComboBox<>(options);
        attachListenerToComboBox(comboBox);
        addLabel(labelText);
        addField(comboBox);
        return comboBox;
    }

    private void fireActionListeners() {
        PropertyChangeEvent event = new PropertyChangeEvent(this, "fieldChange", null, null);

        for (PropertyChangeListener listener : fieldChangeListeners) {
            listener.propertyChange(event);
        }
    }

    private void attachListenerToTextField(JTextField textField) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { fireActionListeners(); }
            public void removeUpdate(DocumentEvent e) { fireActionListeners(); }
            public void changedUpdate(DocumentEvent e) { fireActionListeners(); }
        });
    }

    private <T> void attachListenerToComboBox(JComboBox<T> comboBox) {
        comboBox.addActionListener(e -> {
            fireActionListeners();
        });
    }

    private void attachListenerToSlider(JSlider slider) {
        slider.addChangeListener(e -> {
            fireActionListeners();
        });
    }


    public void addFieldChangeListener(PropertyChangeListener listener) {
        fieldChangeListeners.add(listener);
    }
}
