package ui.controller;

import app.CompilationFeatureToggle ;
import app.RouteAtlasState;
import ordnancesurvey.OrdnanceSurveyRouteDownloader;
import route.Route;
import routeatlas.RouteAtlas;
import routeatlas.RouteAtlasFactory;
import routeatlas.ScaledPaper;
import ui.view.AtlasSetupView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/** Swing Action responsible for creating a RouteAtlas in a background thread */
public class CreateAtlasAction extends AbstractAction{
    private final OrdnanceSurveyRouteDownloader downloader;
    private final AtlasSetupView atlasSetupView;
    private final RouteAtlasState routeAtlasState;
    private final CompilationFeatureToggle compilationFeatureToggle  ;

    public CreateAtlasAction(OrdnanceSurveyRouteDownloader downloader, AtlasSetupView atlasSetupView, RouteAtlasState routeAtlasState, CompilationFeatureToggle compilationFeatureToggle ){
        super("Create Atlas");
        this.downloader = downloader;
        this.atlasSetupView = atlasSetupView;
        this.routeAtlasState = routeAtlasState;
        this.compilationFeatureToggle  = compilationFeatureToggle ;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //disable atlas creation/compilation until we have created this atlas
        compilationFeatureToggle.enableAtlasCreation(false);
        compilationFeatureToggle.enableCompilation(false);
        ScaledPaper paper = getPaper();

        //create atlas in a background thread
        new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground(){
                try {
                    Route route = downloader.downloadRoute(atlasSetupView.getRouteId());
                    RouteAtlas atlas = RouteAtlasFactory.create(route, paper);
                    //once done update state
                    routeAtlasState.setAtlas(atlas);
                    //guaranteed to have a RouteAtlas now - enable compilation button
                    compilationFeatureToggle.enableCompilation(true);
                } catch (IOException ex) {
                    //route download exception
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally{
                    //remember to re-enable the creation button
                    compilationFeatureToggle.enableAtlasCreation(true);
                }

                return null;
            }

        }.execute();
    }

    private ScaledPaper getPaper() {
        return ScaledPaper.fromMetres(atlasSetupView.getPageWidth(), atlasSetupView.getPageHeight(), atlasSetupView.getPageScale());
    }
}
