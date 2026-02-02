package ui.panes;

import wmts.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Renders a JProgressBar and a cancel button.
 *  Task progress is reported via the ProgressListener interface.
 *  The panel is only visible between onStart and onCancelled/onComplete. **/
public class ProgressBarPane extends JPanel implements ProgressListener {
    private final JProgressBar bar = new JProgressBar();
    private final JButton cancelBtn = new JButton("Cancel");

    public ProgressBarPane(String name){
        JPanel inner = new JPanel();
        inner.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        bar.setStringPainted(true);
        inner.add(bar);
        inner.add(cancelBtn);
        add(inner);
        setBorder(BorderFactory.createTitledBorder(name));
        setVisible(false);
    }

    public void setCancelAction(Runnable action) {
        cancelBtn.addActionListener(e -> action.run());
    }

    @Override
    public void onStart(int total) {
        setVisible(true);
        bar.setMinimum(0);
        bar.setMaximum(total);
        bar.setValue(0);
        setBarString(0, total);
    }

    private void setBarString(int completed, int total) {
        if (total <= 0) {
            bar.setString("Starting...");
            return;
        }

        bar.setString(completed + " / " + total);
    }

    @Override
    public void onProgress(int completed, int total) {
        setBarString(completed, total);
        bar.setValue(completed);
    }

    @Override
    public void onCancelled(String reason) {
        setVisible(false);
    }

    @Override
    public void onComplete(BufferedImage img) {
        setVisible(false);
    }
}
