package ui.panes;

import render.TileLayer;
import ui.TileLayerModel;
import ui.swing.BasicForm;
import wmts.Layer;
import wmts.WebMapTileService;

import javax.swing.*;

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
}