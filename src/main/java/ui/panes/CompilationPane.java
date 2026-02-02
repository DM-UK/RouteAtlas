package ui.panes;

import javax.swing.*;
import java.awt.*;

/** Panel containing the compile button and compilation progress bar. **/
public class CompilationPane extends JPanel {
    private final ProgressBarPane progressBar = new ProgressBarPane("Compiling...");
    private final JButton compilationButton = new JButton();

    public CompilationPane() {
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.add(compilationButton);
        setLayout(new BorderLayout());
        add(buttonWrapper, BorderLayout.NORTH);
        add(progressBar, BorderLayout.SOUTH);
    }

    public ProgressBarPane getProgressBar() {
        return progressBar;
    }

    public JButton getCompilationButton() {
        return compilationButton;
    }

    public void setCancelAction(Runnable action) {
        progressBar.setCancelAction(action);
    }
}