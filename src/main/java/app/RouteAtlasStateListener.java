package app;

import routeatlas.RouteAtlas;

public interface RouteAtlasStateListener {
    void onAtlasChanged(RouteAtlas newAtlas);
    void onMapSelectionIndexChanged(int newIndex);
}