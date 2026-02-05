package ui.controller;

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
                    compilationControl.setCompilationEnabled(false);
                    compilationControl.setCompilationEnabled(false);
                    Path filePath = routeAtlasCompiler.compile(routeAtlasState.getAtlas());
                    openFolder(filePath.getParent());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally{
                    compilationControl.setCompilationEnabled(true);
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
}
