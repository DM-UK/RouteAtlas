package render;

public interface RenderSetup {
    TileLayer getTileLayer();

    MapRenderSettings getRenderSettings();

    ElevationProfileSettings getElevationSettings();
}
