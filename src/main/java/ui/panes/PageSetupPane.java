package ui.panes;

import render.ElevationProfileSettings;
import render.MapRenderSettings;
import render.RenderSetup;
import render.TileLayer;
import ui.swing.AccordionPanel;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/** Panel encompassing a TileLayerSetupPane, RenderSetupPane and ElevationSetupPane in AccordionPanels, while allowing only a single AccordionPanel to expand at a time **/
public class PageSetupPane extends JPanel implements RenderSetup {
    private final DefaultComboBoxModel<String> unitsComboBox = new DefaultComboBoxModel<>(new String[]{"Miles", "Kilometres"});

    private final TileLayerSetupPane tileLayerPane = new TileLayerSetupPane();
    private final RenderSetupPane renderPane = new RenderSetupPane(unitsComboBox);
    private final ElevationSetupPane elevationSetupPane = new ElevationSetupPane(unitsComboBox);

    private final AccordionPanel tileLayerAccordionPane;
    private final AccordionPanel renderAccordionPane;
    private final AccordionPanel elevationAccordionPane;

    public PageSetupPane(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        tileLayerAccordionPane = new AccordionPanel("Tiles", tileLayerPane);
        renderAccordionPane = new AccordionPanel("Render", renderPane);
        elevationAccordionPane = new AccordionPanel("Elevation Profile", elevationSetupPane);

        add(tileLayerAccordionPane);
        add(renderAccordionPane);
        add(elevationAccordionPane);

        tileLayerAccordionPane.addPropertyChangeListener(evt -> {
            if (Boolean.TRUE.equals(evt.getNewValue()))
                collapseOtherPanels(tileLayerAccordionPane);
        });

        renderAccordionPane.addPropertyChangeListener(evt -> {
            if (Boolean.TRUE.equals(evt.getNewValue()))
                collapseOtherPanels(renderAccordionPane);
        });

        elevationAccordionPane.addPropertyChangeListener(evt -> {
            if (Boolean.TRUE.equals(evt.getNewValue()))
                collapseOtherPanels(elevationAccordionPane);
        });

        tileLayerAccordionPane.setExpanded(true);
    }

    private void collapseOtherPanels(AccordionPanel selection) {
        if (tileLayerAccordionPane.isExpanded() && selection != tileLayerAccordionPane)
            tileLayerAccordionPane.setExpanded(false);

        if (renderAccordionPane.isExpanded() && selection != renderAccordionPane)
            renderAccordionPane.setExpanded(false);

        if (elevationAccordionPane.isExpanded() && selection != elevationAccordionPane)
            elevationAccordionPane.setExpanded(false);
    }

    @Override
    public TileLayer getTileLayer() {
        return tileLayerPane;
    }

    @Override
    public MapRenderSettings getRenderSettings() {
        return renderPane;
    }

    @Override
    public ElevationProfileSettings getElevationSettings() {
        return elevationSetupPane;
    }

    public void setPropertyChangeListener(PropertyChangeListener listener) {
        tileLayerPane.getTileLayerModel().addListener(listener);
        renderPane.addFieldChangeListener(listener);
        elevationSetupPane.addFieldChangeListener(listener);
    }

    public void setToOverviewDefaults() {
        elevationSetupPane.setDistanceSpacing(10);
        elevationSetupPane.setSegmentSpacing(30);
        tileLayerPane.setProvider(1);
        tileLayerPane.setZoom(10);
    }
}
