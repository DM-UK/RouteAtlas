package ui.controller;

import app.RouteAtlasState;
import render.RouteAtlasCompiler;
import wmts.WMTSException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;

/** Swing Action responsible for compiling a RouteAtlas to a pdf in a background thread. */
public class CompileAtlasAction extends AbstractAction {
    private final RouteAtlasCompiler routeAtlasCompiler;
    private final RouteAtlasState routeAtlasState;
    private final CompilationControl compilationControl;

    public CompileAtlasAction(RouteAtlasCompiler routeAtlasCompiler, RouteAtlasState routeAtlasState, CompilationControl compilationControl) {
        super("Compile PDF");
        this.routeAtlasCompiler = routeAtlasCompiler;
        this.routeAtlasState = routeAtlasState;
        this.compilationControl = compilationControl;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground(){
                try {
                    //prevent atlas compilation/creation until complete
                    compilationControl.setAtlasCreationEnabled(false);
                    compilationControl.setCompilationEnabled(false);
                    Path filePath = routeAtlasCompiler.compile(routeAtlasState.getAtlas());
                    openFolder(filePath.getParent());
                } catch (IOException | WMTSException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally{
                    //re-enable
                    compilationControl.setAtlasCreationEnabled(true);
                    compilationControl.setCompilationEnabled(true);
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

    public void cancel() {
        routeAtlasCompiler.cancel();
    }
}
