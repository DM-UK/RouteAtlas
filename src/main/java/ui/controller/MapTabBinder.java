package ui.controller;

import app.RouteAtlasState;
import app.RouteAtlasStateListener;
import routeatlas.RouteAtlas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Responsible for 'binding' the SingleSelectionModel model (in our case of a tabbedpane) to that of a RouteAtlasState's selectionIndex. **/
public class MapTabBinder implements ChangeListener, RouteAtlasStateListener {
    private static final int SINGLE_SELECTION_MODEL_MAXIMUM_INDEX = 1; // since we only have two tabs. set to 0 or 1 (maximum)
    private final RouteAtlasState routeAtlasState;
    private final SingleSelectionModel tabModel;
    private boolean updating = false; // prevent endless loop

    public MapTabBinder(RouteAtlasState routeAtlasState, SingleSelectionModel tabModel) {
        this.routeAtlasState = routeAtlasState;
        this.tabModel = tabModel;
        routeAtlasState.addListener(this);
        tabModel.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (updating)
            return;

        updating = true;
        routeAtlasState.setMapSelectionIndex(tabModel.getSelectedIndex());
        updating = false;
    }

    @Override
    public void onMapSelectionIndexChanged(int index) {
        if (updating)
            return;

        updating = true;
        index = Math.min(SINGLE_SELECTION_MODEL_MAXIMUM_INDEX, index);
        tabModel.setSelectedIndex(index);
        updating = false;
    }

    @Override
    public void onAtlasChanged(RouteAtlas newAtlas) {

    }
}