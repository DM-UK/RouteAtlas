package ui.panes;

import render.TileLayer;
import ui.swing.BasicForm;
import wmts.Layer;
import wmts.WebMapProviders;
import wmts.WebMapTileService;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Panel containing provider/layer/zoom settings. **/
public class TileLayerSetupPane extends BasicForm implements TileLayer {
    private final JComboBox<WebMapTileService> providerField;
    private final JComboBox<Layer> layerField;
    private final JSlider zoomField;
    private final TileLayerModel tileLayerModel = new TileLayerModel();

    public TileLayerSetupPane(){
        providerField = addComboBoxField("Provider", tileLayerModel.getProviderModel());
        layerField = addComboBoxField("Layer", tileLayerModel.getLayerModel());
        zoomField = addSliderField("Zoom", tileLayerModel.getZoomModel());
        providerField.addActionListener(e -> tileLayerModel.updateLayers());//update model on provider change
        layerField.addActionListener(e -> tileLayerModel.updateZoom());//update model on layer change
    }

    public TileLayerModel getTileLayerModel() {
        return tileLayerModel;
    }

    @Override
    public WebMapTileService getProvider() {
        return (WebMapTileService) providerField.getSelectedItem();
    }

    @Override
    public Layer getLayer() {
        return (Layer) layerField.getSelectedItem();
    }

    @Override
    public int getZoom() {
        return zoomField.getValue();
    }

    public void setProvider(int index) {
        providerField.setSelectedIndex(index);
    }

    public void setZoom(int zoom) {
        zoomField.setValue(zoom);
    }

    /** Auto-updates provider/layer/zoom UI models. **/
    public static class TileLayerModel{
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
}