package ui;

import wmts.Layer;
import app.WebMapProviders;
import wmts.WebMapTileService;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Auto-updates provider/layer/zoom UI models. **/
public class TileLayerModel{
    private final static WebMapTileService[] ALL_PROVIDERS = WebMapProviders.getAll();
    private final DefaultComboBoxModel<WebMapTileService> providers = new DefaultComboBoxModel<>(ALL_PROVIDERS);
    private final DefaultComboBoxModel<Layer> layers = new DefaultComboBoxModel<>();
    private final BoundedRangeModel zoom = new DefaultBoundedRangeModel();
    private boolean isUpdating = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public TileLayerModel() {
        updateLayers();
        zoom.addChangeListener(e -> {
            if (!zoom.getValueIsAdjusting())
                pcs.firePropertyChange("tileLayerChanged", null, null);
        });
    }

    public void addListener(PropertyChangeListener l){
        pcs.addPropertyChangeListener(l);
    }

    public void updateLayers() {
        isUpdating = true;
        WebMapTileService selectedProvider = (WebMapTileService) providers.getSelectedItem();
        layers.removeAllElements();

        for (Layer layer : selectedProvider.layers())
            layers.addElement(layer);

        isUpdating = false;
        updateZoom();
    }

    public void updateZoom() {
        Layer layer = (Layer) layers.getSelectedItem();
        if (layer != null && !isUpdating){
            zoom.setRangeProperties(zoom.getValue(), -1, -1, -1, true);//force update
            int min = layer.minZoom();
            int max = layer.maxZoom();
            int value = Math.max(min, Math.min(zoom.getValue(), max));
            zoom.setRangeProperties(value, 0, min, max, false);
            System.out.println(min+","+max);
        }
    }

    public DefaultComboBoxModel<WebMapTileService> getProviderModel() {
        return providers;
    }

    public DefaultComboBoxModel<Layer> getLayerModel() {
        return layers;
    }

    public BoundedRangeModel getZoomModel() {
        return zoom;
    }
}
