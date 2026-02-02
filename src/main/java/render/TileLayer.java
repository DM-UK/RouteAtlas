package render;

import wmts.Layer;
import wmts.WebMapTileService;

public interface TileLayer {
    WebMapTileService getProvider();

    Layer getLayer();

    int getZoom();
}
