package ui.controller;

import app.CompilationFeatureToggle;
import app.RouteAtlasState;
import routeatlas.RouteAtlasCompiler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;

/** Swing Action responsible for compiling a RouteAtlas to pdf in a background thread */
public class CompileAtlasAction extends AbstractAction {
    private final RouteAtlasCompiler routeAtlasCompiler;
    private final RouteAtlasState routeAtlasState;
    private final CompilationFeatureToggle compilationFeatureToggle;

    public CompileAtlasAction(RouteAtlasCompiler routeAtlasCompiler, RouteAtlasState routeAtlasState, CompilationFeatureToggle compilationFeatureToggle) {
        super("Compile PDF");
        this.routeAtlasCompiler = routeAtlasCompiler;
        this.routeAtlasState = routeAtlasState;
        this.compilationFeatureToggle = compilationFeatureToggle;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground(){
                try {
                    compilationFeatureToggle.enableCompilation(false);
                    compilationFeatureToggle.enableAtlasCreation(false);
                    Path filePath = routeAtlasCompiler.compile(routeAtlasState.getAtlas());
                    openFolder(filePath.getParent());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally{
                    compilationFeatureToggle.enableCompilation(true);
                    compilationFeatureToggle.enableAtlasCreation(true);
                }

                return null;
            }

        }.execute();
    }

    private void openFolder(Path path) {
        try {
            Desktop.getDesktop().open(path.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
