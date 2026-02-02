package app;

import routeatlas.RouteAtlas;

import java.util.ArrayList;
import java.util.List;

public class RouteAtlasState {
    private RouteAtlas atlas;
    private int mapSelectionIndex;

    private final List<RouteAtlasStateListener> listeners = new ArrayList<>();

    public int getMapSelectionIndex() {
        return mapSelectionIndex;
    }

    public RouteAtlas getAtlas() {
        return atlas;
    }

    public void setAtlas(RouteAtlas atlas) {
        this.atlas = atlas;
        mapSelectionIndex = 0;
        notifyAtlasChanged();
        notifyMapSelectionChanged();
    }

    public void setMapSelectionIndex(int mapSelectionIndex) {
        this.mapSelectionIndex = mapSelectionIndex;
        notifyMapSelectionChanged();
    }

    public void selectNextMapIndex() {
        if (atlas == null)
            return;

        int size = atlas.getAllPages().size();
        mapSelectionIndex = (mapSelectionIndex + 1) % size;
        notifyMapSelectionChanged();
    }

    public void selectPreviousMapIndex() {
        if (atlas == null)
            return;

        int size = atlas.getAllPages().size();
        mapSelectionIndex--;
        if (mapSelectionIndex < 0)
            mapSelectionIndex = size - 1;

        notifyMapSelectionChanged();
    }

    public void addListener(RouteAtlasStateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RouteAtlasStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyAtlasChanged() {
        for (RouteAtlasStateListener l : listeners) {
            l.onAtlasChanged(atlas);
        }
    }

    private void notifyMapSelectionChanged() {
        for (RouteAtlasStateListener l : listeners) {
            l.onMapSelectionIndexChanged(mapSelectionIndex);
        }
    }
}
