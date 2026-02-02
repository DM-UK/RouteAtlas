package ui.panes;

import routeatlas.ScaledPaper;
import ui.view.AtlasSetupView;
import ui.swing.BasicForm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Panel containing route and page details. **/
public class AtlasSetupPane extends BasicForm implements AtlasSetupView {
    private final JTextField routeIdField;
    private final JTextField pageWidthField;
    private final JTextField pageHeightField;
    private final JTextField mapScaleField;
    private final JComboBox<String> pageSizeField;

    public AtlasSetupPane() {
        setBorder(BorderFactory.createCompoundBorder(
                  BorderFactory.createTitledBorder("Atlas Setup"),
                  BorderFactory.createEmptyBorder(3, 3, 3, 3)
        ));
        routeIdField = addTextField("Route ID", "3798636", 10);
        pageSizeField = addComboBoxField("Page size", ScaledPaper.PAPER_SIZE_STRINGS);
        pageWidthField = addTextField("Page width (mm)", "0", 6);
        pageHeightField = addTextField("Page height (mm)", "0", 6);
        mapScaleField = addTextField("Scale", "25000", 6);
        pageSizeField.addActionListener(new PageSizeListener()); // listen for combobox changes so we can update width/height fields
        pageSizeField.setSelectedItem("A4"); // set default to A4 which will also trigger the PageSizeListener
        getButton().setVisible(true);
    }

    public String getRouteId() {
        return routeIdField.getText();
    }

    @Override
    public double getPageWidth() {
        return Double.parseDouble(pageWidthField.getText()) / 1000;//mm to metres
    }

    @Override
    public double getPageHeight() {
        return Double.parseDouble(pageHeightField.getText()) / 1000;//mm to metres
    }

    @Override
    public double getPageScale() {
        return Double.parseDouble(mapScaleField.getText());
    }

    private class PageSizeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selected = (String) pageSizeField.getSelectedItem();

            if (!"Custom".equals(selected)) {
                ScaledPaper paper = ScaledPaper.fromString(selected, -1);
                long width = Math.round(paper.getPaperWidth() * 1000);
                long height = Math.round(paper.getPaperHeight() * 1000);
                pageWidthField.setText(""+width);
                pageHeightField.setText(""+height);
                pageWidthField.setEnabled(false);
                pageHeightField.setEnabled(false);
            } else {
                pageWidthField.setEnabled(true);
                pageHeightField.setEnabled(true);
                pageWidthField.setText("0");
                pageHeightField.setText("0");
            }
        }
    }
}
