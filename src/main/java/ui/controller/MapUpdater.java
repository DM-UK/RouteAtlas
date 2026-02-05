package ui.controller;

import app.RouteAtlasState;
import app.RouteAtlasStateListener;
import render.AtlasImageCreator;
import routeatlas.MapPage;
import routeatlas.RouteAtlas;
import ui.view.AtlasInfoView;
import wmts.WMTSException;

import javax.swing.*;
import java.awt.image.BufferedImage;

/** Handles map rendering updates through the RouteAtlasStateListener and external calls to requestMap(). */
public class MapUpdater implements RouteAtlasStateListener {
    private final RouteAtlasState appState;
    private final AtlasImageCreator atlasImageCreator;
    private final AtlasInfoView atlasInfoView;

    public MapUpdater(RouteAtlasState appState, AtlasInfoView atlasInfoView, AtlasImageCreator atlasImageCreator) {
        this.appState = appState;
        this.atlasInfoView = atlasInfoView;
        this.atlasImageCreator = atlasImageCreator;
    }

    public void requestMap() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    BufferedImage img = atlasImageCreator.createAtlasImage(appState.getAtlas(), appState.getMapSelectionIndex());
                    updateInfoView(img);
                } catch (WMTSException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    //throw new RuntimeException(e);
                }

                return null;
            }
        }.execute();
    }

    private void updateInfoView(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int selectionIndex = appState.getMapSelectionIndex();
        MapPage page = appState.getAtlas().getAllPages().get(selectionIndex);
        double dpi = page.getScaledPaper().getDPI(width, height);
        double sizeMb = (width * height * 4L) / (1024.0 * 1024.0);
        atlasInfoView.setInfo(page.getScaledPaper().getScale(), width, height, dpi, sizeMb);
    }

    public void cancelMapRequest() {
        atlasImageCreator.getClient().cancelCurrentRequest();
    }

    @Override
    public void onAtlasChanged(RouteAtlas newAtlas) {

    }

    @Override
    public void onMapSelectionIndexChanged(int newIndex) {
        requestMap();
    }
}
