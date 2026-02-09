package render;


import routeatlas.MapPage;
import routeatlas.RouteAtlas;
import wmts.*;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.CancellationException;

public class AtlasImageCreator {
    private final RenderSetup overviewRenderSetup;
    private final RenderSetup sectionRenderSetup;
    private final ProgressListener progressListener;
    private final WMTSClient client;

    public AtlasImageCreator(Path tileCacheDir, RenderSetup overviewRenderSetup, RenderSetup sectionRenderSetup, ProgressListener progressListener) {
        this.overviewRenderSetup = overviewRenderSetup;
        this.sectionRenderSetup = sectionRenderSetup;
        this.progressListener = progressListener;
        this.client = new DefaultWMTSClient(tileCacheDir);
    }

    /** Returns a rendered map image for the given atlas page. */
    public BufferedImage createAtlasImage(RouteAtlas atlas, int pageIndex) throws WMTSException, CancellationException {
        //get either the overview or section map setup
        RenderSetup renderSetup = selectRenderer(pageIndex);
        //transform atlas to the CRS that the WMTS is in
        RouteAtlas transformedAtlas = atlas.convertCRS(renderSetup.getTileLayer().getProvider().crs());
        MapPage transformedMap = transformedAtlas.getAllPages().get(pageIndex);
        BufferedImage image = createMapImage(renderSetup, transformedMap);
        //convert to miles if necessary
        if (renderSetup.getRenderSettings().useMileUnits())
            transformedAtlas = transformedAtlas.convertAtlasToMiles();

        //now draw onto our map image
        AtlasMapRenderer atlasRenderer = new AtlasMapRenderer(transformedMap, image, transformedAtlas, pageIndex, renderSetup.getRenderSettings());
        ElevationProfileRenderer elevationProfileRenderer = new ElevationProfileRenderer(transformedAtlas, image, transformedMap, renderSetup.getElevationSettings());
        atlasRenderer.render();
        elevationProfileRenderer.render();
        //update our progressListener with our updated image
        //NOTE: this may may cause undesired behaviour because onComplete would have already been called during initial image creation. Will leave for time being.
        progressListener.onComplete(image);
        return image;
    }

    private BufferedImage createMapImage(RenderSetup renderSetup, MapPage transformedMap) throws WMTSException, CancellationException {
        TileLayer tileLayer = renderSetup.getTileLayer();
        WebMapTileService provider = tileLayer.getProvider();
        MapRequest request = new MapRequest(provider, transformedMap.getBounds(), tileLayer.getLayer(), tileLayer.getZoom(), progressListener);
        return client.requestMap(request);
    }

    private RenderSetup selectRenderer(int pageIndex) {
        if (pageIndex == 0)
            return overviewRenderSetup;
        else
            return sectionRenderSetup;
    }

    public WMTSClient getClient() {
        return client;
    }
}
